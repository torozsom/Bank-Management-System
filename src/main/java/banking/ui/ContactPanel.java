package banking.ui;

import banking.model.Contact;
import banking.service.ContactService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


/**
 * The ContactPanel class represents the user interface for managing contacts in the banking application.
 * It allows users to add new contacts, view existing contacts, and search for contacts by name or account number.
 */
public class ContactPanel extends VBox {

    private final ContactService contactService;
    private final ObservableList<String> contactListModel;

    private TextField searchField;
    private TextField nameField;
    private TextField accountField;


    /**
     * Constructs a new ContactPanel and initializes the user interface components.
     */
    public ContactPanel() {
        contactService = new ContactService();
        contactListModel = FXCollections.observableArrayList();

        setSpacing(20);
        setAlignment(Pos.CENTER);
        setPadding(new Insets(20));

        getChildren().add(createTitlePanel());
        getChildren().add(createInputPanel());
        getChildren().add(createContactListPanel());

        loadContactsToList();
    }


    /**
     * Creates the title panel for the contact manager.
     *
     * @return An HBox containing the title label.
     */
    private HBox createTitlePanel() {
        HBox titlePanel = new HBox();
        titlePanel.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Contact Manager");
        titleLabel.getStyleClass().add("section-title");
        titlePanel.getChildren().add(titleLabel);

        return titlePanel;
    }


    /**
     * Creates the input panel for adding new contacts, including
     * fields for name and account number, and a save button.
     *
     * @return A GridPane containing the input fields and save button.
     */
    private GridPane createInputPanel() {
        GridPane inputPanel = new GridPane();
        inputPanel.setAlignment(Pos.CENTER);
        inputPanel.setHgap(10);
        inputPanel.setVgap(10);

        Label nameLabel = new Label("Name / Company:");
        inputPanel.add(nameLabel, 0, 0);

        nameField = new TextField();
        nameField.setPromptText("Enter name");
        inputPanel.add(nameField, 1, 0);

        Label accountLabel = new Label("Account number:");
        inputPanel.add(accountLabel, 0, 1);

        accountField = new TextField();
        accountField.setPromptText("Enter account");
        inputPanel.add(accountField, 1, 1);

        Button saveButton = new Button("Save");
        saveButton.getStyleClass().add("btn-green");
        saveButton.setOnAction(_ -> saveContact());
        inputPanel.add(saveButton, 1, 2);

        return inputPanel;
    }


    /**
     * Creates the contact list panel, which includes a search field and a list view to display contacts.
     *
     * @return A VBox containing the search field and contact list view.
     */
    private VBox createContactListPanel() {
        VBox contactListPanel = new VBox();
        contactListPanel.setAlignment(Pos.CENTER);
        contactListPanel.setSpacing(10);

        searchField = new TextField();
        searchField.setPromptText("Search by name or account number...");
        searchField.setMaxWidth(Double.MAX_VALUE);

        searchField.focusedProperty().addListener((_, _, newValue) -> {
            if (newValue)
                searchField.setText("");
            else
                loadContactsToList();
        });

        searchField.textProperty().addListener((_, _, newValue) -> filterContactList(newValue));
        ListView<String> contactList = new ListView<>(contactListModel);
        contactListPanel.getChildren().addAll(searchField, contactList);

        return contactListPanel;
    }

    /**
     * Saves a new contact using the input from the name and account number fields.
     * Validates the input and displays appropriate success or error messages.
     */
    private void saveContact() {
        String name = nameField.getText().trim();
        String accountNumberText = accountField.getText().trim();

        if (name.isEmpty() || accountNumberText.isEmpty()) {
            showErrorMessage("Please fill in all fields.");
            return;
        }

        try {
            ContactService.ContactResult result = contactService.saveContact(name, accountNumberText);

            if (result.success()) {
                showSuccessMessage();
                nameField.setText("");
                accountField.setText("");
                loadContactsToList();
            } else {
                showErrorMessage(result.message());
            }
        } catch (NumberFormatException ex) {
            showErrorMessage("Account number must be a valid integer.");
        }
    }


    /**
     * Loads the contacts from the contact service and updates the contact list model.
     * Displays an error message if the contacts cannot be loaded.
     */
    public void loadContactsToList() {
        ContactService.ContactListResult result = contactService.loadContacts();
        contactListModel.clear();

        if (result.success()) {
            for (Contact contact : result.contacts()) {
                contactListModel.add(contact.name() + " - " + contact.accountNumber());
            }
        } else {
            showErrorMessage(result.errorMessage());
        }
    }


    /**
     * Filters the contact list based on the provided query, which can be a name or account number.
     * Updates the contact list model with the filtered results and displays an error message if the filtering fails.
     *
     * @param query The search query to filter contacts by name or account number.
     */
    private void filterContactList(String query) {
        ContactService.ContactListResult result = contactService.filterContacts(query);
        contactListModel.clear();

        if (result.success()) {
            for (Contact contact : result.contacts()) {
                contactListModel.add(contact.name() + " - " + contact.accountNumber());
            }
        } else {
            showErrorMessage(result.errorMessage());
        }
    }


    /**
     * Displays an error message in an alert dialog.
     *
     * @param message The error message to be displayed.
     */
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Displays a success message in an alert dialog indicating that a contact was saved successfully.
     */
    private void showSuccessMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Contact saved successfully.");
        alert.showAndWait();
    }
}