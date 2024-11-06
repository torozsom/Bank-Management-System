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
    private void close() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
    }

    /**
     * Saves an account with its data in the database.
     *
     * @param a the account to be inserted into the table
     * @throws SQLException when connection is unsuccessful
     */
    public void saveAccount(Account a) throws SQLException {
        if (accountExists(a.getAccountNumber()))
            return;

        String query = "INSERT INTO Accounts (user_id, account_number, balance, is_frozen) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, a.getUserID());
            statement.setInt(2, a.getAccountNumber());
            statement.setDouble(3, a.getBalance());
            statement.setBoolean(4, a.isFrozen());
            statement.executeUpdate();
        }
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
     * Deletes an account from the database
     *
     * @param number the account number to be searched for
     * @return True when deletion is successful, false otherwise.
     * @throws SQLException when connection is unsuccessful
     */
    public boolean deleteAccount(int number) throws SQLException {
        String query = "DELETE FROM Accounts WHERE account_number = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, number);
            statement.executeUpdate();
            return true;
        }
    }

}
