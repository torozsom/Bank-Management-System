package banking;

import java.sql.*;
import java.time.format.DateTimeFormatter;


/**
 * The {@code UserManager} class handles the necessary database actions
 * and SQL queries. It saves user data on successful registry and
 * authenticates it on login attempts.
 */
public class UserManager {

    public static final String USERNAME_REGEX = "^[a-zA-Z0-9._]+$";
    public static final String SERVICE_REGEX = "^[a-z]+$";
    public static final String DOMAIN_REGEX = "^[a-z]{2,}$";

    private final String dataBaseURL;
    private final Connection connection;

    Users userList;


    /**
     * Ctor that initializes the URL to the database
     * and sets up the connection.
     *
     * @throws SQLException when connection is unsuccessful
     */
    public UserManager() throws SQLException {
        dataBaseURL = "jdbc:sqlite:Banking.db";
        connection = DriverManager.getConnection(dataBaseURL);
        userList = new Users();
    }


    /// Safely closes the connection to the database
    private void close() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
    }


    /**
     * Registers a user and saves its email, password and date
     * of registry in the database.
     *
     * @param user the user to be registered
     * @return True when successfully registered a user and saved in the database
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
                userList.addUser(user);
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
        String query = "SELECT * FROM Users WHERE email = ? AND password = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String storedPassword = result.getString("password");
                return storedPassword.equals(password);
            }

            return false;
        }
    }


//TODO:public User getUser(String email) throws SQLException {}


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
