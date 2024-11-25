package banking;

import java.time.LocalDateTime;


public class Transaction {

    private int transactionID = 0;
    private final Account sender;
    private final Account receiver;
    private final double amount;
    private final String comment;
    private final LocalDateTime date;

    public Transaction(Account s, Account r, double a, String c, LocalDateTime d) {
        sender = s;
        receiver = r;
        amount = a;
        comment = c;
        date = d;
    }

    public Transaction(int id, Account s, Account r, double a, String c) {
        transactionID = id;
        sender = s;
        receiver = r;
        amount = a;
        comment = c;
        date = LocalDateTime.now();
    }

    public Transaction(int id, Account s, Account r, double a, String c, LocalDateTime d) {
        transactionID = id;
        sender = s;
        receiver = r;
        amount = a;
        comment = c;
        date = d;
    }


    public int getTransactionID() {
        return transactionID;
    }

    public Account getSender() {
        return sender;
    }

    public Account getReceiver() {
        return receiver;
    }

    public double getAmount() {
        return amount;
    }

    public String getComment() {
        return comment;
    }

    public LocalDateTime getDate() {
        return date;
    }

}
