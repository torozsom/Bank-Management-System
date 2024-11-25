package banking;

import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransactionManagerTest {

    private Connection connection;
    private TransactionManager transactionManager;
    private AccountManager accountManager;
    private UserManager userManager;
    private User testUser1;
    private User testUser2;
    private Account senderAccount;
    private Account receiverAccount;

    @BeforeAll
    void setupDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:Banking.db");
        userManager = new UserManager();
        accountManager = new AccountManager();
        transactionManager = new TransactionManager();

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM Transactions");
            statement.executeUpdate("DELETE FROM Accounts");
            statement.executeUpdate("DELETE FROM Users");
        }

        testUser1 = new User("test1@example.com", "password1", LocalDateTime.now());
        testUser2 = new User("test2@example.com", "password2", LocalDateTime.now());
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
            transactionManager.deleteTransaction(transaction.getTransactionID());

        Transaction transaction1 = new Transaction(senderAccount, receiverAccount, 1000.0, "Test Load 1", LocalDateTime.now());
        Transaction transaction2 = new Transaction(senderAccount, receiverAccount, 2000.0, "Test Load 2", LocalDateTime.now());
        transactionManager.saveTransaction(transaction1);
        transactionManager.saveTransaction(transaction2);
        List<Transaction> senderTransactions = transactionManager.loadTransactions(senderAccount);

        assertEquals(2, senderTransactions.size(), "Sender account should have 2 transactions");
        assertTrue(senderTransactions.stream().anyMatch(t -> t.getAmount() == 1000.0 && t.getComment().equals("Test Load 1")),
                "First transaction should be loaded correctly");
        assertTrue(senderTransactions.stream().anyMatch(t -> t.getAmount() == 2000.0 && t.getComment().equals("Test Load 2")),
                "Second transaction should be loaded correctly");
    }


    @Test
    void testDeleteTransaction() throws SQLException {
        Transaction transaction = new Transaction(senderAccount, receiverAccount, 3000.0, "Test Delete", LocalDateTime.now());
        transactionManager.saveTransaction(transaction);

        List<Transaction> senderTransactions = transactionManager.loadTransactions(senderAccount);
        assertFalse(senderTransactions.isEmpty(), "Sender account should have at least one transaction after saving");

        Transaction savedTransaction = senderTransactions.stream()
                .filter(t -> t.getAmount() == transaction.getAmount() && t.getComment().equals(transaction.getComment()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Transaction not found after saving"));

        boolean deleted = transactionManager.deleteTransaction(savedTransaction.getTransactionID());
        assertTrue(deleted, "Transaction should be deleted");

        List<Transaction> updatedSenderTransactions = transactionManager.loadTransactions(senderAccount);
        assertTrue(updatedSenderTransactions.stream().noneMatch(t -> t.getTransactionID() == savedTransaction.getTransactionID()),
                "Deleted transaction should not exist in sender's transactions");

        List<Transaction> updatedReceiverTransactions = transactionManager.loadTransactions(receiverAccount);
        assertTrue(updatedReceiverTransactions.stream().noneMatch(t -> t.getTransactionID() == savedTransaction.getTransactionID()),
                "Deleted transaction should not exist in receiver's transactions");
    }


    @AfterAll
    void teardown() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM Transactions");
            statement.executeUpdate("DELETE FROM Accounts");
            statement.executeUpdate("DELETE FROM Users");
        }
        connection.close();
    }
}
