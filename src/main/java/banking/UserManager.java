package banking;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class UserManager {

    private final String dataBaseURL;
    private Connection connection;


    public UserManager() throws SQLException {
        dataBaseURL = "jdbc:sqlite:Accounts.db";
        connect();
    }


    private void connect() throws SQLException {
        connection = DriverManager.getConnection(dataBaseURL);
    }


    private void close() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
    }


    public boolean registerUser(String email, String password) throws SQLException {

        if (userExists(email))
            return false;

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String dateOfRegistry = now.format(formatter);

        String query = "INSERT INTO Users (email, password, datetime) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);
            statement.setString(3, dateOfRegistry);
            statement.executeUpdate();
            return true;
        }
    }


    private boolean userExists(String email) throws SQLException {
        String query = "SELECT * FROM Users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();
            return (result.next());
        }
    }


    public boolean authenticateUser(String email, String password) throws SQLException {
        String query = "SELECT * FROM Users WHERE email = ? AND password = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet result = statement.executeQuery();

            if (result.next()) {
                String storedPassword = result.getString("password");
                close();
                return storedPassword.equals(password);
            }

            return false;
        }
    }


    public boolean deleteUser(String email) throws SQLException {
        String query = "DELETE FROM Users WHERE email = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.executeUpdate();
            return true;
        }
    }
}
