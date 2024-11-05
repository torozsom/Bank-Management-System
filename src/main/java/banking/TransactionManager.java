package banking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TransactionManager {

    private final String dataBaseURL;
    private final Connection connection;
    private final AccountManager accountManager;


    public TransactionManager() throws SQLException {
        dataBaseURL = "jdbc:sqlite:Banking.db";
        connection = DriverManager.getConnection(dataBaseURL);
        accountManager = new AccountManager();
    }

    /// Safely closes the connection to the database
    private void close() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
    }


    public void saveTransaction(Transaction transaction) throws SQLException {
        if (!accountManager.accountExists(transaction.getSender().getAccountNumber())
                || !accountManager.accountExists(transaction.getReceiver().getAccountNumber()))
            return;

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
            statement.executeUpdate();
        }
    }


//TODO: public Transaction getTransaction(sth) throws SQLException {}


}
