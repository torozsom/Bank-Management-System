package banking.data;

import banking.model.Account;
import banking.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 * TransactionManager is responsible for managing transactions in the banking application.
 * It provides methods to save, load, and delete transactions from the database.
 */
public class TransactionManager {

    private final Connection connection;
    private final AccountManager accountManager;


    /**
     * Constructor for TransactionManager.
     * Initializes the connection to the database and the AccountManager.
     *
     * @throws SQLException if a database access error occurs
     */
    public TransactionManager() throws SQLException {
        connection = DatabaseManager.getInstance().getConnection();
        accountManager = new AccountManager();
    }


    /**
     * Saves a transaction to the database.
     * Checks if both sender and receiver accounts exist before saving.
     *
     * @param transaction the transaction to be saved
     * @return true if the transaction was saved successfully, false otherwise
     * @throws SQLException if a database access error occurs
     */
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
     * Searches for transactions based on the given account in the
     * database and returns them as a list of Transaction objects.
     * Uses optimized JOIN query to avoid N+1 query problem.
     *
     * @param a the account to search transactions for
     * @return a List of Transactions that involve the given account (as sender or receiver)
     * @throws SQLException when connection is unsuccessful
     */
    public List<Transaction> loadTransactions(Account a) throws SQLException {
        String query = """
                SELECT t.transaction_id, t.amount, t.comment, t.date,
                       s.account_id as sender_id, s.user_id as sender_user_id, s.account_number as sender_number, 
                       s.balance as sender_balance, s.is_frozen as sender_frozen,
                       r.account_id as receiver_id, r.user_id as receiver_user_id, r.account_number as receiver_number, 
                       r.balance as receiver_balance, r.is_frozen as receiver_frozen
                FROM Transactions t
                JOIN Accounts s ON t.sender_account_number = s.account_number
                JOIN Accounts r ON t.receiver_account_number = r.account_number
                WHERE t.receiver_account_number = ? OR t.sender_account_number = ?
                ORDER BY t.date DESC
                """;

        List<Transaction> transactions = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, a.getAccountNumber());
            statement.setInt(2, a.getAccountNumber());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int transactionID = resultSet.getInt("transaction_id");
                    double amount = resultSet.getDouble("amount");
                    String comment = resultSet.getString("comment");

                    String date = resultSet.getString("date");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

                    // Create sender account from joined data
                    Account sender = new Account(
                            resultSet.getInt("sender_id"),
                            resultSet.getInt("sender_user_id"),
                            resultSet.getInt("sender_number"),
                            resultSet.getDouble("sender_balance"),
                            resultSet.getBoolean("sender_frozen")
                    );

                    // Create receiver account from joined data
                    Account receiver = new Account(
                            resultSet.getInt("receiver_id"),
                            resultSet.getInt("receiver_user_id"),
                            resultSet.getInt("receiver_number"),
                            resultSet.getDouble("receiver_balance"),
                            resultSet.getBoolean("receiver_frozen")
                    );

                    transactions.add(new Transaction(transactionID, sender, receiver, amount, comment, localDateTime));
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
