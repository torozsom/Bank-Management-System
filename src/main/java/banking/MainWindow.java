package banking;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class MainWindow extends JFrame {

    private final JScrollPane scrollPane;
    private final JPanel contentPanel;
    private final JPanel sidebar;
    private boolean isSidebarVisible;



    public MainWindow(String username) {

        setTitle(username);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);


        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < 5; i++) {
            JLabel label = new JLabel("Content Section " + (i + 1));
            label.setFont(new Font("Arial", Font.BOLD, 16));
            label.setBorder(BorderFactory.createEmptyBorder(20, 20, 300, 20)); // Keep your 300px bottom padding
            contentPanel.add(label);
        }


        scrollPane = new JScrollPane(contentPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setUnitIncrement(10);


        sidebar = new JPanel();
        isSidebarVisible = true;
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(122, 0));

        for (int i = 0; i < 5; i++) {
            JButton sectionButton = new JButton("Go to Section " + (i + 1));
            final int index = i;
            sectionButton.addActionListener(e -> scrollToSection(index));
            sidebar.add(sectionButton);
        }


        JButton toggleSidebarButton = new JButton("☰");
        toggleSidebarButton.addActionListener(e -> toggleSidebar());


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, scrollPane);
        splitPane.setDividerSize(0);
        splitPane.setDividerLocation(122);
        splitPane.setEnabled(false);


        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toggleSidebarButton, BorderLayout.WEST);
        topPanel.add(splitPane, BorderLayout.CENTER);

        setContentPane(topPanel);
        setVisible(true);
    }


    private void toggleSidebar() {
        isSidebarVisible = !isSidebarVisible;
        if (isSidebarVisible) {
            ((JSplitPane) getContentPane().getComponent(1)).setDividerLocation(122);
        } else {
            ((JSplitPane) getContentPane().getComponent(1)).setDividerLocation(0);
        }
    }


    private void scrollToSection(int index) {
        Rectangle sectionBounds = contentPanel.getComponent(index).getBounds();
        int scrollPosition = sectionBounds.y - scrollPane.getVerticalScrollBar().getModel().getExtent() / 2;
        scrollPosition = Math.max(0, scrollPosition);
        scrollPane.getVerticalScrollBar().setValue(scrollPosition);
    }

    public static void main(String[] args) throws SQLException {
        new LoginWindow();
    }
}

