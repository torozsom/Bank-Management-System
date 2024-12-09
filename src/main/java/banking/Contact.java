package banking;

public class Contact {

    private String name;
    private int accountNumber;

    public Contact(String n, int num) {
        name = n;
        accountNumber = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int num) {
        accountNumber = num;
    }
}
