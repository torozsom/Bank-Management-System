package banking.model;

import java.time.LocalDateTime;


/**
 * Represents a financial transaction between two accounts.
 * Each transaction has a sender, receiver, amount, comment, and timestamp.
 */
public record Transaction(int transactionID, Account sender, Account receiver,
                          double amount, String comment, LocalDateTime date) {

    /**
     * Creates a new Transaction with sender, receiver, amount, comment, and date. Transaction ID is set to 0.
     *
     * @param s the sender account
     * @param r the receiver account
     * @param a the transaction amount
     * @param c the transaction comment
     * @param d the transaction date and time
     */
    public Transaction(Account s, Account r, double a, String c, LocalDateTime d) {
        this(0, s, r, a, c, d);
    }


    /// Gets the transaction ID.
    @Override
    public int transactionID() {
        return transactionID;
    }

    /// Gets the sender account.
    @Override
    public Account sender() {
        return sender;
    }

    /// Gets the receiver account.
    @Override
    public Account receiver() {
        return receiver;
    }

    /// Gets the transaction amount.
    @Override
    public double amount() {
        return amount;
    }

    /// Gets the transaction comment.
    @Override
    public String comment() {
        return comment;
    }

    /// Gets the transaction date and time.
    @Override
    public LocalDateTime date() {
        return date;
    }

}
