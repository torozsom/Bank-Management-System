package banking;

import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountManagerTest {

    private AccountManager accountManager;
    private UserManager userManager;
    private User testUser;

    @BeforeAll
    void setUp() throws SQLException {
        userManager = new UserManager();
        accountManager = new AccountManager();
        userManager.saveUser(new User("testuser@gmail.com", "password123", java.time.LocalDateTime.now()));
        testUser = userManager.loadUser("testuser@gmail.com");
    }

    @AfterAll
    void tearDown() throws SQLException {
        List<Account> accounts = accountManager.loadAccounts(testUser.getUserID());
        for (Account account : accounts)
            accountManager.deleteAccount(account);

        userManager.deleteUser(testUser.getEmail());
    }

    @Test
    void testSaveAccount() throws SQLException {
        Account account = new Account(testUser.getUserID(), 12345678, 1000.0, false);
        accountManager.saveAccount(account);

        Account loadedAccount = accountManager.loadAccount(12345678);
        assertNotNull(loadedAccount);
        assertEquals(12345678, loadedAccount.getAccountNumber());
        assertEquals(1000.0, loadedAccount.getBalance());
        assertFalse(loadedAccount.isFrozen());
    }

    @Test
    void testAccountExists() throws SQLException {
        Account account = new Account(testUser.getUserID(), 87654321, 500.0, false);
        accountManager.saveAccount(account);

        assertTrue(accountManager.accountExists(87654321));
        assertFalse(accountManager.accountExists(99999999));
    }

    @Test
    void testLoadAccount() throws SQLException {
        Account account = new Account(testUser.getUserID(), 56789012, 1500.0, true);
        accountManager.saveAccount(account);
        Account loadedAccount = accountManager.loadAccount(56789012);
        assertNotNull(loadedAccount);
        assertEquals(56789012, loadedAccount.getAccountNumber());
        assertEquals(1500.0, loadedAccount.getBalance());
        assertTrue(loadedAccount.isFrozen());
    }

    @Test
    void testLoadAccounts() throws SQLException {
        Account account1 = new Account(testUser.getUserID(), 11112222, 2000.0, false);
        Account account2 = new Account(testUser.getUserID(), 33334444, 3000.0, true);
        accountManager.saveAccount(account1);
        accountManager.saveAccount(account2);
        List<Account> accounts = accountManager.loadAccounts(testUser.getUserID());
        assertEquals(3, accounts.size());
    }

    @Test
    void testDeleteAccount() throws SQLException {
        Account account = new Account(testUser.getUserID(), 55556666, 4000.0, false);
        accountManager.saveAccount(account);
        assertTrue(accountManager.accountExists(55556666));
        accountManager.deleteAccount(account);
        assertFalse(accountManager.accountExists(55556666));
    }
}

