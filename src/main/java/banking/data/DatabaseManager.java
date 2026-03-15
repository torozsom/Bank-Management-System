package banking.data;

import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * DatabaseManager is a singleton class responsible for managing database
 * connections to the SQLite database. It uses SQLiteDataSource to provide
 * connections and ensures that foreign key constraints are enforced.
 */
public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:config/Banking.db";

    private static DatabaseManager instance;
    private final SQLiteDataSource dataSource;


    /**
     * Initializes the SQLiteDataSource with the database URL.
     */
    private DatabaseManager() {
        // Initialize the DataSource (Factory)
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(DATABASE_URL);
    }

    /**
     * Gets the singleton instance of the DatabaseManager.
     *
     * @return the DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null)
            instance = new DatabaseManager();

        return instance;
    }


    /**
     * Retrieves a new database connection from the DataSource.
     * Ensures that foreign key constraints are enforced for SQLite.
     *
     * @return a new Connection object
     * @throws SQLException if a database access error occurs
     */
    public Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();

        // Enforce foreign keys on every new connection
        // (SQLite defaults to OFF for backward compatibility)
        try (var stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 30000;");
        }

        return conn;
    }

}