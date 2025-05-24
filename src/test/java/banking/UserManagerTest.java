package banking;

import banking.controller.UserManager;
import banking.model.User;
import banking.util.DatabaseManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


class UserManagerTest {

    private UserManager userManager;


    @BeforeEach
    void setUp() throws SQLException {
        userManager = new UserManager();
    }


    @AfterEach
    void tearDown() throws SQLException {
        userManager.deleteUser("testuser@example.com");
        userManager.deleteUser("anotheruser@example.com");
    }


    @org.junit.jupiter.api.AfterAll
    static void tearDownAll() throws SQLException {
        // Close the database connection
        DatabaseManager.getInstance().closeConnection();
    }


    @Test
    void testSaveUser() throws SQLException {
        String email = "testuser@example.com";
        String password = "password123";
        LocalDateTime now = LocalDateTime.now();
        User user = new User(email, password, now);

        int userId = userManager.saveUser(user);
        assertTrue(userId > 0, "User ID should be greater than 0 for a saved user");
        assertTrue(userManager.userExists(email), "Saved user should exist in the database");
    }


    @Test
    void testUserExists() throws SQLException {
        String email = "testuser@example.com";
        String password = "password123";
        LocalDateTime now = LocalDateTime.now();
        User user = new User(email, password, now);

        userManager.saveUser(user);

        assertTrue(userManager.userExists(email), "userExists should return true for an existing user");
        assertFalse(userManager.userExists("nonexistent@example.com"), "userExists should return false for a non-existing user");
    }


    @Test
    void testAuthenticateUser() throws SQLException {
        String email = "testuser@example.com";
        String password = "password123";
        LocalDateTime now = LocalDateTime.now();
        User user = new User(email, password, now);

        userManager.saveUser(user);

        assertTrue(userManager.authenticateUser(email, password), "Authentication should succeed with correct credentials");
        assertFalse(userManager.authenticateUser(email, "wrongpassword"), "Authentication should fail with incorrect password");
        assertFalse(userManager.authenticateUser("nonexistent@example.com", password), "Authentication should fail for non-existent email");
    }


    @Test
    void testLoadUser() throws SQLException {
        String email = "testuser@example.com";
        String password = "password123";
        LocalDateTime now = LocalDateTime.now().withNano(0);
        User user = new User(email, password, now);

        userManager.saveUser(user);

        User loadedUser = userManager.loadUser(email);
        assertNotNull(loadedUser, "loadUser should return a valid User object for existing email");
        assertEquals(email, loadedUser.getEmail(), "Loaded user's email should match the saved email");
        assertEquals(password, loadedUser.getPassword(), "Loaded user's password should match the saved password");

        LocalDateTime truncatedLoadedDate = loadedUser.getDateOfRegistry().withNano(0);
        assertEquals(now, truncatedLoadedDate, "Loaded user's date of registry should match the saved date (to seconds precision)");
    }


    @Test
    void testDeleteUser() throws SQLException {
        String email = "testuser@example.com";
        String password = "password123";
        LocalDateTime now = LocalDateTime.now();
        User user = new User(email, password, now);

        userManager.saveUser(user);

        assertTrue(userManager.userExists(email), "User should exist before deletion");
        assertTrue(userManager.deleteUser(email), "deleteUser should return true after successful deletion");
        assertFalse(userManager.userExists(email), "User should not exist after deletion");
    }

}
