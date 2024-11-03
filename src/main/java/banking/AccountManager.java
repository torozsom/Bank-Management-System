package banking;

import java.sql.*;


public class AccountManager {
    private final String dataBaseURL;
    private final Connection connection;


    public AccountManager() throws SQLException {
        dataBaseURL = "jdbc:mysql:Banking.db";
        connection = DriverManager.getConnection(dataBaseURL);
    }

    private void close() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
    }


}
