package banking.model;


/**
 * Represents a contact in the banking system.
 * Each contact has a name and an associated account number.
 */
public record Contact(String name, int accountNumber) {


    /// Gets the contact's name.
    @Override
    public String name() {
        return name;
    }

    /// Gets the contact's account number.
    @Override
    public int accountNumber() {
        return accountNumber;
    }

}
