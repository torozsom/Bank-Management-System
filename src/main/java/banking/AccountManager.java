package banking;

import java.sql.*;


public class AccountManager {
    private final String dataBaseURL;
    private final Connection connection;


    public AccountManager() throws SQLException {
        dataBaseURL = "jdbc:sqlite:Banking.db";
        connection = DriverManager.getConnection(dataBaseURL);
    }


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
    public void addAccount(Account a) throws SQLException {
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
     * @throws SQLException
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
