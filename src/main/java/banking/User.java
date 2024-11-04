package banking;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class User {

    private final String email;
    private final String password;
    private final LocalDateTime dateOfRegistry;
    private final List<Account> accounts;


    public User(String e, String p, LocalDateTime d) {
        email = e;
        password = p;
        dateOfRegistry = d;
        accounts = new ArrayList<>();
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
    public void removeAccount(Account a) {
        accounts.remove(a);
    }

}
