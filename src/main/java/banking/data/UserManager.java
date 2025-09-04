package banking.data;

import banking.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;


/**
 * UserManager is responsible for managing user-related operations
 * such as saving, loading, authenticating, and deleting users in the database.
 * It also provides validation for usernames, services, and domains.
 */
public class UserManager {

    public static final String USERNAME_REGEX = "^[a-zA-Z0-9._]+$";
    public static final String SERVICE_REGEX = "^[a-z]+$";
    public static final String DOMAIN_REGEX = "^[a-z]{2,}$";

    private final Connection connection;
    private final SecureRandom secureRandom;


    /**
     * Constructor that initializes the connection to the database.
     *
     * @throws SQLException when connection is unsuccessful
     */
    public UserManager() throws SQLException {
        connection = DatabaseManager.getInstance().getConnection();
        secureRandom = new SecureRandom();
    }


    /**
     * Generates a random salt for password hashing.
     *
     * @return a Base64 encoded salt string
     */
    private String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }


    /**
     * Hashes a password with the given salt using SHA-256.
     *
     * @param password the plain text password
     * @param salt     the salt to use for hashing
     * @return the hashed password as a Base64 encoded string
     * @throws RuntimeException if SHA-256 algorithm is not available
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }


    /**
     * Creates a combined salt:hash string for storage.
     *
     * @param password the plain text password to hash
     * @return a string in format "salt:hash"
     */
    private String createPasswordHash(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return salt + ":" + hash;
    }


    /**
     * Verifies a password against a stored salt:hash combination.
     *
     * @param password           the plain text password to verify
     * @param storedPasswordHash the stored password in format "salt:hash"
     * @return true if the password matches, false otherwise
     */
    private boolean verifyPassword(String password, String storedPasswordHash) {
        if (storedPasswordHash == null || !storedPasswordHash.contains(":"))
            return password.equals(storedPasswordHash);  // Handle legacy plain text passwords for backward compatibility

        String[] parts = storedPasswordHash.split(":", 2);

        if (parts.length != 2)
            return false;

        String salt = parts[0];
        String storedHash = parts[1];
        String hashedInput = hashPassword(password, salt);
        return hashedInput.equals(storedHash);
    }


    /**
     * Registers a user and saves their email, hashed password and date
     * of registry in the database.
     *
     * @param user the user to be registered
     * @return The user ID that is associated with the saved user in the database
     * @throws SQLException when connection is unsuccessful
     */
    public int saveUser(User user) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateOfRegistry = user.getDateOfRegistry().format(formatter);

        // Hash the password with salt
        String hashedPassword = createPasswordHash(user.getPassword());

        String query = "INSERT INTO Users (email, password, datetime) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, hashedPassword);
            statement.setString(3, dateOfRegistry);
            statement.executeUpdate();

            ResultSet result = statement.getGeneratedKeys();
            if (result.next())
                return result.getInt(1);
            return -1;
        }
    }


    /**
     * Checks if a user already exists in the database.
     *
     * @param email the address that is being searched for
     * @return True if the email address is found in the database, false otherwise.
     * @throws SQLException when connection is unsuccessful
     */
    public boolean userExists(String email) throws SQLException {
        String query = "SELECT * FROM Users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();
            return result.next();
        }
    }


    /**
     * Checks if the given email and password belong to the same user.
     * Supports both hashed passwords and legacy plain text passwords.
     *
     * @param email    the email to be found
     * @param password the password to be checked
     * @return True if the email and password are in the same row in the db, false otherwise.
     * @throws SQLException when connection is unsuccessful
     */
    public boolean authenticateUser(String email, String password) throws SQLException {
        if (email == null || email.isEmpty() || password == null || password.isEmpty())
            return false;

        String query = "SELECT password FROM Users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String storedPassword = result.getString("password");
                    return verifyPassword(password, storedPassword);
                }
                return false;
            }
        }
    }


    /**
     * Searches for a user based on the given email in the
     * database and returns it with all its data as an object.
     *
     * @param email the address to be searched for
     * @return a User object with the given email, or null if not found
     * @throws SQLException             when connection is unsuccessful
     * @throws IllegalArgumentException if email is null or empty
     */
    public User loadUser(String email) throws SQLException {
        if (email == null || email.isEmpty())
            throw new IllegalArgumentException("Email cannot be null or empty");

        String query = "SELECT * FROM Users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int userID = result.getInt("user_id");
                    String password = result.getString("password");
                    String date = result.getString("datetime");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

                    return new User(userID, email, password, localDateTime);
                }
                return null;
            }
        }
    }


    /**
     * Deletes a user from the database
     *
     * @param email the email of the user to be deleted
     * @return True when deletion is successful, false otherwise.
     * @throws SQLException when connection is unsuccessful
     */
    public boolean deleteUser(String email) throws SQLException {
        String query = "DELETE FROM Users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.executeUpdate();
            return true;
        }
    }

}
