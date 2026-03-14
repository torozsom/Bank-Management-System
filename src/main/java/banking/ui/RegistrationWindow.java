package banking.ui;

import banking.service.RegistrationService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.sql.SQLException;


/**
 * The RegistrationWindow class provides a GUI for users to register
 * a new account in the banking application. It allows users to enter
 * their email, password, and confirm their password.
 * Upon successful registration, it navigates to the login window.
 */
public class RegistrationWindow extends Stage {

    private final RegistrationService registrationService;

    private final TextField emailField;
    private final PasswordField passwordField;
    private final PasswordField confirmPasswordField;


    /**
     * Opens the registration window to
     * sing up an account for banking.
     *
     * @throws SQLException when connection is unsuccessful
     */
    public RegistrationWindow() throws SQLException {

        registrationService = new RegistrationService();

        setTitle("Bank Account - Registration");
        setResizable(false);

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        Label emailLabel = new Label("Email:");
        gridPane.add(emailLabel, 0, 0);

        emailField = new TextField();
        gridPane.add(emailField, 1, 0);

        Label passwordLabel = new Label("Password:");
        gridPane.add(passwordLabel, 0, 1);

        passwordField = new PasswordField();
        gridPane.add(passwordField, 1, 1);

        Label confirmPasswordLabel = new Label("Confirm Password:");
        gridPane.add(confirmPasswordLabel, 0, 2);

        confirmPasswordField = new PasswordField();
        gridPane.add(confirmPasswordField, 1, 2);

        Button closeButton = new Button("Close");
        gridPane.add(closeButton, 0, 3);

        Button signUpButton = new Button("Sign Up");
        gridPane.add(signUpButton, 1, 3);


        // Sign up button's event handler
        signUpButton.setOnAction(_ -> {
            String email = emailField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            RegistrationService.RegistrationResult result = registrationService.registerUser(email, password, confirmPassword);

            if (result.success()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", result.message());
                RegistrationService.NavigationResult navResult = registrationService.navigateToLoginWindow();
                if (navResult.success()) {
                    close();
                } else {
                    showAlert(Alert.AlertType.ERROR, "ERROR", navResult.errorMessage());
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "WARNING", result.message());
            }
        });

        // Close button's event handler
        closeButton.setOnAction(_ -> {
            RegistrationService.NavigationResult navResult = registrationService.navigateToLoginWindow();
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
     * @param alertType the type of alert (e.g., INFORMATION, WARNING, ERROR)
     * @param title     the title of the alert dialog
     * @param message   the content message to display in the alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}