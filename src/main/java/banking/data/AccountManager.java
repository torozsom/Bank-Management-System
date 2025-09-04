package banking.data;

import banking.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * AccountManager is responsible for managing accounts in the banking application.
 * It provides methods to save, load, deposit, withdraw, transfer, freeze, unfreeze,
 * and delete accounts in the database.
 */
public class AccountManager {

    private final Connection connection;


    /**
     * Constructor for AccountManager that initializes the database connection.
     *
     * @throws SQLException when connection is unsuccessful
     */
    public AccountManager() throws SQLException {
        connection = DatabaseManager.getInstance().getConnection();
    }


    /**
     * Saves an account with its data in the database.
     *
     * @param a the account to be inserted into the table
     * @throws SQLException when connection is unsuccessful
     */
    public boolean saveAccount(Account a) throws SQLException {
        if (accountExists(a.getAccountNumber()))
            return false;

        String query = "INSERT INTO Accounts (user_id, account_number, balance, is_frozen) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, a.getUserID());
            statement.setInt(2, a.getAccountNumber());
            statement.setDouble(3, a.getBalance());
            statement.setBoolean(4, a.isFrozen());
            statement.executeUpdate();
        }
        return true;
    }


    /**
     * Checks if an account with the given number exists already in the database.
     *
     * @param accountNumber the account number that is being searched for
     * @return True if the account number is found in the table
     * @throws SQLException when connection is unsuccessful
     */
    public boolean accountExists(int accountNumber) throws SQLException {
        String query = "SELECT * FROM Accounts WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, accountNumber);
            ResultSet result = statement.executeQuery();
            return result.next();
        }
    }


    /**
     * Searches for an account based on account number in the
     * database and returns it with all its data as an object.
     *
     * @param accountNumber the number that is searched for
     * @return an Account object with the given account number
     * @throws SQLException when connection is unsuccessful
     */
    public Account loadAccount(int accountNumber) throws SQLException {
        String query = "SELECT * FROM Accounts WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, accountNumber);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                int accID = result.getInt("account_id");
                int userID = result.getInt("user_id");
                int accNum = result.getInt("account_number");
                double balance = result.getDouble("balance");
                boolean isFrozen = result.getBoolean("is_frozen");

                return new Account(accID, userID, accNum, balance, isFrozen);
            }
            return null;
        }
    }


    /**
     * Searches for accounts based on the given user ID in the
     * database and returns them with all their data as objects.
     *
     * @param user_id the user ID number that is searched for
     * @return a List of Accounts that belong to the user with the given ID
     * @throws SQLException when connection is unsuccessful
     */
    public List<Account> loadAccounts(int user_id) throws SQLException {
        String query = "SELECT * FROM Accounts WHERE user_id = ?";
        List<Account> accounts = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, user_id);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    int accID = result.getInt("account_id");
                    int accNum = result.getInt("account_number");
                    double balance = result.getDouble("balance");
                    boolean isFrozen = result.getBoolean("is_frozen");
                    accounts.add(new Account(accID, user_id, accNum, balance, isFrozen));
                }
                return accounts;
            }
        }
    }


    /**
     * Deposits the specified amount into the given account.
     * Uses atomic database operations to prevent race conditions.
     *
     * @param acc    the account to deposit to
     * @param amount the amount to deposit
     * @throws SQLException             when a database error occurs
     * @throws IllegalArgumentException if the amount is not positive or the account is frozen
     */
    public void depositMoney(Account acc, double amount) throws SQLException {
        if (amount <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive");

        // Use atomic database operation with frozen check
        String query = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ? AND is_frozen = 0";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, amount);
            statement.setInt(2, acc.getAccountNumber());
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // Update in-memory object only after successful database update
                acc.deposit(amount);
            } else {
                // Check if account exists and is frozen to provide specific error message
                Account currentAccount = loadAccount(acc.getAccountNumber());

                if (currentAccount == null)
                    throw new IllegalArgumentException("Account does not exist");

                if (currentAccount.isFrozen())
                    throw new IllegalArgumentException("Cannot deposit to a frozen account");

            }
        }
    }


    /**
     * Withdraws the specified amount from the given account.
     * Uses atomic database operations to prevent race conditions.
     *
     * @param acc    the account to withdraw from
     * @param amount the amount to withdraw
     * @throws SQLException             when a database error occurs
     * @throws IllegalArgumentException if the amount is not positive, exceeds the balance, or the account is frozen
     */
    public void withdrawMoney(Account acc, double amount) throws SQLException {
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive");

        // Use atomic database operation with balance and frozen checks
        String query = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ? AND balance >= ? AND is_frozen = 0";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, amount);
            statement.setInt(2, acc.getAccountNumber());
            statement.setDouble(3, amount);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                // Update in-memory object only after successful database update
                acc.withdraw(amount);
            } else {
                // Check specific failure reason to provide appropriate error message
                Account currentAccount = loadAccount(acc.getAccountNumber());

                if (currentAccount == null)
                    throw new IllegalArgumentException("Account does not exist");

                if (currentAccount.isFrozen())
                    throw new IllegalArgumentException("Cannot withdraw from a frozen account");

                if (currentAccount.getBalance() < amount)
                    throw new IllegalArgumentException("Insufficient funds");

            }
        }
    }


    /**
     * Transfers the specified amount from one account to another.
     * Uses atomic database operations within a transaction to prevent race conditions.
     *
     * @param sourceAccount      the source account number
     * @param destinationAccount the destination account number
     * @param amount             the amount to transfer
     * @throws SQLException             when a database error occurs
     * @throws IllegalArgumentException if the amount is not positive, accounts are the same,
     *                                  accounts don't exist, insufficient funds, or either account is frozen
     */
    public void transferMoney(int sourceAccount, int destinationAccount, double amount) throws SQLException {
        if (amount <= 0)
            throw new IllegalArgumentException("Transfer amount must be positive");

        if (sourceAccount == destinationAccount)
            throw new IllegalArgumentException("Source and destination accounts cannot be the same");

        // Use atomic operations within transaction with all validations in SQL
        String deductQuery = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ? AND balance >= ? AND is_frozen = 0";
        String addQuery = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ? AND is_frozen = 0";

        try {
            connection.setAutoCommit(false);

            // First, deduct from source account with atomic validation
            try (PreparedStatement deductStatement = connection.prepareStatement(deductQuery)) {
                deductStatement.setDouble(1, amount);
                deductStatement.setInt(2, sourceAccount);
                deductStatement.setDouble(3, amount);
                int sourceRowsAffected = deductStatement.executeUpdate();

                if (sourceRowsAffected == 0) {
                    connection.rollback();
                    // Determine specific failure reason
                    Account source = loadAccount(sourceAccount);
                    if (source == null)
                        throw new IllegalArgumentException("Source account does not exist");

                    if (source.isFrozen())
                        throw new IllegalArgumentException("Cannot transfer from a frozen account");

                    if (source.getBalance() < amount)
                        throw new IllegalArgumentException("Insufficient funds in source account");

                    throw new IllegalArgumentException("Transfer failed: source account validation failed");
                }

                // Then, add to destination account with atomic validation
                try (PreparedStatement addStatement = connection.prepareStatement(addQuery)) {
                    addStatement.setDouble(1, amount);
                    addStatement.setInt(2, destinationAccount);
                    int destRowsAffected = addStatement.executeUpdate();

                    if (destRowsAffected == 0) {
                        connection.rollback();
                        // Determine specific failure reason
                        Account destination = loadAccount(destinationAccount);

                        if (destination == null)
                            throw new IllegalArgumentException("Destination account does not exist");

                        if (destination.isFrozen())
                            throw new IllegalArgumentException("Cannot transfer to a frozen account");

                        throw new IllegalArgumentException("Transfer failed: destination account validation failed");
                    }

                    // Both operations successful, commit transaction
                    connection.commit();

                    // Update in-memory objects only after successful database operations
                    Account source = loadAccount(sourceAccount);
                    Account destination = loadAccount(destinationAccount);
                    if (source != null) source.withdraw(amount);
                    if (destination != null) destination.deposit(amount);

                }
            }
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true);
        }
    }


    /**
     * Freezes the given account.
     *
     * @param acc the account to freeze
     * @throws SQLException when a database error occurs
     */
    public void freezeAccount(Account acc) throws SQLException {
        String query = "UPDATE Accounts SET is_frozen = 1 WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, acc.getAccountNumber());
            statement.executeUpdate();
        }
    }


    /**
     * Unfreezes the given account.
     *
     * @param acc the account to unfreeze
     * @throws SQLException when a database error occurs
     */
    public void unfreezeAccount(Account acc) throws SQLException {
        String query = "UPDATE Accounts SET is_frozen = 0 WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, acc.getAccountNumber());
            statement.executeUpdate();
        }
    }


    /**
     * Deletes the given account from the database.
     *
     * @param acc the account to be deleted
     * @throws SQLException when connection is unsuccessful
     */
    public void deleteAccount(Account acc) throws SQLException {
        String query = "DELETE FROM Accounts WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, acc.getAccountNumber());
            statement.executeUpdate();
        }
    }

}
