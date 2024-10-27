package banking;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;



public class MainWindow extends JFrame {

    private final JScrollPane scrollPane;
    private final JPanel contentPanel;



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
            label.setBorder(BorderFactory.createEmptyBorder(20, 20, 300, 20)); // Keep 300px bottom padding
            contentPanel.add(label);
        }

        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(10);
        setContentPane(scrollPane);

        setVisible(true);
    }


    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenuItem logOutItem = new JMenuItem("Log Out");
        logOutItem.addActionListener(e -> {
            dispose();
            try {
                new LoginWindow();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        fileMenu.add(logOutItem);


        JMenu navigationMenu = new JMenu("Navigáció");
        for (int i = 0; i < 5; i++) {
            int sectionIndex = i;
            JMenuItem sectionItem = new JMenuItem("Ugrás a(z) " + (sectionIndex + 1) + ". szekcióra");
            sectionItem.addActionListener(e -> scrollToSection(sectionIndex));
            navigationMenu.add(sectionItem);
        }

        menuBar.add(fileMenu);
        menuBar.add(navigationMenu);

        setJMenuBar(menuBar);
    }


    private void scrollToSection(int index) {
        Rectangle sectionBounds = contentPanel.getComponent(index).getBounds();
        scrollPane.getViewport().setViewPosition(new Point(sectionBounds.x, sectionBounds.y));
    }




    public static void main(String[] args) throws SQLException {
        new LoginWindow();
    }
}
