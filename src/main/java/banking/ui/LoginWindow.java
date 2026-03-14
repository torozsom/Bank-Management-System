package banking.ui;

import banking.service.LoginService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;


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

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        Label emailLabel = new Label("Email address:");
        gridPane.add(emailLabel, 0, 0);

        emailField = new TextField();
        gridPane.add(emailField, 1, 0);

        Label passwordLabel = new Label("Password:");
        gridPane.add(passwordLabel, 0, 1);

        passwordField = new PasswordField();
        gridPane.add(passwordField, 1, 1);

        Button registerButton = new Button("Register");
        gridPane.add(registerButton, 0, 2);

        Button loginButton = new Button("Login");
        gridPane.add(loginButton, 1, 2);

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

        // Set the scene and show the window
        Scene scene = new Scene(gridPane, 800, 500);
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