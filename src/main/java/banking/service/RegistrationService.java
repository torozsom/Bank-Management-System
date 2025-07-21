package banking.service;

import banking.data.AccountManager;
import banking.data.DatabaseManager;
import banking.data.UserManager;
import banking.model.Account;
import banking.model.User;
import banking.ui.LoginWindow;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;


/**
 * RegistrationViewModel handles the business logic for user registration and account creation.
 * This class separates the functional operations from the RegistrationWindow View.
 */
public class RegistrationService {

    private final UserManager userManager;
    private final AccountManager accountManager;


    /// Constructor initializes the UserManager and AccountManager.
    public RegistrationService() throws SQLException {
        this.userManager = new UserManager();
        this.accountManager = new AccountManager();
    }


    /**
     * Validates an email address format using regex patterns.
     *
     * @param email the email address to validate
     * @return true if the email format is valid, false otherwise
     */
    public boolean isValidEmailAddress(String email) {
        if (email == null || email.trim().isEmpty())
            return false;

        String[] emailSections = email.split("@");

        if (emailSections.length != 2)
            return false;

        String username = emailSections[0];
        String[] domainParts = emailSections[1].split("\\.");

        if (domainParts.length != 2)
            return false;

        String service = domainParts[0];
        String domain = domainParts[1];

        boolean validUsername = username.matches(UserManager.USERNAME_REGEX);
        boolean validService = service.matches(UserManager.SERVICE_REGEX);
        boolean validDomain = domain.matches(UserManager.DOMAIN_REGEX);

        return validUsername && validService && validDomain;
    }


    /**
     * Validates password requirements.
     *
     * @param password the password to validate
     * @return ValidationResult containing success status and message
     */
    public ValidationResult validatePassword(String password) {
        if (password == null || password.trim().isEmpty())
            return new ValidationResult(false, "Password cannot be empty.");

        if (password.length() < 5 || password.length() > 15)
            return new ValidationResult(false, "Password length should be between 5 and 15!");

        return new ValidationResult(true, "Password is valid.");
    }


    /**
     * Validates that passwords match.
     *
     * @param password        the original password
     * @param confirmPassword the confirmation password
     * @return ValidationResult containing success status and message
     */
    public ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword))
            return new ValidationResult(false, "Passwords do not match!");

        return new ValidationResult(true, "Passwords match.");
    }


    /**
     * Registers a new user with the provided information.
     * Uses database transactions to ensure atomicity between user and account creation.
     *
     * @param email           the user's email address
     * @param password        the user's password
     * @param confirmPassword the password confirmation
     * @return RegistrationResult containing success status and message
     */
    public RegistrationResult registerUser(String email, String password, String confirmPassword) {
        Connection connection = null;

        try {
            // Trim email
            email = email.trim();

            // Validate email format
            if (!isValidEmailAddress(email))
                return new RegistrationResult(false, "Invalid email format!");

            // Validate password
            ValidationResult passwordValidation = validatePassword(password);
            if (!passwordValidation.success())
                return new RegistrationResult(false, passwordValidation.message());

            // Validate password match
            ValidationResult passwordMatchValidation = validatePasswordMatch(password, confirmPassword);
            if (!passwordMatchValidation.success())
                return new RegistrationResult(false, passwordMatchValidation.message());

            // Check if user already exists
            if (userManager.userExists(email))
                return new RegistrationResult(false, "Email already in use!");

            // Get database connection and start transaction
            connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);

            try {
                // Create and save user
                LocalDateTime now = LocalDateTime.now();
                User user = new User(email, password, now);
                int userID = userManager.saveUser(user);

                if (userID <= 0) {
                    connection.rollback();
                    return new RegistrationResult(false, "User creation failed!");
                }

                // Generate unique account number
                Random rand = new Random();
                int accountNumber;
                do {
                    accountNumber = rand.nextInt(10000000, 99999999);
                } while (accountManager.accountExists(accountNumber));

                // Create and save account
                Account account = new Account(userID, accountNumber, 0.0, false);
                if (!accountManager.saveAccount(account)) {
                    connection.rollback();
                    return new RegistrationResult(false, "Account creation failed!");
                }

                // Both operations successful, commit transaction
                connection.commit();
                return new RegistrationResult(true, "Successful registration!");

            } catch (SQLException transactionEx) {
                // Rollback transaction on any error
                connection.rollback();
                return new RegistrationResult(false, "Registration failed: " + transactionEx.getMessage());
            }

        } catch (SQLException ex) {
            return new RegistrationResult(false, "Database error occurred during registration: " + ex.getMessage());
        } finally {
            // Restore auto-commit mode
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    // Log error but don't fail the operation
                    System.err.println("Warning: Failed to restore auto-commit mode: " + ex.getMessage());
                }
            }
        }
    }


    /**
     * Handles navigation back to the login window.
     *
     * @return NavigationResult containing success status and any error message
     */
    public NavigationResult navigateToLoginWindow() {
        try {
            new LoginWindow();
            return new NavigationResult(true, null);
        } catch (SQLException ex) {
            return new NavigationResult(false, "Error opening login window: " + ex.getMessage());
        }
    }


    /// Result class for validation operations.
    public record ValidationResult(boolean success, String message) { }

    /// Result class for registration operations.
    public record RegistrationResult(boolean success, String message) { }

    /// Result class for navigation operations.
    public record NavigationResult(boolean success, String errorMessage) { }

}