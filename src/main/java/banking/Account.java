package banking;

import java.util.ArrayList;
import java.util.List;


public class Account {

    private final int accountID;
    private final int userID;
    private final int accountNumber;
    private double balance;
    private boolean isFrozen;
    private List<Transaction> transactions;


    public Account(int acc_id, int user_id, int num, double bal, boolean fr) {
        accountID = acc_id;
        userID = user_id;
        accountNumber = num;
        balance = bal;
        isFrozen = fr;
        transactions = new ArrayList<>();
    }


    public Account(int user_id, int num, double bal, boolean fr) {
        accountID = 0;
        userID = user_id;
        accountNumber = num;
        balance = bal;
        isFrozen = fr;
        transactions = new ArrayList<>();
    }


    public int getAccountID() {
        return accountID;
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


    public void freeze() {
        isFrozen = true;
    }

    public void unfreeze() {
        isFrozen = false;
    }

    public void deposit(double amount) {
        balance += amount;
    }

    public void withdraw(double amount) {
        balance -= amount;
    }

    public void setTransactions(List<Transaction> t) {
        transactions = t;
    }

}
