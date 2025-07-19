package banking.model;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the banking system.
 * Each user has an email, password, registration date, and can have multiple accounts.
 */
public class User {

    private final String email;
    private final String password;
    private final LocalDateTime dateOfRegistry;
    private final List<Account> accounts;
    private int userID;

    /**
     * Creates a new User with specified ID, email, password, and registration date.
     * @param id the user ID
     * @param e the email address
     * @param p the password
     * @param d the registration date
     */
    public User(int id, String e, String p, LocalDateTime d) {
        userID = id;
        email = e;
        password = p;
        dateOfRegistry = d;
        accounts = new ArrayList<>();
    }

    /**
     * Creates a new User with email, password, and registration date. User ID is set to 0.
     * @param e the email address
     * @param p the password
     * @param d the registration date
     */
    public User(String e, String p, LocalDateTime d) {
        userID = 0;
        email = e;
        password = p;
        dateOfRegistry = d;
        accounts = new ArrayList<>();
    }

    /**
     * Gets the user ID.
     * @return the user ID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Sets the user ID.
     * @param id the user ID to set
     */
    public void setUserID(int id) {
        userID = id;
    }

    /**
     * Gets the user's email address.
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Gets the user's password.
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the user's registration date.
     * @return the registration date
     */
    public LocalDateTime getDateOfRegistry() {
        return dateOfRegistry;
    }

    /**
     * Gets a copy of the user's accounts list.
     * @return a new list containing all user accounts
     */
    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }

    /**
     * Adds an account to the user's account list.
     * @param a the account to add
     */
    public void addAccount(Account a) {
        accounts.add(a);
    }

    /**
     * Adds all accounts from the provided list to the user's account list.
     * @param a the list of accounts to add
     */
    public void addAllAccounts(List<Account> a) {
        accounts.addAll(a);
    }

    /**
     * Removes an account from the user's account list.
     * @param a the account to remove
     */
    public void removeAccount(Account a) {
        accounts.remove(a);
    }

    /**
     * Clears all accounts from the user's account list.
     */
    public void clearAccounts() {
        accounts.clear();
    }

}
