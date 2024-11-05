package banking;

import java.util.ArrayList;
import java.util.List;


public class Account {

    private final int userID;
    private final int accountNumber;
    private final double balance;
    private final boolean isFrozen;
    private final List<Transaction> transactions;


    public Account(int id, int num, double bal, boolean fr) {
        userID = id;
        accountNumber = num;
        balance = bal;
        isFrozen = fr;
        transactions = new ArrayList<>();
    }

    public int getUserID() {
        return userID;
    }
    public int getAccountNumber() {
        return accountNumber;
    }
    public double getBalance() {
        return balance;
    }
    public boolean isFrozen() {
        return isFrozen;
    }
    public List<Transaction> getTransactions() {
        return transactions;
    }
    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

}
