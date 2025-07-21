package banking.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Centralized database connection manager for the banking application.
 * This class provides thread-safe access to database connections with
 * proper connection lifecycle management and validation.
 */
public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:config/Banking.db";
    private static DatabaseManager instance;
    private final Object connectionLock = new Object();
    private Connection connection;


    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the database connection with proper settings.
     *
     * @throws SQLException if a database access error occurs
     */
    private DatabaseManager() throws SQLException {
        createConnection();
    }

    /**
     * Gets the singleton instance of the DatabaseManager.
     *
     * @return the DatabaseManager instance
     * @throws SQLException if a database access error occurs
     */
    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null)
            instance = new DatabaseManager();

        return instance;
    }

    /**
     * Creates a new database connection with optimized settings.
     *
     * @throws SQLException if a database access error occurs
     */
    private void createConnection() throws SQLException {
        connection = DriverManager.getConnection(DATABASE_URL);
        // Enable foreign key constraints for data integrity
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        // Set reasonable timeout
        connection.createStatement().execute("PRAGMA busy_timeout = 30000");
    }


    /**
     * Gets a valid database connection, creating a new one if necessary.
     * This method is thread-safe and validates the connection before returning it.
     *
     * @return a valid database connection
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        synchronized (connectionLock) {
            // Check if connection is valid, create new one if not
            if (connection == null || connection.isClosed() || !isConnectionValid())
                createConnection();

            return connection;
        }
    }


    /**
     * Validates the current connection by executing a simple query.
     *
     * @return true if the connection is valid, false otherwise
     */
    private boolean isConnectionValid() {
        try {
            return connection.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            return false;
        }
    }


    /**
     * Closes the database connection if it is open.
     * This method is thread-safe.
     *
     * @throws SQLException if a database access error occurs
     */
    public void closeConnection() throws SQLException {
        synchronized (connectionLock) {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        }
    }

}