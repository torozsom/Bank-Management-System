package banking.ui;

import banking.model.Account;
import banking.model.Transaction;
import banking.model.User;
import banking.service.MainService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class MainWindow extends JFrame {

    // UI Constants
    private static final Font TITLE_FONT = new Font("Times New Roman", Font.BOLD, 20);
    private static final Font SECTION_FONT = new Font("Times New Roman", Font.BOLD, 17);
    private static final Font BUTTON_FONT = new Font("Times New Roman", Font.BOLD, 14);
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 14);

    private static final Dimension BUTTON_SIZE = new Dimension(100, 30);
    private static final Dimension TEXT_FIELD_SIZE = new Dimension(150, 25);
    private static final Insets DEFAULT_INSETS = new Insets(5, 5, 5, 5);


    private final JScrollPane scrollPane;
    private final JPanel contentPanel;
    private final MainService mainService;
    private final User currentUser;

    private JComboBox<Integer> accountSelector;
    private JLabel balanceLabel;
    private JButton depositButton;
    private JButton withdrawButton;
    private JButton transferButton;
    private JButton freezeButton;
    private JButton unfreezeButton;
    private Account currentAccount;


    /// Creates and shows the main window
    /// of the app with the email as title
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
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        Image icon = Toolkit.getDefaultToolkit().getImage("icon.png");
        setIconImage(icon);

        createMenuBar();
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        setUpAccountChooser();
        setUpAccountActions();
        setUpContactManager();
        transactionsTable();

        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(10);
        setContentPane(scrollPane);

        setVisible(true);
    }


    /// Creates a menu bar with the following options:
    /// file, view, settings.
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(_ -> System.exit(0));
        fileMenu.add(exitItem);
        JMenuItem logOutItem = new JMenuItem("Log Out");

        //Logs out when clicked and gets back to the login window
        logOutItem.addActionListener(_ -> {
            dispose();
            try {
                new LoginWindow();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        fileMenu.add(logOutItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }


    /// Introduces the account chooser and the balance contents
    private void setUpAccountChooser() {
        JPanel accountChooserPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JLabel chooseLabel = new JLabel("Choose account:");
        chooseLabel.setFont(new Font("Times New Roman", Font.BOLD, 18));
        accountChooserPanel.add(chooseLabel);

        List<Account> accounts = currentUser.getAccounts();
        Integer[] accountNums = new Integer[accounts.size()];
        for (int i = 0; i < accountNums.length; i++)
            accountNums[i] = accounts.get(i).getAccountNumber();

        accountSelector = new JComboBox<>(accountNums);
        accountSelector.setPreferredSize(new Dimension(150, 30));
        accountSelector.setFont(new Font("Times New Roman", Font.BOLD, 16));
        accountSelector.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        accountSelector.addActionListener(_ -> {
            int selectedAccountNumber = (int) accountSelector.getSelectedItem();
            // Find the account from the user's accounts list
            for (Account account : currentUser.getAccounts()) {
                if (account.getAccountNumber() == selectedAccountNumber) {
                    currentAccount = account;
                    mainService.setSelectedAccount(currentAccount);
                    refreshPage();
                    break;
                }
            }
        });

        accountChooserPanel.add(accountSelector);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(_ -> {
            try {
                // Refresh the accounts from ViewModel
                MainService.AccountListResult accountsResult = mainService.getUserAccounts();
                if (accountsResult.success()) {
                    currentUser.clearAccounts();
                    currentUser.addAllAccounts(accountsResult.accounts());
                    updateAccountSelector();
                    refreshPage();
                } else {
                    showErrorMessage("Failed to refresh accounts: " + accountsResult.errorMessage());
                }
            } catch (SQLException ex) {
                showErrorMessage("Error refreshing accounts: " + ex.getMessage());
            }
        });

        accountChooserPanel.add(refreshButton);
        accountChooserPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(accountChooserPanel);

        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        balanceLabel = new JLabel("Balance: " + currentAccount.getBalance() + " Ft");
        balanceLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
        balancePanel.add(balanceLabel);
        balancePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 70, 20));

        contentPanel.add(balancePanel);
    }


    /// Real-time update of data on the UI
    private void refreshPage() {
        if (currentAccount != null) {
            balanceLabel.setText("Balance: " + currentAccount.getBalance() + " Ft");

            depositButton.setEnabled(!currentAccount.isFrozen());
            withdrawButton.setEnabled(!currentAccount.isFrozen());
            transferButton.setEnabled(!currentAccount.isFrozen());
            freezeButton.setEnabled(!currentAccount.isFrozen());
            unfreezeButton.setEnabled(currentAccount.isFrozen());
        }

        updateTransactionTable();
        repaint();
        revalidate();
    }


    /// Creates the grid layout and its account action contents
    private void setUpAccountActions() {
        JPanel actionsPanel = createActionsPanel();
        addSectionTitle(actionsPanel);

        GridBagConstraints gbc = createDefaultConstraints();

        addDepositSection(actionsPanel, gbc);
        addWithdrawSection(actionsPanel, gbc);
        addTransferSection(actionsPanel, gbc);
        addAccountManagementSection(actionsPanel, gbc);

        contentPanel.add(actionsPanel);
    }


    /// Creates the actions panel with a grid layout
    private JPanel createActionsPanel() {
        return new JPanel(new GridBagLayout());
    }


    /// Adds a title to the account actions section
    private void addSectionTitle(JPanel ignoredActionsPanel) {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel accountActionsLabel = new JLabel("Account Actions");
        accountActionsLabel.setFont(TITLE_FONT);
        accountActionsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        titlePanel.add(accountActionsLabel);
        contentPanel.add(titlePanel);
    }


    /// Creates default constraints for the grid layout
    private GridBagConstraints createDefaultConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = DEFAULT_INSETS;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.gridy = 0;
        return gbc;
    }


    /// Adds sections for deposit, withdraw, transfer, and account management
    private void addDepositSection(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        JLabel depositLabel = createSectionLabel("1) Deposit money:");
        panel.add(depositLabel, gbc);

        gbc.gridx = 1;
        JTextField depositField = createTextField();
        panel.add(depositField, gbc);

        gbc.gridx = 2;
        depositButton = createButton("Deposit");
        depositButton.addActionListener(_ -> handleDeposit(depositField));
        panel.add(depositButton, gbc);

        gbc.gridy++;
    }


    /// Handles the deposit action
    private void handleDeposit(JTextField depositField) {
        MainService.TransactionResult result = mainService.handleDeposit(depositField.getText());
        if (result.success()) {
            showSuccessMessage(result.message());
            depositField.setText("");
            refreshPage();
        } else {
            showErrorMessage(result.message());
        }
    }


    /// Adds the withdraw section to the actions panel
    private void addWithdrawSection(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        JLabel withdrawLabel = createSectionLabel("2) Withdraw money:");
        panel.add(withdrawLabel, gbc);

        gbc.gridx = 1;
        JTextField withdrawField = createTextField();
        panel.add(withdrawField, gbc);

        gbc.gridx = 2;
        withdrawButton = createButton("Withdraw");
        withdrawButton.addActionListener(_ -> handleWithdraw(withdrawField));
        panel.add(withdrawButton, gbc);

        gbc.gridy++;
    }


    /// Handles the withdraw action
    private void handleWithdraw(JTextField withdrawField) {
        MainService.TransactionResult result = mainService.handleWithdraw(withdrawField.getText());
        if (result.success()) {
            showSuccessMessage(result.message());
            withdrawField.setText("");
            refreshPage();
        } else {
            showErrorMessage(result.message());
        }
    }


    /// Adds the transfer section to the actions panel
    private void addTransferSection(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        JLabel transferLabel = createSectionLabel("3) Transfer money");
        panel.add(transferLabel, gbc);
        gbc.gridy++;

        JTextField transferAccountField = createTextField();
        JTextField transferAmountField = createTextField();
        JTextField transferCommentField = createTextField();

        addFieldWithLabel(panel, gbc, "Account:", transferAccountField);
        gbc.gridy++;

        addFieldWithLabel(panel, gbc, "Amount:", transferAmountField);
        gbc.gridy++;

        addFieldWithLabel(panel, gbc, "Comment:", transferCommentField);

        gbc.gridx = 3;
        transferButton = createButton("Transfer");
        transferButton.addActionListener(_ -> handleTransfer(
                transferAccountField, transferAmountField, transferCommentField));
        panel.add(transferButton, gbc);

        gbc.gridy++;
    }


    /// Adds a field with a label to the panel
    private void addFieldWithLabel(JPanel panel, GridBagConstraints gbc, String labelText, JTextField field) {
        gbc.gridx = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        panel.add(label, gbc);

        gbc.gridx = 2;
        panel.add(field, gbc);
    }


    /// Handles the transfer action
    private void handleTransfer(JTextField accountField, JTextField amountField, JTextField commentField) {
        MainService.TransactionResult result = mainService.handleTransfer(
                accountField.getText(),
                amountField.getText(),
                commentField.getText()
        );

        if (result.success()) {
            showSuccessMessage(result.message());
            refreshPage();
            clearFields(accountField, amountField, commentField);
        } else {
            showErrorMessage(result.message());
        }
    }


    /// Adds sections for account management actions
    private void addAccountManagementSection(JPanel panel, GridBagConstraints gbc) {
        addOpenAccountSection(panel, gbc);
        addFreezeAccountSection(panel, gbc);
        addUnfreezeAccountSection(panel, gbc);
        addCloseAccountSection(panel, gbc);
    }


    /// Adds the open account section to the actions panel
    private void addOpenAccountSection(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel openAccLabel = createSectionLabel("4) Open new account");
        panel.add(openAccLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        JButton openButton = createButton("Open");
        openButton.addActionListener(_ -> handleOpenAccount());
        panel.add(openButton, gbc);

        gbc.gridy++;
    }


    /// Handles the action of opening a new account
    private void handleOpenAccount() {
        MainService.AccountResult result = mainService.handleOpenAccount();
        if (result.success()) {
            showSuccessMessage(result.message());
            try {
                // Refresh the accounts list
                MainService.AccountListResult accountsResult = mainService.getUserAccounts();
                if (accountsResult.success()) {
                    currentUser.clearAccounts();
                    currentUser.addAllAccounts(accountsResult.accounts());
                    updateAccountSelector();
                }
            } catch (SQLException ex) {
                showErrorMessage("Error refreshing accounts: " + ex.getMessage());
            }
        } else {
            showErrorMessage(result.message());
        }
    }


    /// Adds the freeze account section to the actions panel
    private void addFreezeAccountSection(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel freezeLabel = createSectionLabel("5) Freeze current account");
        panel.add(freezeLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        freezeButton = createButton("Freeze");
        freezeButton.addActionListener(_ -> handleFreezeAccount());
        panel.add(freezeButton, gbc);

        gbc.gridy++;
    }


    /// Handles the action of freezing the current account
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


    /// Adds the unfreeze account section to the actions panel
    private void addUnfreezeAccountSection(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel unfreezeLabel = createSectionLabel("6) Unfreeze account");
        panel.add(unfreezeLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        unfreezeButton = createButton("Unfreeze");
        unfreezeButton.addActionListener(_ -> handleUnfreezeAccount());
        panel.add(unfreezeButton, gbc);

        gbc.gridy++;
    }


    /// Handles the action of unfreezing the current account
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


    /// Adds the close account section to the actions panel
    private void addCloseAccountSection(JPanel panel, GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel closeAccLabel = createSectionLabel("7) Close account");
        panel.add(closeAccLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        JButton closeButton = createButton("Close");
        closeButton.addActionListener(_ -> handleCloseAccount());
        panel.add(closeButton, gbc);
    }


    /// Handles the action of closing the current account
    private void handleCloseAccount() {
        try {
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
                        dispose();
                        MainService.NavigationResult navResult = mainService.navigateToLoginWindow();
                        if (!navResult.success()) {
                            showErrorMessage("Error navigating to login: " + navResult.errorMessage());
                        }
                    } else {
                        currentUser.getAccounts().remove(currentAccount);
                        updateAccountSelector();
                    }
                } else {
                    showErrorMessage(result.message());
                }
            }
        } catch (SQLException ex) {
            showErrorMessage("Database error while closing the account.");
        }
    }


    /// Creates a section label with a specific font
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SECTION_FONT);
        return label;
    }


    /// Creates a text field with a preferred size
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(TEXT_FIELD_SIZE);
        return field;
    }


    /// Creates a button with a specific text and font
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(BUTTON_SIZE);
        return button;
    }


    /// Shows an error message dialog
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    /// Shows a success message dialog
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }


    /// Confirms an action with the user
    private boolean confirmAction(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Confirm Action",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }


    /// Clears the text fields
    private void clearFields(JTextField... fields) {
        for (JTextField field : fields)
            field.setText("");
    }


    /// Refresh account selector and UI
    public void updateAccountSelector() throws SQLException {
        MainService.AccountListResult accountsResult = mainService.getUserAccounts();
        if (accountsResult.success()) {
            List<Account> updatedAccounts = accountsResult.accounts();
            Integer[] accountNumbers = updatedAccounts.stream().map(Account::getAccountNumber).toArray(Integer[]::new);
            accountSelector.setModel(new DefaultComboBoxModel<>(accountNumbers));
            if (accountNumbers.length > 0) {
                currentAccount = updatedAccounts.getFirst();
                mainService.setSelectedAccount(currentAccount);
            } else {
                currentAccount = null;
            }
        } else {
            showErrorMessage("Failed to update account selector: " + accountsResult.errorMessage());
        }

        refreshPage();
    }


    /// Creates a table that shows the user's transaction history
    public void transactionsTable() {
        JPanel transactionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel transactionHistory = new JLabel("Transaction History");
        transactionHistory.setFont(new Font("Times New Roman", Font.BOLD, 20));
        transactionHistory.setBorder(BorderFactory.createEmptyBorder(100, 20, 20, 20));
        transactionsPanel.add(transactionHistory);

        contentPanel.add(transactionsPanel);

        Object[] headers = new Object[]{"Sender", "Receiver", "Amount", "Comment", "Date"};
        List<Transaction> transactions = currentAccount.getTransactions();

        Object[][] data = new Object[transactions.size()][5];
        for (int i = 0; i < transactions.size(); i++) {
            data[i][0] = transactions.get(i).getSender().getAccountNumber();
            data[i][1] = transactions.get(i).getReceiver().getAccountNumber();
            data[i][2] = transactions.get(i).getAmount();
            data[i][3] = transactions.get(i).getComment();
            data[i][4] = transactions.get(i).getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        JTable recentTransactions = new JTable(data, headers);
        recentTransactions.setFont(new Font("Times New Roman", Font.BOLD, 15));
        recentTransactions.setRowHeight(30);
        recentTransactions.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        recentTransactions.getTableHeader().setReorderingAllowed(false);

        contentPanel.add(new JScrollPane(recentTransactions));
    }


    /// updates the content of the transaction table real-time
    private void updateTransactionTable() {
        if (currentAccount == null) return;

        MainService.TransactionListResult transactionsResult = mainService.getTransactions();

        for (Component component : contentPanel.getComponents())
            if (component instanceof JScrollPane)
                contentPanel.remove(component);

        if (transactionsResult.success()) {
            Object[] headers = new Object[]{"Sender", "Receiver", "Amount", "Comment", "Date"};
            List<Transaction> transactions = transactionsResult.transactions();
            currentAccount.setTransactions(transactions);

            Object[][] data = new Object[transactions.size()][5];
            for (int i = 0; i < transactions.size(); i++) {
                data[i][0] = transactions.get(i).getSender().getAccountNumber();
                data[i][1] = transactions.get(i).getReceiver().getAccountNumber();
                data[i][2] = transactions.get(i).getAmount();
                data[i][3] = transactions.get(i).getComment();
                data[i][4] = transactions.get(i).getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }

            JTable recentTransactions = new JTable(data, headers);
            recentTransactions.setFont(new Font("Times New Roman", Font.BOLD, 15));
            recentTransactions.setRowHeight(30);
            recentTransactions.getTableHeader().setReorderingAllowed(false);

            contentPanel.add(new JScrollPane(recentTransactions));
        } else {
            showErrorMessage("Error updating transactions: " + transactionsResult.errorMessage());
        }
    }


    /// Sets up the contact manager section using the ContactPanel component
    private void setUpContactManager() {
        ContactPanel contactPanel = new ContactPanel(this);
        contentPanel.add(contactPanel);
    }

}