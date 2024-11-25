package banking;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;


public class MainWindow extends JFrame {

    private final JScrollPane scrollPane;
    private final JPanel contentPanel;
    private JComboBox<Integer> accountSelector;

    private JLabel balanceLabel;
    private JButton refreshButton;
    private JButton depositButton;
    private JButton withdrawButton;
    private JButton transferButton;
    private JButton freezeButton;
    private JButton unfreezeButton;
    private JButton openButton;
    private JButton closeButton;

    private final UserManager userManager;
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;

    private final User currentUser;
    private Account currentAccount;
    private final Image icon;


    /// Creates and shows the main window
    /// of the app with the email as title
    public MainWindow(String email) throws SQLException {
        userManager = new UserManager();
        accountManager = new AccountManager();
        transactionManager = new TransactionManager();

        currentUser = userManager.loadUser(email);
        List<Account> accounts = accountManager.loadAccounts(currentUser.getUserID());
        currentUser.addAllAccounts(accounts);
        currentAccount = accounts.getFirst();
        currentAccount.setTransactions(transactionManager.loadTransactions(currentAccount));

        setTitle(email);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        icon = Toolkit.getDefaultToolkit().getImage("icon.png");
        setIconImage(icon);

        createMenuBar();
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        setUpAccountChooser();
        setUpAccountActions();
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
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        JMenuItem logOutItem = new JMenuItem("Log Out");

        //Logs out when clicked and gets back to the login window
        logOutItem.addActionListener(e -> {
            dispose();
            try {
                new LoginWindow();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        fileMenu.add(logOutItem);

        JMenu navigationMenu = new JMenu("View");

        JMenuItem balance = new JMenuItem("Balance");
        balance.addActionListener(e -> scrollToSection(0));
        navigationMenu.add(balance);

        JMenuItem transactionHistory = new JMenuItem("Account Actions");
        transactionHistory.addActionListener(e -> scrollToSection(1));
        navigationMenu.add(transactionHistory);

        JMenuItem accountActions = new JMenuItem("Transaction History");
        accountActions.addActionListener(e -> scrollToSection(2));
        navigationMenu.add(accountActions);

        menuBar.add(fileMenu);
        menuBar.add(navigationMenu);

        setJMenuBar(menuBar);
    }


    /// Jump to the specified section of the main window
    private void scrollToSection(int index) {
        Rectangle sectionBounds = contentPanel.getComponent(index).getBounds();
        scrollPane.getViewport().setViewPosition(new Point(sectionBounds.x, sectionBounds.y));
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

        accountSelector.addActionListener(e -> {
            try {
                int selectedAccountNumber = (int) accountSelector.getSelectedItem();
                currentAccount = accountManager.loadAccount(selectedAccountNumber);
                refreshPage();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error loading account: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        accountChooserPanel.add(accountSelector);

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> {
            try {
                currentAccount = accountManager.loadAccount((int) accountSelector.getSelectedItem());
                refreshPage();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        accountChooserPanel.add(refreshButton);
        accountChooserPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(accountChooserPanel);

        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        balanceLabel = new JLabel("Balance: " + currentAccount.getBalance() + " Ft"); // Placeholder balance
        balanceLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
        balancePanel.add(balanceLabel);
        balancePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 70, 20));

        contentPanel.add(balancePanel);
    }


    ///  real-time update of data on the UI
    private void refreshPage() {
        // Update balance label
        if (currentAccount != null) {
            balanceLabel.setText("Balance: " + currentAccount.getBalance() + " Ft");

            // Update Freeze/Unfreeze button visibility
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


    /// creates the grid layout and its account action contents
    private void setUpAccountActions() {
        JPanel actionsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel accountActionsLabel = new JLabel("Account Actions");
        accountActionsLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
        accountActionsLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        titlePanel.add(accountActionsLabel);
        contentPanel.add(titlePanel);

        Dimension buttonSize = new Dimension(100, 30);
        Dimension textFieldSize = new Dimension(150, 25);
        gbc.gridwidth = 1;
        gbc.gridy = 0;


        // Deposit contents
        JLabel depositLabel = new JLabel("1) Deposit money:");
        depositLabel.setFont(new Font("Times New Roman", Font.BOLD, 17));
        actionsPanel.add(depositLabel, gbc);

        gbc.gridx = 1;
        JTextField depositField = new JTextField();
        depositField.setPreferredSize(textFieldSize);
        actionsPanel.add(depositField, gbc);

        gbc.gridx = 2;
        depositButton = new JButton("Deposit");
        depositButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        depositButton.setPreferredSize(buttonSize);


        depositButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(depositField.getText());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (accountManager.depositMoney(currentAccount, amount)) {
                    JOptionPane.showMessageDialog(this, "Deposit successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    Transaction transaction = new Transaction(currentAccount, currentAccount, amount, "Deposit", LocalDateTime.now());
                    transactionManager.saveTransaction(transaction);
                    currentAccount.deposit(amount);
                    depositField.setText("");
                    refreshPage();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to deposit money.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input or error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionsPanel.add(depositButton, gbc);


        // Withdraw contents
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel withdrawLabel = new JLabel("2) Withdraw money:");
        withdrawLabel.setFont(new Font("Times New Roman", Font.BOLD, 17));
        actionsPanel.add(withdrawLabel, gbc);

        gbc.gridx = 1;
        JTextField withdrawField = new JTextField();
        withdrawField.setPreferredSize(textFieldSize);
        actionsPanel.add(withdrawField, gbc);

        gbc.gridx = 2;
        withdrawButton = new JButton("Withdraw");
        withdrawButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        withdrawButton.setPreferredSize(buttonSize);

        withdrawButton.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(withdrawField.getText());
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (currentAccount.getBalance() < amount) {
                    JOptionPane.showMessageDialog(this, "You don't have enough money.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (accountManager.withdrawMoney(currentAccount, amount)) {
                    JOptionPane.showMessageDialog(this, "Withdrawal successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    Transaction transaction = new Transaction(currentAccount, currentAccount, amount, "Withdrawal", LocalDateTime.now());
                    transactionManager.saveTransaction(transaction);
                    currentAccount.withdraw(amount);
                    withdrawField.setText(""); // Clear the field
                    refreshPage(); // Refresh UI
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to withdraw money.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid input or error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionsPanel.add(withdrawButton, gbc);


        // Transfer contents
        gbc.gridx = 0;
        gbc.gridy++;
        JLabel transferLabel = new JLabel("3) Transfer money");
        transferLabel.setFont(new Font("Times New Roman", Font.BOLD, 17));
        actionsPanel.add(transferLabel, gbc);

        gbc.gridy++;
        gbc.gridx = 1;
        JLabel accountLabel = new JLabel("Account:");
        accountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        actionsPanel.add(accountLabel, gbc);

        gbc.gridx = 2;
        JTextField transferAccountField = new JTextField();
        transferAccountField.setPreferredSize(textFieldSize);
        actionsPanel.add(transferAccountField, gbc);

        gbc.gridx = 1;
        gbc.gridy++;
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        actionsPanel.add(amountLabel, gbc);

        gbc.gridx = 2;
        JTextField transferAmountField = new JTextField();
        transferAmountField.setPreferredSize(textFieldSize);
        actionsPanel.add(transferAmountField, gbc);

        gbc.gridy++;
        gbc.gridx = 1;
        JLabel transferComment = new JLabel("Comment:");
        transferComment.setFont(new Font("Arial", Font.BOLD, 14));
        actionsPanel.add(transferComment, gbc);

        gbc.gridx = 2;
        JTextField transferCommentField = new JTextField();
        transferCommentField.setPreferredSize(textFieldSize);
        actionsPanel.add(transferCommentField, gbc);


        gbc.gridx = 3;
        transferButton = new JButton("Transfer");
        transferButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        transferButton.setPreferredSize(buttonSize);
        actionsPanel.add(transferButton, gbc);

        transferButton.addActionListener(e -> {
            try {
                int destAccNum = Integer.parseInt(transferAccountField.getText());
                double amount = Double.parseDouble(transferAmountField.getText());
                String comment = transferCommentField.getText();
                Account destinationAccount = accountManager.loadAccount(destAccNum);

                if (currentAccount.isFrozen() || destinationAccount.isFrozen()) {
                    JOptionPane.showMessageDialog(this, "Current our destination account is frozen!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Amount must be greater than zero.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!accountManager.transferMoney(currentAccount.getAccountNumber(), destAccNum, amount)) {
                    JOptionPane.showMessageDialog(this, "Transfer failed. Insufficient balance or invalid account.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Transaction transaction = new Transaction(currentAccount, destinationAccount, amount, comment, LocalDateTime.now());
                transactionManager.saveTransaction(transaction);
                currentAccount.withdraw(amount);
                JOptionPane.showMessageDialog(this, "Transfer successful.", "Success", JOptionPane.INFORMATION_MESSAGE);

                refreshPage();

                transferAccountField.setText("");
                transferAmountField.setText("");
                transferCommentField.setText("");
            } catch (NumberFormatException | SQLException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input or database error.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        actionsPanel.add(transferButton, gbc);


        // Open account
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel openAccLabel = new JLabel("4) Open new account");
        openAccLabel.setFont(new Font("Times New Roman", Font.BOLD, 17));
        actionsPanel.add(openAccLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        openButton = new JButton("Open");
        openButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        openButton.setPreferredSize(buttonSize);

        openButton.addActionListener(e -> {
            try {
                Random rand = new Random();
                int accountNumber;

                do {
                    accountNumber = rand.nextInt(10000000, 99999999);
                } while (accountManager.accountExists(accountNumber));

                Account account = new Account(currentUser.getUserID(), accountNumber, 0.0, false);
                if (accountManager.saveAccount(account)) {
                    currentUser.addAccount(account);
                    updateAccountSelector();
                    JOptionPane.showMessageDialog(this, "New account has been opened successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                }


            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        actionsPanel.add(openButton, gbc);


        // Freeze account
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel freezeLabel = new JLabel("5) Freeze current account");
        freezeLabel.setFont(new Font("Times New Roman", Font.BOLD, 17));
        actionsPanel.add(freezeLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        freezeButton = new JButton("Freeze");
        freezeButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        freezeButton.setPreferredSize(buttonSize);

        freezeButton.addActionListener(e -> {
            try {
                if (currentAccount.isFrozen()) {
                    JOptionPane.showMessageDialog(this, "The account is already frozen!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                accountManager.freezeAccount(currentAccount);
                JOptionPane.showMessageDialog(this, "The account has been frozen successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                currentAccount.freeze();

                refreshPage();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error while freezing the account.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        actionsPanel.add(freezeButton, gbc);


        // Unfreeze account
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel unfreezeLabel = new JLabel("6) Unfreeze account");
        unfreezeLabel.setFont(new Font("Times New Roman", Font.BOLD, 17));
        actionsPanel.add(unfreezeLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        unfreezeButton = new JButton("Unfreeze");
        unfreezeButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        unfreezeButton.setPreferredSize(buttonSize);

        unfreezeButton.addActionListener(e -> {
            try {
                if (!currentAccount.isFrozen()) {
                    JOptionPane.showMessageDialog(this, "The account is not frozen!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                accountManager.unfreezeAccount(currentAccount);
                JOptionPane.showMessageDialog(this, "The account has been unfrozen successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                currentAccount.unfreeze();

                refreshPage();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error while unfreezing the account.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        });

        actionsPanel.add(unfreezeButton, gbc);


        // Close account
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel closeAccLabel = new JLabel("7) Close account");
        closeAccLabel.setFont(new Font("Times New Roman", Font.BOLD, 17));
        actionsPanel.add(closeAccLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        closeButton = new JButton("Close");
        closeButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        closeButton.setPreferredSize(buttonSize);

        closeButton.addActionListener(e -> {
            try {

                if (currentUser.getAccounts().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No accounts available to close.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int choice;

                if (accountManager.loadAccounts(currentUser.getUserID()).size() == 1) {
                    choice = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to close this account?\n" +
                                    "This will also delete your user profile as it's the last account.",
                            "Confirm Account Closure",
                            JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.NO_OPTION)
                        return;

                    // Delete the account and the user profile
                    accountManager.deleteAccount(currentAccount);
                    userManager.deleteUser(currentUser.getEmail());

                    JOptionPane.showMessageDialog(this,
                            "The account and user profile have been deleted successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Close the current window and redirect to the login page
                    dispose();
                    new LoginWindow();

                } else if (accountManager.loadAccounts(currentUser.getUserID()).size() > 1) {
                    choice = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to close this account?",
                            "Confirm Account Closure",
                            JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.NO_OPTION)
                        return;


                    accountManager.deleteAccount(currentAccount);
                    currentUser.getAccounts().remove(currentAccount);

                    JOptionPane.showMessageDialog(this,
                            "The account has been deleted successfully.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Update account selector
                    updateAccountSelector();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error while closing the account.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        actionsPanel.add(closeButton, gbc);

        contentPanel.add(actionsPanel);
    }

    /// Refresh account selector and UI
    public void updateAccountSelector() throws SQLException {
        List<Account> updatedAccounts = accountManager.loadAccounts(currentUser.getUserID());
        Integer[] accountNumbers = updatedAccounts.stream().map(Account::getAccountNumber).toArray(Integer[]::new);
        accountSelector.setModel(new DefaultComboBoxModel<>(accountNumbers));
        if (accountNumbers.length > 0)
            currentAccount = accountManager.loadAccount(accountNumbers[0]);
        else
            currentAccount = null;

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
        try {
            if (currentAccount != null)
                currentAccount.setTransactions(transactionManager.loadTransactions(currentAccount));

            for (Component component : contentPanel.getComponents())
                if (component instanceof JScrollPane)
                    contentPanel.remove(component);

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
            recentTransactions.getTableHeader().setReorderingAllowed(false);

            contentPanel.add(new JScrollPane(recentTransactions));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating transactions: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    public static void main(String[] args) throws SQLException {
        new LoginWindow();
    }

}
