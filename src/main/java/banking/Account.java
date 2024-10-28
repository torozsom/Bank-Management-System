package banking;

public class Account {

    private final int accountNumber;
    private final double balance;
    private boolean isFrozen  = false;



    public Account(int a, double b, boolean f) {
        accountNumber = a;
        balance = b;
        isFrozen = f;
    }

    public int getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public boolean isFrozen() { return isFrozen; }
}
