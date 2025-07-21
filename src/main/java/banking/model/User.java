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
     *
     * @param id the user ID
     * @param e  the email address
     * @param p  the password
     * @param d  the registration date
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
     *
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


    /// Gets the user ID.
    public int getUserID() {
        return userID;
    }

    /// Sets the user ID.
    public void setUserID(int id) {
        userID = id;
    }

    /// Gets the user's email address.
    public String getEmail() {
        return email;
    }

    /// Gets the user's password.
    public String getPassword() {
        return password;
    }

    /// Gets the user's registration date.
    public LocalDateTime getDateOfRegistry() {
        return dateOfRegistry;
    }

    /// Gets a copy of the user's accounts list.
    public List<Account> getAccounts() {
        return new ArrayList<>(accounts);
    }

    /// Adds an account to the user's account list.
    public void addAccount(Account a) {
        accounts.add(a);
    }

    /// Adds all accounts from the provided list to the user's account list.
    public void addAllAccounts(List<Account> a) {
        accounts.addAll(a);
    }

    /// Removes an account from the user's account list.
    public void removeAccount(Account a) {
        accounts.remove(a);
    }

    /// Clears all accounts from the user's account list.
    public void clearAccounts() {
        accounts.clear();
    }

}
