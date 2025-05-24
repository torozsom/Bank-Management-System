package banking.controller;

import banking.util.DatabaseManager;
import banking.model.Account;
import banking.model.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class TransactionManager {

    private final Connection connection;
    private final AccountManager accountManager;


    public TransactionManager() throws SQLException {
        connection = DatabaseManager.getInstance().getConnection();
        accountManager = new AccountManager();
    }


    public boolean saveTransaction(Transaction transaction) throws SQLException {
        if (!accountManager.accountExists(transaction.getSender().getAccountNumber())
                || !accountManager.accountExists(transaction.getReceiver().getAccountNumber()))
            return false;

        String query = "INSERT INTO Transactions " +
                "(sender_account_number, receiver_account_number, amount, comment, date) VALUES (?, ?, ?, ?, ?)";

        LocalDateTime date = transaction.getDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateOfTransaction = date.format(formatter);

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, transaction.getSender().getAccountNumber());
            statement.setInt(2, transaction.getReceiver().getAccountNumber());
            statement.setDouble(3, transaction.getAmount());
            statement.setString(4, transaction.getComment());
            statement.setString(5, dateOfTransaction);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        }
    }


    /**
     * Searches for transactions based on the given user id in the
     * database and returns it with all its data as an object.
     *
     * @param a the id number that is searched for
     * @return a List of Accounts that belong to the user with the given id
     * @throws SQLException when connection is unsuccessful
     */
    public List<Transaction> loadTransactions(Account a) throws SQLException {
        String query = "SELECT * FROM Transactions WHERE receiver_account_number = ? OR sender_account_number = ?";
        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, a.getAccountNumber());
            statement.setInt(2, a.getAccountNumber());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int transactionID = resultSet.getInt("transaction_id");
                    int senderNumber = resultSet.getInt("sender_account_number");
                    int receiverNumber = resultSet.getInt("receiver_account_number");
                    double amount = resultSet.getDouble("amount");
                    String comment = resultSet.getString("comment");

                    String date = resultSet.getString("date");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

                    if (accountManager.accountExists(senderNumber) && accountManager.accountExists(receiverNumber)) {
                        Account sender = accountManager.loadAccount(senderNumber);
                        Account receiver = accountManager.loadAccount(receiverNumber);
                        transactions.add(new Transaction(transactionID, sender, receiver, amount, comment, localDateTime));
                    }
                }
            }
        }
        return transactions;
    }


    /**
     * Deletes a transaction from the database.
     *
     * @param transactionID the ID of the transaction to be deleted
     * @return true if the transaction was deleted successfully, false otherwise
     * @throws SQLException if a database error occurs
     */
    public boolean deleteTransaction(int transactionID) throws SQLException {
        String query = "DELETE FROM Transactions WHERE transaction_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, transactionID);
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;
        }
    }

}
