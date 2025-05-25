package banking.model;

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
        return new ArrayList<>(transactions);
    }

    public void freeze() {
        isFrozen = true;
    }

    public void unfreeze() {
        isFrozen = false;
    }

    public void setTransactions(List<Transaction> t) {
        transactions = t;
    }


    /**
     * Deposits the specified amount into this account.
     *
     * @param amount the amount to deposit
     * @throws IllegalArgumentException if the amount is not positive
     */
    public void deposit(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive");

        balance += amount;
    }


    /**
     * Withdraws the specified amount from this account.
     *
     * @param amount the amount to withdraw
     * @throws IllegalArgumentException if the amount is not positive or exceeds the balance
     */
    public void withdraw(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be positive");

        if (amount > balance)
            throw new IllegalArgumentException("Insufficient funds");

        balance -= amount;
    }

}
