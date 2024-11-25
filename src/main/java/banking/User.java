package banking;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class User {

    private int userID;
    private final String email;
    private final String password;
    private final LocalDateTime dateOfRegistry;
    private final List<Account> accounts;


    public User(int id, String e, String p, LocalDateTime d) {
        userID = id;
        email = e;
        password = p;
        dateOfRegistry = d;
        accounts = new ArrayList<>();
    }

    public User(String e, String p, LocalDateTime d) {
        userID = 0;
        email = e;
        password = p;
        dateOfRegistry = d;
        accounts = new ArrayList<>();
    }

    public int getUserID() {
        return userID;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getDateOfRegistry() {
        return dateOfRegistry;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account a) {
        accounts.add(a);
    }

    public void addAllAccounts(List<Account> a) {
        accounts.addAll(a);
    }

    public void removeAccount(Account a) {
        accounts.remove(a);
    }

    public void clearAccounts() {
        accounts.clear();
    }

    public void setUserID(int id) {
        userID = id;
    }

}
