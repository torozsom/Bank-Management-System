package banking;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class User {

    private final String email;
    private final String password;
    private final String dateOfRegistry;
    private final List<Account> accounts;



    public User(String e, String p, String d) {
        email = e;
        password = p;
        dateOfRegistry = d;
        accounts = new ArrayList<>();

        Random rand = new Random();
        int accountNum = rand.nextInt(10000000, Integer.MAX_VALUE);
        accounts.add(new Account(accountNum, 0.0, false));
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getDateOfRegistry() { return dateOfRegistry; }
    public List<Account> getAccounts() { return accounts; }

    public void addAccount(Account a) { accounts.add(a); }
    public void removeAccount(Account a) { accounts.remove(a); }

}
