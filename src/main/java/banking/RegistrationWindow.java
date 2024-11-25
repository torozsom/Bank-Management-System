package banking;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Random;


public class RegistrationWindow extends JFrame {

    private final UserManager userManager;
    private final AccountManager accountManager;

    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;

    private final JButton signUpButton;
    private final JButton closeButton;

    private final JLabel emailLabel;
    private final JLabel passwordLabel;
    private final JLabel confirmPasswordLabel;

    private final Image icon;


    /**
     * Opens the registration window to
     * sing up an account for banking.
     *
     * @throws SQLException when connection is unsuccesful
     */
    public RegistrationWindow() throws SQLException {

        accountManager = new AccountManager();
        userManager = new UserManager();

        setTitle("Bank Account - Registration");

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

        emailLabel = new JLabel("Email:");
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

        passwordLabel = new JLabel("Password:");
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

        confirmPasswordLabel = new JLabel("Confirm Password:");
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

        signUpButton = new JButton("Sign Up");
        gbc.gridx = 1;
        gbc.gridy = 3;
        contentPanel.add(signUpButton, gbc);

        closeButton = new JButton("Close");
        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(closeButton, gbc);

        add(contentPanel);

        signUpButton.addActionListener(e -> {
            try {
                signUpCheck();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        closeButton.addActionListener(e -> {
            dispose();
            try {
                new LoginWindow();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        setVisible(true);
    }


    /// Validates the email address by comparing with the REGEX-es
    public boolean validEmailAddress(String email) {

        String[] emailSections = email.split("@");
        if (emailSections.length != 2)
            return false;
        String username = emailSections[0];
        String service = emailSections[1].split("\\.")[0];
        String domain = emailSections[1].split("\\.")[1];

        boolean validUsername = username.matches(UserManager.USERNAME_REGEX);
        boolean validService = service.matches(UserManager.SERVICE_REGEX);
        boolean validDomain = domain.matches(UserManager.DOMAIN_REGEX);
        boolean oneAtCharacter = emailSections.length == 2;

        return validUsername && validService && validDomain && oneAtCharacter;
    }


    /**
     * Validates the data entered by the user.
     *
     * @throws SQLException
     * @1 Checks the format of the email address
     * @2 Checks if the email address is already in use
     * @3 Checks if the password was correctly confirmed
     */
    public void signUpCheck() throws SQLException {

        String email = emailField.getText();
        email = email.trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        LocalDateTime now = LocalDateTime.now();
        User user = new User(email, password, now);

        if (!validEmailAddress(email)) {
            JOptionPane.showMessageDialog(null,
                    "Invalid email format!", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 5 || password.length() > 15) {
            JOptionPane.showMessageDialog(null,
                    "Password length should be between 5 and 15!", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(null,
                    "Passwords do not match!", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (userManager.userExists(email)) {
            JOptionPane.showMessageDialog(null,
                    "Email already in use!", "WARNING", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userID = userManager.saveUser(user);
        if (userID > 0) {
            JOptionPane.showMessageDialog(null,
                    "Succesful registration!", "Success", JOptionPane.INFORMATION_MESSAGE);
            Random rand = new Random();
            int accountNumber;

            do {
                accountNumber = rand.nextInt(10000000, 99999999);
            } while (accountManager.accountExists(accountNumber));

            Account account = new Account(userID, accountNumber, 0.0, false);
            if (accountManager.saveAccount(account)) {
                dispose();
                new LoginWindow();
            } else
                JOptionPane.showMessageDialog(null, "Registration failed!", "ERROR", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Registration failed!", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

}
