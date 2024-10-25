package banking;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class RegistrationWindow extends JFrame {

    private final UserManager userManager;

    private final JTextField emailField;
    private final JPasswordField passwordField;
    private final JPasswordField confirmPasswordField;

    private final JButton signUpButton;
    private final JButton closeButton;

    private final JLabel emailLabel;
    private final JLabel passwordLabel;
    private final JLabel confirmPasswordLabel;

    private final Image icon;


    public RegistrationWindow() throws SQLException {

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


    public void signUpCheck() throws SQLException {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(null, "Passwords do not match!");
            return;
        }

        if (userManager.registerUser(email, password)) {
            JOptionPane.showMessageDialog(null, "Succesful registration!");
            dispose();
            new LoginWindow();
        } else {
            JOptionPane.showMessageDialog(null, "Registration failed!");
        }
    }

}
