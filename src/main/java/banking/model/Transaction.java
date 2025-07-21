package banking.model;

import java.time.LocalDateTime;


/**
 * Represents a financial transaction between two accounts.
 * Each transaction has a sender, receiver, amount, comment, and timestamp.
 */
public class Transaction {

    private final int transactionID;
    private final Account sender;
    private final Account receiver;
    private final double amount;
    private final String comment;
    private final LocalDateTime date;

    /**
     * Creates a new Transaction with sender, receiver, amount, comment, and date. Transaction ID is set to 0.
     * @param s the sender account
     * @param r the receiver account
     * @param a the transaction amount
     * @param c the transaction comment
     * @param d the transaction date and time
     */
    public Transaction(Account s, Account r, double a, String c, LocalDateTime d) {
        transactionID = 0;
        sender = s;
        receiver = r;
        amount = a;
        comment = c;
        date = d;
    }

    /**
     * Creates a new Transaction with specified ID, sender, receiver, amount, comment, and date.
     * @param id the transaction ID
     * @param s the sender account
     * @param r the receiver account
     * @param a the transaction amount
     * @param c the transaction comment
     * @param d the transaction date and time
     */
    public Transaction(int id, Account s, Account r, double a, String c, LocalDateTime d) {
        transactionID = id;
        sender = s;
        receiver = r;
        amount = a;
        comment = c;
        date = d;
    }

    /// Gets the transaction ID.
    public int getTransactionID() {
        return transactionID;
    }

    /// Gets the sender account.
    public Account getSender() {
        return sender;
    }

    /// Gets the receiver account.
    public Account getReceiver() {
        return receiver;
    }

    /// Gets the transaction amount.
    public double getAmount() {
        return amount;
    }

    /// Gets the transaction comment.
    public String getComment() {
        return comment;
    }

    /// Gets the transaction date and time.
    public LocalDateTime getDate() {
        return date;
    }

}
