package banking;

public class Account {

    private final int userID;
    private final int accountNumber;
    private double balance;
    private boolean isFrozen;


    public Account(int id, int num, double bal, boolean fr) {
        userID = id;
        accountNumber = num;
        balance = bal;
        isFrozen = fr;
    }

    public int getUserID() { return userID; }
    public int getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public boolean isFrozen() { return isFrozen; }
}
