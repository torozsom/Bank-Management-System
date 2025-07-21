package banking.ui;

import banking.model.Contact;
import banking.service.ContactService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


/**
 * Panel component for managing contacts in the banking application.
 * Encapsulates all contact-related UI and functionality.
 */
public class ContactPanel extends JPanel {

    private final JFrame parentFrame;
    private final ContactService contactService;
    private DefaultListModel<String> contactListModel;

    private JTextField searchField;
    private JTextField nameField;
    private JTextField accountField;


    /**
     * Creates a new contact management panel
     *
     * @param pf The parent frame for showing dialogs
     */
    public ContactPanel(JFrame pf) {
        parentFrame = pf;
        contactService = new ContactService();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel titlePanel = createTitlePanel();
        add(titlePanel);
        JPanel inputPanel = createInputPanel();
        add(inputPanel);
        JPanel contactListPanel = createContactListPanel();
        add(contactListPanel);

        loadContactsToList();
    }


    /// Creates the title panel with the application title
    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Contact Manager");
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 20, 20, 20));
        titlePanel.add(titleLabel);
        return titlePanel;
    }

    /// Creates the input panel for adding new contacts
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        Dimension textFieldSize = new Dimension(300, 25);

        // Name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel nameLabel = new JLabel("Name of company / individual:");
        nameLabel.setFont(new Font("Times New Roman", Font.BOLD, 17));
        inputPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        nameField = new JTextField();
        nameField.setPreferredSize(textFieldSize);
        inputPanel.add(nameField, gbc);

        // Account field
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel accountLabel = new JLabel("Account number:");
        accountLabel.setFont(new Font("Times New Roman", Font.BOLD, 17));
        inputPanel.add(accountLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        accountField = new JTextField();
        accountField.setPreferredSize(textFieldSize);
        inputPanel.add(accountField, gbc);

        // Save button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        gbc.anchor = GridBagConstraints.EAST;
        JButton saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(100, 25));
        saveButton.addActionListener(e -> saveContact());
        inputPanel.add(saveButton, gbc);

        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        return inputPanel;
    }


    /// Creates the contact list panel with search functionality
    private JPanel createContactListPanel() {
        JPanel contactListPanel = new JPanel();
        contactListPanel.setLayout(new BoxLayout(contactListPanel, BoxLayout.Y_AXIS));
        contactListPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Search field
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 25));
        searchField.setFont(new Font("Times New Roman", Font.BOLD, 16));
        searchField.setText("Search by name or account number");
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterContactList(searchField.getText());
            }
        });
        searchPanel.add(searchField);
        contactListPanel.add(searchPanel);

        // Contact list
        contactListModel = new DefaultListModel<>();
        JList<String> contactList = new JList<>(contactListModel);
        contactList.setFont(new Font("Times New Roman", Font.BOLD, 16));
        contactList.setFixedCellWidth(300);
        contactList.setVisibleRowCount(10);
        JScrollPane scrollPane = new JScrollPane(contactList);
        contactListPanel.add(scrollPane);

        return contactListPanel;
    }


    /// Saves a new contact based on the input fields
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
                showSuccessMessage("Contact saved successfully.");
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


    /// Loads all contacts into the list
    public void loadContactsToList() {
        ContactService.ContactListResult result = contactService.loadContacts();
        contactListModel.clear();

        if (result.success()) {
            for (Contact contact : result.contacts()) {
                contactListModel.addElement(contact.getName() + " - " + contact.getAccountNumber());
            }
        } else {
            showErrorMessage(result.errorMessage());
        }
    }


    /// Filters the contact list based on a search query
    private void filterContactList(String query) {
        ContactService.ContactListResult result = contactService.filterContacts(query);
        contactListModel.clear();

        if (result.success()) {
            for (Contact contact : result.contacts()) {
                contactListModel.addElement(contact.getName() + " - " + contact.getAccountNumber());
            }
        } else {
            showErrorMessage(result.errorMessage());
        }
    }


    /// Displays an error message dialog
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }


    /// Displays a success message dialog
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(parentFrame, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

}