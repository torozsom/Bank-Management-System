package banking.ui;

import banking.data.DatabaseManager;
import banking.model.Account;
import banking.model.Transaction;
import banking.model.User;
import banking.service.MainService;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


/**
 * MainWindow class represents the main user interface of the banking application.
 * It allows users to manage their accounts, perform transactions,
 * and view their transaction history.
 */
public class MainWindow extends Stage {

    private final MainService mainService;
    private final User currentUser;
    private Account currentAccount;

    private ComboBox<Integer> accountSelector;
    private Label balanceLabel;
    private Button depositButton;
    private Button withdrawButton;
    private Button transferButton;
    private Button freezeButton;
    private Button unfreezeButton;
    private TableView<Transaction> transactionsTable;


    /**
     * Constructs the main window for the banking application, initializing
     * the user interface and loading the user's accounts and transactions.
     *
     * @param email The email of the currently logged-in user, used to load their accounts and transactions.
     * @throws SQLException if there is an error loading the user's accounts or transactions from the database.
     */
    public MainWindow(String email) throws SQLException {
        mainService = new MainService(email);
        currentUser = mainService.getCurrentUser();

        MainService.AccountListResult accountsResult = mainService.getUserAccounts();

        if (accountsResult.success() && !accountsResult.accounts().isEmpty()) {
            List<Account> accounts = accountsResult.accounts();
            currentUser.addAllAccounts(accounts);
            currentAccount = accounts.getFirst();
            mainService.setSelectedAccount(currentAccount);
        } else {
            throw new SQLException("Failed to load user accounts: " +
                    (accountsResult.errorMessage() != null ? accountsResult.errorMessage() : "No accounts found"));
        }

        setTitle(email);
        this.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon.png"))));
        setResizable(true);
        setMinWidth(1100);
        setMinHeight(700);

        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());

        VBox dashboardContent = new VBox(20);
        dashboardContent.setPadding(new Insets(25));

        dashboardContent.getChildren().add(createSummaryCard());

        HBox middleRow = new HBox(20);
        VBox actionsCard = createAccountActionsCard();
        VBox contactsCard = createContactManagerCard();

        HBox.setHgrow(actionsCard, Priority.ALWAYS);
        HBox.setHgrow(contactsCard, Priority.ALWAYS);
        middleRow.getChildren().addAll(actionsCard, contactsCard);

        dashboardContent.getChildren().add(middleRow);
        dashboardContent.getChildren().add(createTransactionsCard());

        ScrollPane scrollPane = new ScrollPane(dashboardContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #f0f2f5;");

        root.setCenter(scrollPane);
        Scene scene = new Scene(root, 1100, 800);

        try {
            String cssPath = Objects.requireNonNull(getClass().getResource("/main-styles.css")).toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.err.println("Warning: main-styles.css not found!");
        }

        setScene(scene);
        refreshPage();
        show();
    }


    /**
     * Creates the menu bar for the main window, including options for logging out and exiting the application.
     *
     * @return A MenuBar object containing the menu items for the main window.
     */
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem logOutItem = new MenuItem("Log Out");
        logOutItem.setOnAction(_ -> {
            close();
            try {
                new LoginWindow();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(_ -> {
            try {
                DatabaseManager.getInstance().closeConnection();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        });

        fileMenu.getItems().addAll(logOutItem, new SeparatorMenuItem(), exitItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }


    /**
     * Creates the account summary card, which displays the user's current
     * account balance and allows them to select different accounts.
     *
     * @return A VBox containing the account summary information and controls.
     */
    private VBox createSummaryCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Account Summary");
        title.getStyleClass().add("section-title");

        HBox topControls = new HBox(15);
        topControls.setAlignment(Pos.CENTER);

        Label chooseLabel = new Label("Selected Account:");
        accountSelector = new ComboBox<>();
        accountSelector.setStyle("-fx-font-size: 14px; -fx-padding: 2;");
        updateAccountSelectorDropdown();

        // Account selector's event handler
        accountSelector.setOnAction(_ -> {
            Integer selectedAccountNumber = accountSelector.getValue();
            if (selectedAccountNumber == null) return;
            for (Account account : currentUser.getAccounts()) {
                if (account.getAccountNumber() == selectedAccountNumber) {
                    currentAccount = account;
                    mainService.setSelectedAccount(currentAccount);
                    refreshPage();
                    break;
                }
            }
            refreshPage();
        });

        Button refreshButton = new Button("↻ Refresh");
        refreshButton.getStyleClass().add("btn-blue");
        refreshButton.setOnAction(_ -> handleRefreshAccounts());

        topControls.getChildren().addAll(chooseLabel, accountSelector, refreshButton);

        balanceLabel = new Label();
        balanceLabel.getStyleClass().add("balance-label");

        card.getChildren().addAll(title, topControls, new Separator(), balanceLabel);
        return card;
    }


    /**
     * Creates the account actions card, which provides quick access to common account operations
     * such as depositing, withdrawing, transferring funds, and managing account status.
     *
     * @return A VBox containing the controls for performing account actions.
     */
    private VBox createAccountActionsCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("Quick Actions");
        title.getStyleClass().add("section-title");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);

        // Deposit / Withdraw
        TextField depositField = new TextField();
        depositField.setPromptText("Amount to deposit");
        depositButton = new Button("Deposit");
        depositButton.getStyleClass().add("btn-green");
        depositButton.setOnAction(_ -> handleDeposit(depositField));

        TextField withdrawField = new TextField();
        withdrawField.setPromptText("Amount to withdraw");
        withdrawButton = new Button("Withdraw");
        withdrawButton.getStyleClass().add("btn-blue");
        withdrawButton.setOnAction(_ -> handleWithdraw(withdrawField));

        grid.add(new Label("Deposit:"), 0, 0);
        grid.add(depositField, 1, 0);
        grid.add(depositButton, 2, 0);

        grid.add(new Label("Withdraw:"), 0, 1);
        grid.add(withdrawField, 1, 1);
        grid.add(withdrawButton, 2, 1);

        grid.add(new Separator(), 0, 2, 3, 1);

        // Transfer
        TextField transferAccField = new TextField();
        transferAccField.setPromptText("Target Account");
        TextField transferAmtField = new TextField();
        transferAmtField.setPromptText("Amount");
        TextField transferComField = new TextField();
        transferComField.setPromptText("Comment (optional)");

        transferButton = new Button("Transfer");
        transferButton.getStyleClass().add("btn-blue");
        transferButton.setOnAction(_ -> handleTransfer(transferAccField, transferAmtField, transferComField));

        grid.add(new Label("Transfer to:"), 0, 3);
        grid.add(transferAccField, 1, 3);
        grid.add(new Label("Amount:"), 0, 4);
        grid.add(transferAmtField, 1, 4);
        grid.add(new Label("Comment:"), 0, 5);
        grid.add(transferComField, 1, 5);
        grid.add(transferButton, 2, 5);

        grid.add(new Separator(), 0, 6, 3, 1);

        // Account Management
        HBox managementBox = new HBox(10);
        managementBox.setAlignment(Pos.CENTER);

        Button openButton = new Button("Open New Account");
        openButton.setOnAction(_ -> handleOpenAccount());
        openButton.getStyleClass().add("btn-green");

        freezeButton = new Button("Freeze");
        freezeButton.getStyleClass().add("btn-red");
        freezeButton.setOnAction(_ -> handleFreezeAccount());

        unfreezeButton = new Button("Unfreeze");
        unfreezeButton.getStyleClass().add("btn-blue");
        unfreezeButton.setOnAction(_ -> handleUnfreezeAccount());

        Button closeButton = new Button("Close Account");
        closeButton.getStyleClass().add("btn-red");
        closeButton.setOnAction(_ -> handleCloseAccount());

        managementBox.getChildren().addAll(openButton, freezeButton, unfreezeButton, closeButton);
        grid.add(managementBox, 0, 7, 3, 1);

        card.getChildren().addAll(title, grid);
        return card;
    }


    /**
     * Creates the contact manager card, which allows users to manage their contacts for easier transfers.
     *
     * @return A VBox containing the contact management interface.
     */
    private VBox createContactManagerCard() {
        ContactPanel contactPanel = new ContactPanel();
        contactPanel.getStyleClass().add("card");
        return contactPanel;
    }


    /**
     * Creates the transactions card, which displays the user's transaction history in a table format.
     *
     * @return A VBox containing the transaction history table.
     */
    private VBox createTransactionsCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);

        Label title = new Label("Transaction History");
        title.getStyleClass().add("section-title");

        transactionsTable = new TableView<>();
        transactionsTable.setPrefHeight(250);

        TableColumn<Transaction, Integer> senderCol = new TableColumn<>("Sender");
        senderCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().sender().getAccountNumber()).asObject()
        );

        TableColumn<Transaction, Integer> receiverCol = new TableColumn<>("Receiver");
        receiverCol.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().receiver().getAccountNumber()).asObject()
        );

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount (Ft)");
        amountCol.setCellValueFactory(data ->
                new SimpleDoubleProperty(data.getValue().amount()).asObject()
        );

        TableColumn<Transaction, String> commentCol = new TableColumn<>("Comment");
        commentCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().comment())
        );

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        );

        transactionsTable.getColumns().addAll(senderCol, receiverCol, amountCol, commentCol, dateCol);
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        card.getChildren().addAll(title, transactionsTable);
        return card;
    }


    /**
     * Updates the account selector dropdown with the current user's accounts and selects the current account if available.
     * This method is called after refreshing the accounts to ensure the dropdown reflects any changes.
     */
    private void updateAccountSelectorDropdown() {
        ObservableList<Integer> accountNums = FXCollections.observableArrayList();
        for (Account acc : currentUser.getAccounts())
            accountNums.add(acc.getAccountNumber());

        accountSelector.setItems(accountNums);

        if (currentAccount != null)
            accountSelector.setValue(currentAccount.getAccountNumber());
    }


    /**
     * Handles the action of refreshing the user's accounts. It retrieves the latest account information from the service,
     * updates the current user's account list, and refreshes the UI to reflect any changes.
     */
    private void handleRefreshAccounts() {
        MainService.AccountListResult accountsResult = mainService.getUserAccounts();
        if (accountsResult.success()) {
            currentUser.clearAccounts();
            currentUser.addAllAccounts(accountsResult.accounts());

            if (currentAccount != null) {
                for (Account acc : currentUser.getAccounts()) {
                    if (acc.getAccountNumber() == currentAccount.getAccountNumber()) {
                        currentAccount = acc;
                        mainService.setSelectedAccount(currentAccount);
                        break;
                    }
                }
            }

            updateAccountSelectorDropdown();
            refreshPage();
        } else {
            showErrorMessage("Failed to refresh accounts: " + accountsResult.errorMessage());
        }
    }


    /**
     * Refreshes the main window page by updating the account balance display,
     * enabling or disabling action buttons based on account status, and updating
     * the transaction history table with the latest transactions for the current account.
     */
    private void refreshPage() {
        if (currentAccount != null) {
            balanceLabel.setText(String.format("%,.2f Ft", currentAccount.getBalance()));
            depositButton.setDisable(currentAccount.isFrozen());
            withdrawButton.setDisable(currentAccount.isFrozen());
            transferButton.setDisable(currentAccount.isFrozen());
            freezeButton.setDisable(currentAccount.isFrozen());
            unfreezeButton.setDisable(!currentAccount.isFrozen());
        }
        updateTransactionTableData();
    }


    /**
     * Updates the transaction history table with the latest transactions for the current account.
     * It retrieves the transactions from the service, updates the current account's transaction list,
     * and populates the table with the new data. If there is an error retrieving the transactions,
     * an error message is displayed to the user.
     */
    private void updateTransactionTableData() {
        if (currentAccount == null) return;
        MainService.TransactionListResult transactionsResult = mainService.getTransactions();
        if (transactionsResult.success()) {
            List<Transaction> transactions = transactionsResult.transactions();
            currentAccount.setTransactions(transactions);
            transactionsTable.setItems(FXCollections.observableArrayList(transactions));
        } else {
            showErrorMessage("Error updating transactions: " + transactionsResult.errorMessage());
        }
    }


    /**
     * Handles the action of depositing funds into the current account. It retrieves the deposit amount from the provided text field,
     * calls the service to perform the deposit, and updates the UI based on the result. If the deposit is successful, the text field is cleared and the page is refreshed.
     * If there is an error during the deposit, an error message is displayed to the user.
     *
     * @param depositField The TextField from which to retrieve the deposit amount.
     */
    private void handleDeposit(TextField depositField) {
        MainService.TransactionResult result = mainService.deposit(depositField.getText());
        if (result.success()) {
            depositField.clear();
            refreshPage();
        } else showErrorMessage(result.message());
    }


    /**
     * Handles the action of withdrawing funds from the current account. It retrieves the withdrawal amount from the provided text field,
     * calls the service to perform the withdrawal, and updates the UI based on the result. If the withdrawal is successful, the text field is cleared and the page is refreshed.
     * If there is an error during the withdrawal, an error message is displayed to the user.
     *
     * @param withdrawField The TextField from which to retrieve the withdrawal amount.
     */
    private void handleWithdraw(TextField withdrawField) {
        MainService.TransactionResult result = mainService.withdraw(withdrawField.getText());
        if (result.success()) {
            withdrawField.clear();
            refreshPage();
        } else showErrorMessage(result.message());
    }


    /**
     * Handles the action of transferring funds from the current account to another account. It retrieves the target account number, transfer amount, and optional comment from the provided text fields,
     * calls the service to perform the transfer, and updates the UI based on the result. If the transfer is successful, all text fields are cleared and the page is refreshed.
     * If there is an error during the transfer, an error message is displayed to the user.
     *
     * @param accountField The TextField from which to retrieve the target account number for the transfer.
     * @param amountField  The TextField from which to retrieve the transfer amount.
     * @param commentField The TextField from which to retrieve any optional comment for the transfer.
     */
    private void handleTransfer(TextField accountField, TextField amountField, TextField commentField) {
        MainService.TransactionResult result = mainService.transfer(accountField.getText(), amountField.getText(), commentField.getText());
        if (result.success()) {
            showSuccessMessage(result.message());
            accountField.clear();
            amountField.clear();
            commentField.clear();
        } else showErrorMessage(result.message());
        refreshPage();
    }


    /**
     * Handles the action of opening a new account for the current user. It calls the service to perform the account opening,
     * and updates the UI based on the result. If the account is opened successfully, a success message is displayed and the accounts are refreshed.
     * If there is an error during the account opening, an error message is displayed to the user.
     */
    private void handleOpenAccount() {
        MainService.AccountResult result = mainService.openAccount();
        if (result.success()) {
            showSuccessMessage(result.message());
            handleRefreshAccounts();
        } else showErrorMessage(result.message());
    }


    /**
     * Handles the action of freezing the current account. It calls the service to perform the account freezing,
     * and updates the UI based on the result. If the account is frozen successfully, a success message is displayed and the page is refreshed.
     * If there is an error during the account freezing, an error message is displayed to the user.
     */
    private void handleFreezeAccount() {
        MainService.AccountResult result = mainService.freezeAccount();
        if (result.success())
            refreshPage();
        else showErrorMessage(result.message());
    }


    /**
     * Handles the action of unfreezing the current accounta. It calls the service to perform the account unfreezing,
     * and updates the UI based on the result. If the account is unfrozen successfully, a success message is displayed and the page is refreshed.
     * If there is an error during the account unfreezing, an error message is displayed to the user.
     */
    private void handleUnfreezeAccount() {
        MainService.AccountResult result = mainService.unfreezeAccount();
        if (result.success()) {
            refreshPage();
        } else showErrorMessage(result.message());
    }


    /**
     * Handles the action of closing the current account. It checks if the current account is the last one for the user and prompts for confirmation accordingly.
     * If the user confirms, it calls the service to perform the account closure, and updates the UI based on the result. If the account is closed successfully,
     * a success message is displayed, and if it was the last account, the user is logged out. If there is an error during the account closure, an error message is displayed to the user.
     */
    private void handleCloseAccount() {
        if (currentUser.getAccounts().isEmpty())
            return;

        boolean isLastAccount = currentUser.getAccounts().size() == 1;
        String confirmMessage = isLastAccount
                ? "Are you sure? This deletes your user profile as well."
                : "Are you sure you want to close this account?";

        if (confirmAction(confirmMessage)) {
            MainService.AccountResult result = mainService.closeAccount(isLastAccount);
            if (result.success()) {
                if (isLastAccount) {
                    close();
                    mainService.navigateToLoginWindow();
                } else {
                    currentUser.getAccounts().remove(currentAccount);
                    handleRefreshAccounts();
                }
            } else {
                showErrorMessage(result.message());
            }
        }
    }


    /**
     * Displays an error message to the user in an alert dialog.
     *
     * @param message The error message to be displayed.
     */
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Displays a success message to the user in an alert dialog.
     *
     * @param message The success message to be displayed.
     */
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Displays a confirmation dialog to the user with the given message and returns true if the user confirms the action.
     *
     * @param message The confirmation message to be displayed in the dialog.
     * @return true if the user clicks "OK", false otherwise.
     */
    private boolean confirmAction(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}