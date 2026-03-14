package banking.program;

import banking.ui.LoginWindow;
import javafx.application.Application;
import javafx.stage.Stage;

import java.sql.SQLException;


/**
 * Main class to launch the banking application using JavaFX.
 */
public class Main extends Application {

    /**
     * The main method is the entry point of the application. It launches the JavaFX application.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        launch(args);
    }


    /**
     * The start method is the entry point for the JavaFX application. It initializes the login window.
     *
     * @param primaryStage The primary stage for this application, onto which the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            new LoginWindow();
        } catch (SQLException ex) {
            System.err.println("Database Error: Failed to connect to the database: " + ex.getMessage());
            System.exit(1);
        }
    }
}