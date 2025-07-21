package banking.model;


/**
 * Represents a contact in the banking system.
 * Each contact has a name and an associated account number.
 */
public class Contact {

    private String name;
    private int accountNumber;


    /**
     * Creates a new Contact with the specified name and account number.
     *
     * @param n   the contact's name
     * @param num the contact's account number
     */
    public Contact(String n, int num) {
        name = n;
        accountNumber = num;
    }

    /// Gets the contact's name.
    public String getName() {
        return name;
    }

    /// Sets the contact's name.
    public void setName(String name) {
        this.name = name;
    }

    /// Gets the contact's account number.
    public int getAccountNumber() {
        return accountNumber;
    }

    /// Sets the contact's account number.
    public void setAccountNumber(int num) {
        accountNumber = num;
    }

}
