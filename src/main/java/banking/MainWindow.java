package banking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;


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

        //currentUser

        setTitle(email);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        icon = Toolkit.getDefaultToolkit().getImage("icon.png");
        setIconImage(icon);


        createMenuBar();
        contentPanel = new JPanel();
        setUpContentPanel();

        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(10);
        setContentPane(scrollPane);

        setVisible(true);
    }

    public static void main(String[] args) throws SQLException {
        new LoginWindow();
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

    /**
     * Jump to the specified section of the main window
     *
     * @param index the
     */
    private void scrollToSection(int index) {
        Rectangle sectionBounds = contentPanel.getComponent(index).getBounds();
        scrollPane.getViewport().setViewPosition(new Point(sectionBounds.x, sectionBounds.y));
    }

    /// Introduces the content panel with the necessery components
    public void setUpContentPanel() {
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JLabel balance = new JLabel("Balance");
        balance.setFont(new Font("Arial", Font.BOLD, 16));
        balance.setBorder(BorderFactory.createEmptyBorder(20, 20, 300, 20));
        contentPanel.add(balance);

        JLabel transactionHistory = new JLabel("Transaction History");
        transactionHistory.setFont(new Font("Arial", Font.BOLD, 16));
        transactionHistory.setBorder(BorderFactory.createEmptyBorder(20, 20, 50, 20));
        contentPanel.add(transactionHistory);

        contentPanel.add(transactionsTable());

        JLabel accountActions = new JLabel("Account Actions");
        accountActions.setFont(new Font("Arial", Font.BOLD, 16));
        accountActions.setBorder(BorderFactory.createEmptyBorder(300, 20, 20, 20));
        contentPanel.add(accountActions);

    }

    /// Creates a table that shows the user's transaction history
    public JTable transactionsTable() {
        Object[] headers = new Object[]{"Sender", "Receiver", "Amount", "Comment", "Date"};
        DefaultTableModel model = new DefaultTableModel(headers, 0);
        model.addRow(headers);


        JTable recentTransactions = new JTable(model);
        recentTransactions.setFont(new Font("Arial", Font.BOLD, 12));
        recentTransactions.setRowHeight(30);
        recentTransactions.setBorder(BorderFactory.createEmptyBorder(20, 20, 300, 20));

        return recentTransactions;
    }

}
