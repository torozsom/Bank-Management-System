package banking.ui;

import banking.service.RegistrationService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;


/**
 * The RegistrationWindow class provides a GUI for users to register
 * a new account in the banking application. It allows users to enter
 * their email, password, and confirm their password.
 * Upon successful registration, it navigates to the login window.
 */
public class RegistrationWindow extends JFrame {

    private final RegistrationService registrationService;

    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;


    /**
     * Opens the registration window to
     * sing up an account for banking.
     *
     * @throws SQLException when connection is unsuccessful
     */
    public RegistrationWindow() throws SQLException {

        registrationService = new RegistrationService();

        setTitle("Bank Account - Registration");

        setSize(800, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Image icon = Toolkit.getDefaultToolkit().getImage("icon.png");
        setIconImage(icon);

        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        emailLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(emailLabel, gbc);

        emailField = new JTextField(20);
        emailField.setPreferredSize(new Dimension(300, 30));
        emailField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        emailField.setFont(new Font("Times New Roman", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        contentPanel.add(emailField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        passwordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(new Dimension(300, 30));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        passwordField.setFont(new Font("Times New Roman", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        contentPanel.add(passwordField, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        confirmPasswordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(confirmPasswordLabel, gbc);

        confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setPreferredSize(new Dimension(300, 30));
        confirmPasswordField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        confirmPasswordField.setFont(new Font("Times New Roman", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        contentPanel.add(confirmPasswordField, gbc);

        JButton signUpButton = new JButton("Sign Up");
        gbc.gridx = 1;
        gbc.gridy = 3;
        contentPanel.add(signUpButton, gbc);

        JButton closeButton = new JButton("Close");
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(closeButton, gbc);

        add(contentPanel);

        signUpButton.addActionListener(_ -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            RegistrationService.RegistrationResult result = registrationService.registerUser(email, password, confirmPassword);

            if (result.success()) {
                JOptionPane.showMessageDialog(null, result.message(), "Success", JOptionPane.INFORMATION_MESSAGE);
                RegistrationService.NavigationResult navResult = registrationService.navigateToLoginWindow();
                if (navResult.success()) {
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, navResult.errorMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, result.message(), "WARNING", JOptionPane.WARNING_MESSAGE);
            }
        });

        closeButton.addActionListener(_ -> {
            RegistrationService.NavigationResult navResult = registrationService.navigateToLoginWindow();
            if (navResult.success()) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, navResult.errorMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }


}
