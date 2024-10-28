package banking;

public class Transaction {

    private final Account sender;
    private final Account receiver;
    private final double amount;
    private final String comment;
    private final String date;



    public Transaction(Account s, Account r, double a, String c, String d) {
        sender = s;
        receiver = r;
        amount = a;
        comment = c;
        date = d;
    }

    public Account getSender() { return sender; }
    public Account getReceiver() { return receiver; }
    public double getAmount() { return amount; }
    public String getComment() { return comment; }
    public String getDate() { return date; }

}
