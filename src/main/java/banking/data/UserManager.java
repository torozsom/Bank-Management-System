package banking.data;

import banking.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


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


    /**
     * Constructor that initializes the connection to the database.
     *
     * @throws SQLException when connection is unsuccessful
     */
    public UserManager() throws SQLException {
        connection = DatabaseManager.getInstance().getConnection();
    }


    /**
     * Registers a user and saves their email, password and date
     * of registry in the database.
     *
     * @param user the user to be registered
     * @return The user ID that is associated with the saved user in the database
     * @throws SQLException when connection is unsuccessful
     */
    public int saveUser(User user) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateOfRegistry = user.getDateOfRegistry().format(formatter);

        String query = "INSERT INTO Users (email, password, datetime) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPassword());
            statement.setString(3, dateOfRegistry);
            statement.executeUpdate();

            ResultSet result = statement.getGeneratedKeys();
            if (result.next()) {
                int userID = result.getInt(1);
                return userID;
            }
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
                return result.next() && password.equals(result.getString("password"));
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
     * Searches for a user based on the given email in the
     * database and returns it as an Optional.
     *
     * @param email the address to be searched for
     * @return an Optional containing the User if found, or empty if not found
     * @throws SQLException             when connection is unsuccessful
     * @throws IllegalArgumentException if email is null or empty
     */
    public java.util.Optional<User> findUserByEmail(String email) throws SQLException {
        return java.util.Optional.ofNullable(loadUser(email));
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
