package banking.ui;

import banking.service.LoginService;
import javafx.geometry.HPos;import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;import javafx.stage.Stage;

import java.sql.SQLException;import java.util.Objects;


/**
 * LoginWindow class represents the login window of the banking application.
 * It allows users to enter their email and password to log in or navigate to the registration window.
 */
public class LoginWindow extends Stage {

    private final LoginService loginService;

    private final TextField emailField;
    private final PasswordField passwordField;


    /**
     * Constructor initializes the login window, sets up the UI components,
     * and defines the event handlers for the login and register buttons.
     *
     * @throws SQLException if there is an error initializing the LoginService or connecting to the database.
     */
    public LoginWindow() throws SQLException {

        loginService = new LoginService();

        setTitle("Bank Account - Login");
        setResizable(false);

        StackPane root = new StackPane();
        root.setPadding(new Insets(50));

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("login-card");
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setMaxWidth(400);
        gridPane.setMaxHeight(350);
        gridPane.setHgap(15);
        gridPane.setVgap(15);

        Label titleLabel = new Label("Welcome Back!");
        titleLabel.getStyleClass().add("title-label");
        GridPane.setHalignment(titleLabel, HPos.CENTER);
        gridPane.add(titleLabel, 0, 0, 2, 1);

        Label emailLabel = new Label("Email address:");
        gridPane.add(emailLabel, 0, 1);

        emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setMinWidth(300);
        gridPane.add(emailField, 0, 2, 2, 1);

        Label passwordLabel = new Label("Password:");
        gridPane.add(passwordLabel, 0, 3);

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setMinWidth(300);
        gridPane.add(passwordField, 0, 4, 2, 1);

        Button registerButton = new Button("Register");
        registerButton.getStyleClass().addAll("button", "register-button");

        registerButton.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(registerButton, 0, 5);

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().addAll("button", "login-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(loginButton, 1, 5);

        root.getChildren().add(gridPane);

        // Login button's event handler
        loginButton.setOnAction(_ -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            LoginService.AuthenticationResult authResult = loginService.authenticateUser(email, password);

            if (authResult.success()) {
                LoginService.NavigationResult navResult = loginService.navigateToMainWindow(email);
                if (navResult.success()) {
                    close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "ERROR", navResult.errorMessage());
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "WARNING", authResult.message());
            }
        });

        // Registry button's event handler
        registerButton.setOnAction(_ -> {
            LoginService.NavigationResult navResult = loginService.navigateToRegistrationWindow();
            if (navResult.success()) {
                close();
            } else {
                showAlert(Alert.AlertType.ERROR, "ERROR", navResult.errorMessage());
            }
        });

        // Create and set the scene
        Scene scene = new Scene(root, 800, 500);

        // Load CSS
        try {
            String cssPath = Objects.requireNonNull(getClass().getResource("/login-styles.css")).toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.err.println("Warning: style.css not found in resources folder!");
        }

        setScene(scene);
        show();
    }


    /**
     * Utility method to show an alert dialog with the specified type, title, and message.
     *
     * @param alertType the type of the alert (e.g., ERROR, WARNING, INFORMATION).
     * @param title     the title of the alert dialog.
     * @param message   the content message of the alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}