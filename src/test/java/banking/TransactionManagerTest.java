package banking;

import banking.data.AccountManager;
import banking.data.DatabaseManager;
import banking.data.TransactionManager;
import banking.data.UserManager;
import banking.model.Account;
import banking.model.Transaction;
import banking.model.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionManagerTest {

    private Connection connection;
    private TransactionManager transactionManager;
    private Account senderAccount;
    private Account receiverAccount;


    @BeforeAll
    void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:config/Banking.db");
        UserManager userManager = new UserManager();
        AccountManager accountManager = new AccountManager();
        transactionManager = new TransactionManager();

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM Transactions");
            statement.executeUpdate("DELETE FROM Accounts");
            statement.executeUpdate("DELETE FROM Users");
        }

        User testUser1 = new User("test1@example.com", "password1", LocalDateTime.now());
        User testUser2 = new User("test2@example.com", "password2", LocalDateTime.now());
        testUser1.setUserID(userManager.saveUser(testUser1));
        testUser2.setUserID(userManager.saveUser(testUser2));

        senderAccount = new Account(testUser1.getUserID(), 12345678, 10000.0, false);
        receiverAccount = new Account(testUser2.getUserID(), 87654321, 5000.0, false);
        accountManager.saveAccount(senderAccount);
        accountManager.saveAccount(receiverAccount);
    }


    @Test
    void testSaveTransaction() throws SQLException {
        Transaction transaction = new Transaction(senderAccount, receiverAccount, 2000.0, "Test Transfer", LocalDateTime.now());
        boolean success = transactionManager.saveTransaction(transaction);
        assertTrue(success, "Transaction should be saved successfully");
    }


    @Test
    void testLoadTransactionsForAccount() throws SQLException {
        List<Transaction> existingTransactions = transactionManager.loadTransactions(senderAccount);
        for (Transaction transaction : existingTransactions)
            transactionManager.deleteTransaction(transaction.transactionID());

        Transaction transaction1 = new Transaction(senderAccount, receiverAccount, 1000.0, "Test Load 1", LocalDateTime.now());
        Transaction transaction2 = new Transaction(senderAccount, receiverAccount, 2000.0, "Test Load 2", LocalDateTime.now());
        transactionManager.saveTransaction(transaction1);
        transactionManager.saveTransaction(transaction2);
        List<Transaction> senderTransactions = transactionManager.loadTransactions(senderAccount);

        assertEquals(2, senderTransactions.size(), "Sender account should have 2 transactions");
        assertTrue(senderTransactions.stream().anyMatch(t -> t.amount() == 1000.0 && t.comment().equals("Test Load 1")),
                "First transaction should be loaded correctly");
        assertTrue(senderTransactions.stream().anyMatch(t -> t.amount() == 2000.0 && t.comment().equals("Test Load 2")),
                "Second transaction should be loaded correctly");
    }


    @Test
    void testDeleteTransaction() throws SQLException {
        Transaction transaction = new Transaction(senderAccount, receiverAccount, 3000.0, "Test Delete", LocalDateTime.now());
        transactionManager.saveTransaction(transaction);

        List<Transaction> senderTransactions = transactionManager.loadTransactions(senderAccount);
        assertFalse(senderTransactions.isEmpty(), "Sender account should have at least one transaction after saving");

        Transaction savedTransaction = senderTransactions.stream()
                .filter(t -> t.amount() == transaction.amount() && t.comment().equals(transaction.comment()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Transaction not found after saving"));

        boolean deleted = transactionManager.deleteTransaction(savedTransaction.transactionID());
        assertTrue(deleted, "Transaction should be deleted");

        List<Transaction> updatedSenderTransactions = transactionManager.loadTransactions(senderAccount);
        assertTrue(updatedSenderTransactions.stream().noneMatch(t -> t.transactionID() == savedTransaction.transactionID()),
                "Deleted transaction should not exist in sender's transactions");

        List<Transaction> updatedReceiverTransactions = transactionManager.loadTransactions(receiverAccount);
        assertTrue(updatedReceiverTransactions.stream().noneMatch(t -> t.transactionID() == savedTransaction.transactionID()),
                "Deleted transaction should not exist in receiver's transactions");
    }


    @AfterAll
    void teardown() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM Transactions");
            statement.executeUpdate("DELETE FROM Accounts");
            statement.executeUpdate("DELETE FROM Users");
        }

        DatabaseManager.getInstance().closeConnection();
    }

}
