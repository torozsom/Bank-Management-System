package banking.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Centralized database connection manager for the banking application.
 * This class provides a single point of access to the database connection,
 * eliminating duplicate connection code across manager classes.
 */
public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:config/Banking.db";
    private static DatabaseManager instance;
    private Connection connection;


    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the database connection.
     *
     * @throws SQLException if a database access error occurs
     */
    private DatabaseManager() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_URL);
    }


    /**
     * Gets the singleton instance of the DatabaseManager.
     *
     * @return the DatabaseManager instance
     * @throws SQLException if a database access error occurs
     */
    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed())
            instance = new DatabaseManager();

        return instance;
    }


    /**
     * Gets the database connection.
     *
     * @return the database connection
     */
    public Connection getConnection() {
        return connection;
    }


    /**
     * Closes the database connection if it is open.
     *
     * @throws SQLException if a database access error occurs
     */
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }

}