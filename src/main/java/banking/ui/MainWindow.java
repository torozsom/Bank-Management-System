package banking.ui;

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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

    private final VBox contentPanel;
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
        setResizable(false);

        VBox root = new VBox();
        root.getChildren().add(createMenuBar());

        contentPanel = new VBox();
        contentPanel.setSpacing(20);
        contentPanel.setPadding(new Insets(20));

        setUpAccountChooser();
        setUpAccountActions();
        setUpContactManager();
        setUpTransactionsTable();

        ScrollPane scrollPane = new ScrollPane(contentPanel);
        scrollPane.setFitToWidth(true);

        root.getChildren().add(scrollPane);

        Scene scene = new Scene(root, 900, 600);
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

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(_ -> System.exit(0));

        MenuItem logOutItem = new MenuItem("Log Out");
        logOutItem.setOnAction(_ -> {
            close();
            try {
                new LoginWindow();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        fileMenu.getItems().addAll(logOutItem, exitItem);
        menuBar.getMenus().add(fileMenu);
        return menuBar;
    }


    /**
     * Sets up the account chooser section of the main window, allowing users to select from their available accounts,
     * view their balance, and refresh the account list.
     */
    private void setUpAccountChooser() {
        HBox accountChooserPanel = new HBox(10);
        accountChooserPanel.setAlignment(Pos.CENTER);

        Label chooseLabel = new Label("Choose account:");

        accountSelector = new ComboBox<>();
        updateAccountSelectorDropdown();

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
        });

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(_ -> handleRefreshAccounts());

        accountChooserPanel.getChildren().addAll(chooseLabel, accountSelector, refreshButton);

        balanceLabel = new Label();
        HBox balancePanel = new HBox(balanceLabel);
        balancePanel.setAlignment(Pos.CENTER);

        contentPanel.getChildren().addAll(accountChooserPanel, balancePanel);
    }


    /**
     * Handles the action of refreshing the user's account list, updating the
     * account selector dropdown and refreshing the page to reflect any changes.
     * If there is an error during the refresh process, an error message is displayed to the user.
     */
    private void handleRefreshAccounts() {
        MainService.AccountListResult accountsResult = mainService.getUserAccounts();
        if (accountsResult.success()) {
            currentUser.clearAccounts();
            currentUser.addAllAccounts(accountsResult.accounts());
            updateAccountSelectorDropdown();
            refreshPage();
        } else {
            showErrorMessage("Failed to refresh accounts: " + accountsResult.errorMessage());
        }
    }


    /**
     * Updates the account selector dropdown with the current user's accounts,
     * ensuring that the currently selected account is displayed if it exists.
     */
    private void updateAccountSelectorDropdown() {
        ObservableList<Integer> accountNums = FXCollections.observableArrayList();
        for (Account acc : currentUser.getAccounts()) {
            accountNums.add(acc.getAccountNumber());
        }
        accountSelector.setItems(accountNums);
        if (currentAccount != null) {
            accountSelector.setValue(currentAccount.getAccountNumber());
        }
    }


    /**
     * Refreshes the main window page, updating the balance label and enabling or disabling action buttons
     * based on the current account's status (e.g., frozen or active). It also updates the transaction table data
     * to reflect any recent transactions associated with the current account.
     */
    private void refreshPage() {
        if (currentAccount != null) {
            balanceLabel.setText("Balance: " + currentAccount.getBalance() + " Ft");

            depositButton.setDisable(currentAccount.isFrozen());
            withdrawButton.setDisable(currentAccount.isFrozen());
            transferButton.setDisable(currentAccount.isFrozen());
            freezeButton.setDisable(currentAccount.isFrozen());
            unfreezeButton.setDisable(!currentAccount.isFrozen());
        }
        updateTransactionTableData();
    }


    /**
     * Sets up the account actions section of the main window, providing buttons and input fields for depositing,
     * withdrawing, transferring money, opening new accounts, freezing/unfreezing accounts, and closing accounts.
     * Each action is associated with an event handler that interacts with the MainService to perform the desired operation.
     */
    private void setUpAccountActions() {
        VBox actionsPanel = new VBox(10);
        Label titleLabel = new Label("Account Actions");
        actionsPanel.getChildren().add(titleLabel);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Deposit
        grid.add(new Label("1) Deposit money:"), 0, 0);
        TextField depositField = new TextField();
        grid.add(depositField, 1, 0);
        depositButton = new Button("Deposit");
        depositButton.setOnAction(_ -> handleDeposit(depositField));
        grid.add(depositButton, 2, 0);

        // Withdraw
        grid.add(new Label("2) Withdraw money:"), 0, 1);
        TextField withdrawField = new TextField();
        grid.add(withdrawField, 1, 1);
        withdrawButton = new Button("Withdraw");
        withdrawButton.setOnAction(_ -> handleWithdraw(withdrawField));
        grid.add(withdrawButton, 2, 1);

        // Transfer
        grid.add(new Label("3) Transfer money"), 0, 2);
        grid.add(new Label("Account:"), 1, 2);
        TextField transferAccountField = new TextField();
        grid.add(transferAccountField, 2, 2);

        grid.add(new Label("Amount:"), 1, 3);
        TextField transferAmountField = new TextField();
        grid.add(transferAmountField, 2, 3);

        grid.add(new Label("Comment:"), 1, 4);
        TextField transferCommentField = new TextField();
        grid.add(transferCommentField, 2, 4);

        transferButton = new Button("Transfer");
        transferButton.setOnAction(_ -> handleTransfer(transferAccountField, transferAmountField, transferCommentField));
        grid.add(transferButton, 3, 4);

        // Open Account
        grid.add(new Label("4) Open new account"), 0, 5, 2, 1);
        Button openButton = new Button("Open");
        openButton.setOnAction(_ -> handleOpenAccount());
        grid.add(openButton, 2, 5);

        // Freeze
        grid.add(new Label("5) Freeze current account"), 0, 6, 2, 1);
        freezeButton = new Button("Freeze");
        freezeButton.setOnAction(_ -> handleFreezeAccount());
        grid.add(freezeButton, 2, 6);

        // Unfreeze
        grid.add(new Label("6) Unfreeze account"), 0, 7, 2, 1);
        unfreezeButton = new Button("Unfreeze");
        unfreezeButton.setOnAction(_ -> handleUnfreezeAccount());
        grid.add(unfreezeButton, 2, 7);

        // Close Account
        grid.add(new Label("7) Close account"), 0, 8, 2, 1);
        Button closeButton = new Button("Close");
        closeButton.setOnAction(_ -> handleCloseAccount());
        grid.add(closeButton, 2, 8);

        actionsPanel.getChildren().add(grid);
        contentPanel.getChildren().add(actionsPanel);
    }


    /**
     * Handles the deposit action by interacting with the MainService to perform the deposit operation.
     * It takes the input from the deposit field, processes the deposit, and provides feedback to the user
     * based on the success or failure of the transaction. If successful, it also refreshes the page to reflect
     * the updated account balance and transaction history.
     *
     * @param depositField The TextField containing the amount to be deposited.
     */
    private void handleDeposit(TextField depositField) {
        MainService.TransactionResult result = mainService.handleDeposit(depositField.getText());
        if (result.success()) {
            showSuccessMessage(result.message());
            depositField.clear();
            refreshPage();
        } else {
            showErrorMessage(result.message());
        }
    }


    /**
     * Handles the withdraw action by interacting with the MainService to perform the withdrawal operation.
     * It takes the input from the withdraw field, processes the withdrawal, and provides feedback to the user
     * based on the success or failure of the transaction. If successful, it also refreshes the page to reflect
     * the updated account balance and transaction history.
     *
     * @param withdrawField The TextField containing the amount to be withdrawn.
     */
    private void handleWithdraw(TextField withdrawField) {
        MainService.TransactionResult result = mainService.handleWithdraw(withdrawField.getText());
        if (result.success()) {
            showSuccessMessage(result.message());
            withdrawField.clear();
            refreshPage();
        } else {
            showErrorMessage(result.message());
        }
    }


    /**
     * Handles the transfer action by interacting with the MainService to perform the transfer operation.
     * It takes the input from the account, amount, and comment fields, processes the transfer, and provides
     * feedback to the user based on the success or failure of the transaction. If successful, it also refreshes
     * the page to reflect the updated account balance and transaction history.
     *
     * @param accountField The TextField containing the recipient account number for the transfer.
     * @param amountField  The TextField containing the amount to be transferred.
     * @param commentField The TextField containing any comments associated with the transfer.
     */
    private void handleTransfer(TextField accountField, TextField amountField, TextField commentField) {
        MainService.TransactionResult result = mainService.handleTransfer(
                accountField.getText(), amountField.getText(), commentField.getText());

        if (result.success()) {
            showSuccessMessage(result.message());
            accountField.clear();
            amountField.clear();
            commentField.clear();
        } else {
            showErrorMessage(result.message());
        }
        refreshPage();
    }


    /**
     * Handles the action of opening a new account by interacting with the MainService to perform the operation.
     * It processes the request to open a new account and provides feedback to the user based on the success or failure
     * of the operation. If successful, it also refreshes the account list to include the newly opened account.
     */
    private void handleOpenAccount() {
        MainService.AccountResult result = mainService.handleOpenAccount();
        if (result.success()) {
            showSuccessMessage(result.message());
            handleRefreshAccounts();
        } else {
            showErrorMessage(result.message());
        }
    }


    /**
     * Handles the action of freezing the current account by interacting with the MainService to perform the operation.
     * It processes the request to freeze the account and provides feedback to the user based on the success or failure
     * of the operation. If successful, it also updates the current account's status to frozen and refreshes the page
     * to reflect the changes in available actions and transaction history.
     */
    private void handleFreezeAccount() {
        MainService.AccountResult result = mainService.handleFreezeAccount();
        if (result.success()) {
            showSuccessMessage(result.message());
            currentAccount.freeze();
            refreshPage();
        } else {
            showErrorMessage(result.message());
        }
    }


    /**
     * Handles the action of unfreezing the current account by interacting with the MainService to perform the operation.
     * It processes the request to unfreeze the account and provides feedback to the user based on the success or failure
     * of the operation. If successful, it also updates the current account's status to active and refreshes the page
     * to reflect the changes in available actions and transaction history.
     */
    private void handleUnfreezeAccount() {
        MainService.AccountResult result = mainService.handleUnfreezeAccount();
        if (result.success()) {
            showSuccessMessage(result.message());
            currentAccount.unfreeze();
            refreshPage();
        } else {
            showErrorMessage(result.message());
        }
    }


    /**
     * Handles the action of closing the current account by interacting with the MainService to perform the operation.
     * It checks if there are any accounts available to close and confirms the user's intention to close the account.
     * If it's the last account, it also informs the user that their profile will be deleted. Based on the success or failure
     * of the operation, it provides feedback to the user and updates the account list and page accordingly.
     */
    private void handleCloseAccount() {
        if (currentUser.getAccounts().isEmpty()) {
            showErrorMessage("No accounts available to close.");
            return;
        }

        boolean isLastAccount = currentUser.getAccounts().size() == 1;
        String confirmMessage = isLastAccount ?
                "Are you sure you want to close this account?\nThis will also delete your user profile as it's the last account." :
                "Are you sure you want to close this account?";

        if (confirmAction(confirmMessage)) {
            MainService.AccountResult result = mainService.handleCloseAccount();
            if (result.success()) {
                showSuccessMessage(result.message());
                if (isLastAccount) {
                    close();
                    MainService.NavigationResult navResult = mainService.navigateToLoginWindow();
                    if (!navResult.success()) {
                        showErrorMessage("Error navigating to login: " + navResult.errorMessage());
                    }
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
     * Sets up the contact manager section of the main window by creating a ContactPanel and adding it to the content panel.
     * The ContactPanel allows users to manage their contacts, including adding, viewing, and filtering contacts.
     */
    private void setUpContactManager() {
        ContactPanel contactPanel = new ContactPanel();
        contentPanel.getChildren().add(contactPanel);
    }


    /**
     * Sets up the transactions table in the main window, allowing users to view their transaction history.
     * The table includes columns for sender, receiver, amount, comment, and date of each transaction.
     * It also configures the table to automatically resize columns to fit the available space.
     */
    private void setUpTransactionsTable() {
        VBox tableContainer = new VBox(10);
        Label titleLabel = new Label("Transaction History");
        tableContainer.getChildren().add(titleLabel);

        transactionsTable = new TableView<>();
        transactionsTable.setPrefHeight(200);

        TableColumn<Transaction, Integer> senderCol = new TableColumn<>("Sender");
        senderCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().sender().getAccountNumber()).asObject());

        TableColumn<Transaction, Integer> receiverCol = new TableColumn<>("Receiver");
        receiverCol.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().receiver().getAccountNumber()).asObject());

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().amount()).asObject());

        TableColumn<Transaction, String> commentCol = new TableColumn<>("Comment");
        commentCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().comment()));

        TableColumn<Transaction, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> {
            String formattedDate = data.getValue().date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return new SimpleStringProperty(formattedDate);
        });

        transactionsTable.getColumns().addAll(senderCol, receiverCol, amountCol, commentCol, dateCol);
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        tableContainer.getChildren().add(transactionsTable);
        contentPanel.getChildren().add(tableContainer);
    }


    /**
     * Updates the transaction table data by fetching the latest transactions from the MainService.
     * If the transaction retrieval is successful, it updates the current account's transactions and refreshes the table view.
     * If there is an error during the retrieval process, an error message is displayed to the user.
     */
    private void updateTransactionTableData() {
        if (currentAccount == null) return;

        MainService.TransactionListResult transactionsResult = mainService.getTransactions();

        if (transactionsResult.success()) {
            List<Transaction> transactions = transactionsResult.transactions();
            currentAccount.setTransactions(transactions);

            ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(transactions);
            transactionsTable.setItems(observableTransactions);
        } else {
            showErrorMessage("Error updating transactions: " + transactionsResult.errorMessage());
        }
    }


    /**
     * Displays an error message in an alert dialog with the specified message.
     *
     * @param message The error message to be displayed in the alert dialog.
     */
    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Displays a success message in an alert dialog with the specified message.
     *
     * @param message The success message to be displayed in the alert dialog.
     */
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    /**
     * Displays a confirmation dialog with the specified message and returns true if the user confirms the action.
     *
     * @param message The confirmation message to be displayed in the dialog.
     * @return true if the user clicks "OK" to confirm the action, false otherwise.
     */
    private boolean confirmAction(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Action");
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}