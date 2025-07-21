package banking.ui;

import banking.service.LoginService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;


/**
 * LoginWindow class represents the login window of the banking application.
 * It allows users to enter their email and password to log in or navigate to the registration window.
 */
public class LoginWindow extends JFrame {

    private final LoginService loginService;
    private final Image icon;

    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton registerButton;
    private final JLabel emailLabel;
    private final JLabel passwordLabel;


    /// Creates and shows the login window of the app.
    public LoginWindow() throws SQLException {

        loginService = new LoginService();

        setTitle("Bank Account - Login");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        icon = Toolkit.getDefaultToolkit().getImage("icon.png");
        setIconImage(icon);


        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(Color.LIGHT_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;


        emailLabel = new JLabel("Email address:");
        emailLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        emailLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 0;
        contentPanel.add(emailLabel, gbc);


        emailField = new JTextField(20);
        Dimension textFieldSize = new Dimension(300, 30);
        emailField.setPreferredSize(textFieldSize);
        emailField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        emailField.setFont(new Font("Times New Roman", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 0;
        contentPanel.add(emailField, gbc);


        passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        passwordLabel.setForeground(Color.BLACK);
        gbc.gridx = 0;
        gbc.gridy = 1;
        contentPanel.add(passwordLabel, gbc);


        passwordField = new JPasswordField(20);
        passwordField.setPreferredSize(textFieldSize);
        passwordField.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        passwordField.setFont(new Font("Times New Roman", Font.BOLD, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        contentPanel.add(passwordField, gbc);


        loginButton = new JButton("Login");
        gbc.gridx = 1;
        gbc.gridy = 2;
        contentPanel.add(loginButton, gbc);


        registerButton = new JButton("Register");
        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(registerButton, gbc);

        add(contentPanel);

        //When the login button is clicked, the data entered will be authenticated
        //Proceed to the main window when everything is validated
        //Otherwise show warning message
        loginButton.addActionListener(_ -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            LoginService.AuthenticationResult authResult = loginService.authenticateUser(email, password);

            if (authResult.success()) {
                LoginService.NavigationResult navResult = loginService.navigateToMainWindow(email);
                if (navResult.success()) {
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, navResult.errorMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, authResult.message(), "WARNING", JOptionPane.WARNING_MESSAGE);
            }
        });

        //Proceed to the registation window when clicking the registry button
        registerButton.addActionListener(_ -> {
            LoginService.NavigationResult navResult = loginService.navigateToRegistrationWindow();
            if (navResult.success()) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, navResult.errorMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        });

        setVisible(true);
    }

}
