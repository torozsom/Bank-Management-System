package banking;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;


public class MainWindow extends JFrame {

    private final JScrollPane scrollPane;
    private final JPanel contentPanel;


    /// Creates and shows the main window
    /// of the app with the username as title
    public MainWindow(String username) {
        setTitle(username);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        createMenuBar();

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < 5; i++) {
            JLabel label = new JLabel("Content Section " + (i + 1));
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setBorder(BorderFactory.createEmptyBorder(20, 20, 300, 20));
            contentPanel.add(label);
        }

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
        for (int i = 0; i < 5; i++) {
            int sectionIndex = i;
            JMenuItem sectionItem = new JMenuItem("Section " + (sectionIndex + 1) + ".");
            sectionItem.addActionListener(e -> scrollToSection(sectionIndex)); //Jumps to the specified section
            navigationMenu.add(sectionItem);
        }

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
}
