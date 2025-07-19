package banking.service;

import banking.data.ContactManager;
import banking.model.Contact;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * ContactViewModel handles the business logic for contact management operations.
 * This class separates the functional operations from the ContactPanel View.
 */
public class ContactService {

    /**
     * Saves a new contact with the provided name and account number.
     *
     * @param name              the contact's name
     * @param accountNumberText the contact's account number as string
     * @return ContactResult containing success status and message
     */
    public ContactResult saveContact(String name, String accountNumberText) {
        // Validate input
        if (name == null || name.trim().isEmpty())
            return new ContactResult(false, "Name cannot be empty.");

        if (accountNumberText == null || accountNumberText.trim().isEmpty())
            return new ContactResult(false, "Account number cannot be empty.");

        final String finalName = name.trim();
        final String finalAccountNumberText = accountNumberText.trim();

        // Parse account number
        int accountNumber;
        try {
            accountNumber = Integer.parseInt(finalAccountNumberText);
        } catch (NumberFormatException ex) {
            return new ContactResult(false, "Account number must be a valid integer.");
        }

        final int finalAccountNumber = accountNumber;

        try {
            // Load existing contacts
            List<Contact> contacts = ContactManager.loadContacts();

            // Check for duplicates
            boolean exists = contacts.stream()
                    .anyMatch(contact -> contact.getName().equalsIgnoreCase(finalName) ||
                            contact.getAccountNumber() == finalAccountNumber);

            if (exists)
                return new ContactResult(false, "Contact already exists.");

            // Add new contact and save
            contacts.add(new Contact(finalName, finalAccountNumber));
            ContactManager.saveContacts(contacts);

            return new ContactResult(true, "Contact saved successfully.");

        } catch (IOException ex) {
            return new ContactResult(false, "Failed to save contact: " + ex.getMessage());
        }
    }


    /**
     * Loads all contacts from storage.
     *
     * @return ContactListResult containing success status, message, and contact list
     */
    public ContactListResult loadContacts() {
        try {
            List<Contact> contacts = ContactManager.loadContacts();
            return new ContactListResult(true, null, contacts);
        } catch (IOException ex) {
            return new ContactListResult(false, "Failed to load contacts: " + ex.getMessage(), null);
        }
    }


    /**
     * Filters contacts based on a search query.
     *
     * @param query the search query (name or account number)
     * @return ContactListResult containing success status, message, and filtered contact list
     */
    public ContactListResult filterContacts(String query) {
        try {
            List<Contact> contacts = ContactManager.loadContacts();

            if (query == null || query.trim().isEmpty())
                return new ContactListResult(true, null, contacts);

            String lowerQuery = query.toLowerCase().trim();
            List<Contact> filteredContacts = contacts.stream()
                    .filter(contact ->
                            contact.getName().toLowerCase().contains(lowerQuery) ||
                                    String.valueOf(contact.getAccountNumber()).contains(lowerQuery))
                    .collect(Collectors.toList());

            return new ContactListResult(true, null, filteredContacts);

        } catch (IOException ex) {
            return new ContactListResult(false, "Failed to filter contacts: " + ex.getMessage(), null);
        }
    }


    /**
     * Result class for contact operations.
     */
    public record ContactResult(boolean success, String message) {
    }


    /**
     * Result class for contact list operations.
     */
    public record ContactListResult(boolean success, String errorMessage, List<Contact> contacts) {
    }
}