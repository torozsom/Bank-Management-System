package banking.program;

import banking.view.LoginWindow;

import javax.swing.*;
import java.sql.SQLException;


/// Main class to launch the banking application.
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new LoginWindow();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to connect to the database: "
                        + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}