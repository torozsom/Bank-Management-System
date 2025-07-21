package banking.model;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a bank account in the banking system.
 * Each account has an ID, belongs to a user, has a unique account number, balance, and can be frozen.
 */
public class Account {

    private final int accountID;
    private final int userID;
    private final int accountNumber;
    private double balance;
    private boolean isFrozen;
    private List<Transaction> transactions;


    /**
     * Creates a new Account with specified account ID, user ID, account number, balance, and frozen status.
     * @param acc_id the account ID
     * @param user_id the user ID who owns this account
     * @param num the account number
     * @param bal the initial balance
     * @param fr the frozen status
     */
    public Account(int acc_id, int user_id, int num, double bal, boolean fr) {
        accountID = acc_id;
        userID = user_id;
        accountNumber = num;
        balance = bal;
        isFrozen = fr;
        transactions = new ArrayList<>();
    }


    /**
     * Creates a new Account with user ID, account number, balance, and frozen status. Account ID is set to 0.
     * @param user_id the user ID who owns this account
     * @param num the account number
     * @param bal the initial balance
     * @param fr the frozen status
     */
    public Account(int user_id, int num, double bal, boolean fr) {
        accountID = 0;
        userID = user_id;
        accountNumber = num;
        balance = bal;
        isFrozen = fr;
        transactions = new ArrayList<>();
    }

    /// Gets the account ID.
    public int getAccountID() {
        return accountID;
    }

    /// Gets the user ID who owns this account.
    public int getUserID() {
        return userID;
    }

    /// Gets the account number.
    public int getAccountNumber() {
        return accountNumber;
    }

    /// Gets the current balance of the account.
    public double getBalance() {
        return balance;
    }

    /// Checks if the account is frozen.
    public boolean isFrozen() {
        return isFrozen;
    }

    /// Gets a copy of the account's transaction list.
    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions);
    }

    /// Sets the transaction list for this account.
    public void setTransactions(List<Transaction> t) {
        transactions = t;
    }

    /// Freezes the account, preventing transactions.
    public void freeze() {
        isFrozen = true;
    }

    /// Unfreezes the account, allowing transactions.
    public void unfreeze() {
        isFrozen = false;
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
