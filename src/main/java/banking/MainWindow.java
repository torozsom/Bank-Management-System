package banking;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;


public class MainWindow extends JFrame {

    private final JScrollPane scrollPane;
    private final JPanel contentPanel;

    private final UserManager userManager;
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;

    private User currentUser;
    private final Image icon;



    /// Creates and shows the main window
    /// of the app with the username as title
    public MainWindow(String email) throws SQLException {
        userManager = new UserManager();
        accountManager = new AccountManager();
        transactionManager = new TransactionManager();

        currentUser = userManager.loadUser(email);
        List<Account> accounts = accountManager.loadAccounts(currentUser.getUserID());
        currentUser.addAllAccounts(accounts);

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

        setUpBalanceSection();
        setUpContentPanel();

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

        JMenuItem transactionHistory = new JMenuItem("Transaction History");
        transactionHistory.addActionListener(e -> scrollToSection(1));
        navigationMenu.add(transactionHistory);

        JMenuItem accountActions = new JMenuItem("Account Actions");
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


    /// Introduces the content panel with the necessery components
    public void setUpContentPanel() {
        JLabel accountActions = new JLabel("Account Actions");
        accountActions.setFont(new Font("Arial", Font.BOLD, 16));
        accountActions.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(accountActions);

        JLabel transactionHistory = new JLabel("Transaction History");
        transactionHistory.setFont(new Font("Arial", Font.BOLD, 16));
        transactionHistory.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(transactionHistory);

        contentPanel.add(transactionsTable());
    }


    /// Introduces the account chooser and the balance contents
    public void setUpBalanceSection() {
        JPanel choosingPanel = new JPanel(new FlowLayout());

        JLabel choose = new JLabel("Choose account:");
        choose.setFont(new Font("Arial", Font.BOLD, 16));
        choose.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        choosingPanel.add(choose);

        List<Account> accounts = currentUser.getAccounts();
        Integer[] accountNums = new Integer[accounts.size()];
        for (int i = 0; i < accountNums.length; i++)
            accountNums[i] = accounts.get(i).getAccountNumber();

        JComboBox<Integer> accountSelector = new JComboBox<>(accountNums);
        accountSelector.setPreferredSize(new Dimension(150, 30));
        accountSelector.setFont(new Font("Times New Roman", Font.BOLD, 16));
        choosingPanel.add(accountSelector);
        choosingPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(choosingPanel);

        JLabel balance = new JLabel("Balance");
        balance.setFont(new Font("Arial", Font.BOLD, 16));
        balance.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        balance.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(balance);
    }


    /// Creates a table that shows the user's transaction history
    public JScrollPane transactionsTable() {
        Object[] headers = new Object[]{"Sender", "Receiver", "Amount", "Comment", "Date"};
        Object[][] data = new Object[][]{
                {"data1", "data2", "data3", "data4", "data5"},
                {"data1", "data2", "data3", "data4", "data5"}
        };

        JTable recentTransactions = new JTable(data, headers);
        recentTransactions.setFont(new Font("Times New Roman", Font.BOLD, 15));
        recentTransactions.setRowHeight(30);
        recentTransactions.setBorder(BorderFactory.createEmptyBorder(20, 20, 300, 20));
        recentTransactions.getTableHeader().setReorderingAllowed(false);

        return new JScrollPane(recentTransactions);
    }


    public static void main(String[] args) throws SQLException {
        new LoginWindow();
    }

}
