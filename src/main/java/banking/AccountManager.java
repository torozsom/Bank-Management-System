package banking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class AccountManager {

    private final String dataBaseURL;
    private final Connection connection;


    public AccountManager() throws SQLException {
        dataBaseURL = "jdbc:sqlite:Banking.db";
        connection = DriverManager.getConnection(dataBaseURL);
    }


    /// Safely closes the connection to the database
    void close() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
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
     * Checks if an account with the given number exists already in the DB.
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
     * Searches for accounts based on the given user id in the
     * database and returns it with all its data as an object.
     *
     * @param user_id the id number that is searched for
     * @return a List of Accounts that belong to the user with the given id
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
     *
     * @param acc    the account to deposit to
     * @param amount the amount to deposit
     * @return true if the deposit was successful, false otherwise
     * @throws SQLException when a database error occurs
     */
    public boolean depositMoney(Account acc, double amount) throws SQLException {
        if (amount <= 0)
            return false;

        String query = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, amount);
            statement.setInt(2, acc.getAccountNumber());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }


    /**
     * Withdraws the specified amount from the given account.
     *
     * @param acc    the account to withdraw from
     * @param amount the amount to withdraw
     * @return true if the withdrawal was successful, false otherwise
     * @throws SQLException when a database error occurs
     */
    public boolean withdrawMoney(Account acc, double amount) throws SQLException {
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive.");

        // Check for sufficient funds
        Account account = loadAccount(acc.getAccountNumber());
        if (account.getBalance() < amount)
            return false;

        String query = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setDouble(1, amount);
            statement.setInt(2, acc.getAccountNumber());
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }


    /**
     * Transfers the specified amount from one account to another.
     *
     * @param sourceAccount      the source account number
     * @param destinationAccount the destination account number
     * @param amount             the amount to transfer
     * @return true if the transfer was successful, false otherwise
     * @throws SQLException when a database error occurs
     */
    public boolean transferMoney(int sourceAccount, int destinationAccount, double amount) throws SQLException {
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive.");

        Account source = loadAccount(sourceAccount);
        if (source.getBalance() < amount)
            return false;

        if (!accountExists(destinationAccount))
            throw new IllegalArgumentException("Destination account does not exist.");

        String deductQuery = "UPDATE Accounts SET balance = balance - ? WHERE account_number = ?";
        String addQuery = "UPDATE Accounts SET balance = balance + ? WHERE account_number = ?";

        try (PreparedStatement deductStatement = connection.prepareStatement(deductQuery);
             PreparedStatement addStatement = connection.prepareStatement(addQuery)) {
            connection.setAutoCommit(false);

            // Deduct from source
            deductStatement.setDouble(1, amount);
            deductStatement.setInt(2, sourceAccount);
            deductStatement.executeUpdate();

            // Add to destination
            addStatement.setDouble(1, amount);
            addStatement.setInt(2, destinationAccount);
            addStatement.executeUpdate();

            connection.commit();
            return true;
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(true); // Restore default behavior
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
     * @param acc the account to freeze
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
     * Deletes the given account from the database
     *
     * @param acc the account to be deleted
     * @return True when deletion is successful, false otherwise.
     * @throws SQLException when connection is unsuccessful
     */
    public boolean deleteAccount(Account acc) throws SQLException {
        String query = "DELETE FROM Accounts WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, acc.getAccountNumber());
            statement.executeUpdate();
            return true;
        }
    }

}
