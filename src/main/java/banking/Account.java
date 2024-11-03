package banking;

import java.util.Random;

public class Account {

    private int accountNumber;
    private double balance;
    private boolean isFrozen;


    public Account() {
        Random rand = new Random();
        accountNumber = rand.nextInt(10000000, 99999999);
        balance = 0.0;
        isFrozen = false;
    }


    public Account(double b) {
        Random rand = new Random();
        accountNumber = rand.nextInt(10000000, 99999999);
        balance = b;
        isFrozen = false;
    }

    public int getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public boolean isFrozen() { return isFrozen; }
}
