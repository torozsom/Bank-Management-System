package banking.service;

import banking.data.UserManager;
import banking.ui.MainWindow;
import banking.ui.RegistrationWindow;

import java.sql.SQLException;


/**
 * LoginService handles the business logic for user authentication and navigation.
 * This class separates the functional operations from the LoginWindow View.
 */
public class LoginService {

    private final UserManager userManager;

    /**
     * Creates a new LoginService instance and initializes the UserManager.
     * @throws SQLException if a database error occurs during initialization
     */
    public LoginService() throws SQLException {
        userManager = new UserManager();
    }


    /**
     * Authenticates a user with the provided email and password.
     *
     * @param email    the user's email address
     * @param password the user's password
     * @return AuthenticationResult containing success status and message
     */
    public AuthenticationResult authenticateUser(String email, String password) {
        try {
            if (email == null || email.trim().isEmpty())
                return new AuthenticationResult(false, "Email address cannot be empty.");

            if (password == null || password.trim().isEmpty())
                return new AuthenticationResult(false, "Password cannot be empty.");

            boolean isAuthenticated = userManager.authenticateUser(email, password);

            if (isAuthenticated)
                return new AuthenticationResult(true, "Authentication successful.");
            else
                return new AuthenticationResult(false, "Invalid email or password.");

        } catch (SQLException ex) {
            return new AuthenticationResult(false, "Database error occurred during authentication: " + ex.getMessage());
        }
    }


    /**
     * Handles the navigation to the main window after successful authentication.
     *
     * @param email the authenticated user's email
     * @return NavigationResult containing success status and any error message
     */
    public NavigationResult navigateToMainWindow(String email) {
        try {
            new MainWindow(email);
            return new NavigationResult(true, null);
        } catch (SQLException ex) {
            return new NavigationResult(false, "Error opening main window: " + ex.getMessage());
        }
    }


    /**
     * Handles the navigation to the registration window.
     *
     * @return NavigationResult containing success status and any error message
     */
    public NavigationResult navigateToRegistrationWindow() {
        try {
            new RegistrationWindow();
            return new NavigationResult(true, null);
        } catch (SQLException ex) {
            return new NavigationResult(false, "Error opening registration window: " + ex.getMessage());
        }
    }


    /**
     * Result class for authentication operations.
     */
    public record AuthenticationResult(boolean success, String message) {
    }


    /**
     * Result class for navigation operations.
     */
    public record NavigationResult(boolean success, String errorMessage) {
    }

}