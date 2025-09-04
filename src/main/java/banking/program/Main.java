package banking.program;

import banking.ui.LoginWindow;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;


/// Main class to launch the banking application.
public class Main {
    public static void main(String[] args) {
        // Initialize modern Look and Feel before creating any UI components
        initializeLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                new LoginWindow();
            } catch (SQLException ex) {
                System.err.println("Failed to initialize FlatLaf Look and Feel: " + ex.getMessage());
                JOptionPane.showMessageDialog(null, "Failed to connect to the database: "
                        + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Initializes the modern FlatLaf Look and Feel.
     * Falls back to system default if FlatLaf initialization fails.
     */
    private static void initializeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            UIManager.put("Panel.background", Color.LIGHT_GRAY);

            // Enable modern window decorations
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");

            // Enable smooth scrolling
            System.setProperty("flatlaf.animation", "true");

            // Modern window appearance
            System.setProperty("flatlaf.useNativeWindowDecorations", "true");

        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf Look and Feel: " + ex.getMessage());
            try {
                // Fallback to system Look and Feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception fallbackEx) {
                System.err.println("Failed to set system Look and Feel: " + fallbackEx.getMessage());
            }
        }
    }

}