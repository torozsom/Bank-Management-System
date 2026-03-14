package banking.ui;

import banking.service.RegistrationService;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Objects;


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
        this.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
        setResizable(false);

        StackPane root = new StackPane();
        root.setPadding(new Insets(50));

        GridPane gridPane = new GridPane();
        gridPane.getStyleClass().add("register-card");
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setMaxWidth(450);
        gridPane.setMaxHeight(400);
        gridPane.setHgap(15);
        gridPane.setVgap(15);

        Label titleLabel = new Label("Create Account");
        titleLabel.getStyleClass().add("title-label");
        GridPane.setHalignment(titleLabel, HPos.CENTER);
        gridPane.add(titleLabel, 0, 0, 2, 1);

        Label emailLabel = new Label("Email:");
        gridPane.add(emailLabel, 0, 1);

        emailField = new TextField();
        emailField.setMinWidth(250);
        emailField.setPromptText("Enter your email");
        gridPane.add(emailField, 1, 1);

        Label passwordLabel = new Label("Password:");
        gridPane.add(passwordLabel, 0, 2);

        passwordField = new PasswordField();
        passwordField.setMinWidth(250);
        passwordField.setPromptText("Create a password");
        gridPane.add(passwordField, 1, 2);

        Label confirmPasswordLabel = new Label("Confirm Pwd:");
        gridPane.add(confirmPasswordLabel, 0, 3);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setMinWidth(250);
        confirmPasswordField.setPromptText("Confirm your password");
        gridPane.add(confirmPasswordField, 1, 3);

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().addAll("button", "close-button");
        closeButton.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(closeButton, 0, 4);

        Button signUpButton = new Button("Sign Up");
        signUpButton.getStyleClass().addAll("button", "signup-button");
        signUpButton.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(signUpButton, 1, 4);

        root.getChildren().add(gridPane);


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
        Scene scene = new Scene(root, 800, 500);

        // Load CSS
        try {
            String cssPath = Objects.requireNonNull(getClass().getResource("/register-styles.css")).toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.err.println("Warning: register-styles.css not found in resources folder!");
        }

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