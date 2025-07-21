# 🏦 Banking Model Package

Welcome to the **Model Package** of the Banking Application! This package contains all the core domain model classes
that represent the fundamental entities and business objects in the banking system.

## 🏗️ Package Overview

The `banking.model` package implements the **Domain Model** pattern, containing Plain Old Java Objects (POJOs) that
represent the core business entities. These classes encapsulate the data and basic business rules for the banking
application.

**Key Characteristics**:

- **Data Encapsulation**: Each class properly encapsulates its data with appropriate access modifiers
- **Business Logic**: Contains essential business rules and validation
- **Immutability**: Some classes use immutable design for data integrity
- **Relationships**: Models real-world relationships between banking entities

---

## 📋 Class Directory

| Class                          | Purpose                      | Mutability | Key Features                            |
|--------------------------------|------------------------------|------------|-----------------------------------------|
| [`User`](#-user)               | System user representation   | Mutable    | Authentication, account management      |
| [`Account`](#-account)         | Bank account entity          | Mutable    | Balance operations, transaction history |
| [`Transaction`](#-transaction) | Financial transaction record | Immutable  | Transfer details, audit trail           |
| [`Contact`](#-contact)         | Contact information          | Mutable    | Quick transfer references               |

---

## 🔗 Class Relationships

```
User (1) ←→ (N) Account
    ↓ owns multiple
Account (1) ←→ (N) Transaction
    ↓ participates in (as sender/receiver)
Transaction

Contact (Independent)
    ↓ references
Account (by account number)
```

**Relationship Details**:

- **User ↔ Account**: One-to-many relationship (User can have multiple accounts)
- **Account ↔ Transaction**: Many-to-many relationship (Account can be sender or receiver)
- **Contact → Account**: Reference relationship (Contact points to account by number)

---

## 📚 Detailed Class Documentation

### 👤 User

**Purpose**: Represents a registered user in the banking system with authentication credentials and account ownership.

**Key Features**:

- User authentication with email and password
- Registration timestamp tracking
- Multiple account ownership
- Account collection management
- Immutable core data (email, password, registration date)

**Class Structure**:

```java
public class User {
    private final String email;           // Immutable - user identifier
    private final String password;        // Immutable - authentication
    private final LocalDateTime dateOfRegistry; // Immutable - audit trail
    private final List<Account> accounts;  // Mutable - account collection
    private int userID;                   // Mutable - database identifier
}
```

**Usage Example**:

```java
// Create new user
User newUser = new User("john.doe@email.com", "securePassword123", LocalDateTime.now());

// Create user with ID (from database)
User existingUser = new User(1, "john.doe@email.com", "securePassword123",
        LocalDateTime.of(2023, 1, 15, 10, 30));

// Manage user accounts
Account savingsAccount = new Account(1, 12345, 1000.0, false);
Account checkingAccount = new Account(1, 67890, 500.0, false);

existingUser.

addAccount(savingsAccount);
existingUser.

addAccount(checkingAccount);

// Access user information
String email = existingUser.getEmail();
List<Account> userAccounts = existingUser.getAccounts(); // Returns defensive copy
int accountCount = userAccounts.size();
```

**Important Methods**:

- `getUserID()` / `setUserID(int)`: Database identifier management
- `getEmail()`: Retrieve user's email (immutable)
- `getPassword()`: Retrieve password for authentication
- `getDateOfRegistry()`: Get registration timestamp
- `getAccounts()`: Get defensive copy of user's accounts
- `addAccount(Account)`: Add single account to user
- `addAllAccounts(List<Account>)`: Add multiple accounts
- `removeAccount(Account)`: Remove specific account
- `clearAccounts()`: Remove all accounts

**Design Notes**:

- Email, password, and registration date are immutable for security
- Account list is mutable to allow account management
- Returns defensive copies to prevent external modification

---

### 💳 Account

**Purpose**: Represents a bank account with balance management, transaction tracking, and security controls.

**Key Features**:

- Unique account identification (ID and number)
- Balance management with validation
- Transaction history tracking
- Account freezing/unfreezing capabilities
- Deposit and withdrawal operations
- User ownership association

**Class Structure**:

```java
public class Account {
    private final int accountID;          // Immutable - database identifier
    private final int userID;            // Immutable - owner reference
    private final int accountNumber;      // Immutable - unique account number
    private double balance;               // Mutable - current balance
    private boolean isFrozen;            // Mutable - security status
    private List<Transaction> transactions; // Mutable - transaction history
}
```

**Usage Example**:

```java
// Create new account
Account account = new Account(1, 12345, 1000.0, false); // userID=1, accountNumber=12345

// Create account with database ID
Account dbAccount = new Account(101, 1, 12345, 1000.0, false); // accountID=101

// Perform balance operations
try{
        account.

deposit(500.0);        // Balance becomes 1500.0
    account.

withdraw(200.0);       // Balance becomes 1300.0

double currentBalance = account.getBalance(); // 1300.0
}catch(
IllegalArgumentException e){
        System.err.

println("Invalid operation: "+e.getMessage());
        }

// Account security
        account.

freeze();                  // Prevent transactions

boolean frozen = account.isFrozen(); // true
account.

unfreeze();               // Allow transactions

// Transaction management
List<Transaction> history = account.getTransactions(); // Defensive copy
account.

setTransactions(newTransactionList);
```

**Important Methods**:

- `getAccountID()`: Database identifier (immutable)
- `getUserID()`: Owner's user ID (immutable)
- `getAccountNumber()`: Unique account number (immutable)
- `getBalance()`: Current account balance
- `isFrozen()`: Check if account is frozen
- `deposit(double)`: Add money with validation
- `withdraw(double)`: Remove money with validation
- `freeze()` / `unfreeze()`: Security controls
- `getTransactions()`: Get defensive copy of transaction history
- `setTransactions(List<Transaction>)`: Set transaction list

**Validation Rules**:

- Deposit amounts must be positive
- Withdrawal amounts must be positive and not exceed balance
- Account numbers and IDs are immutable once set

**Design Notes**:

- Core identifiers are immutable for data integrity
- Balance operations include proper validation
- Transaction list returns defensive copies

---

### 💸 Transaction

**Purpose**: Immutable record of financial transactions between accounts with complete audit information.

**Key Features**:

- Complete transaction immutability
- Sender and receiver account references
- Amount and comment tracking
- Timestamp for audit trail
- Unique transaction identification

**Class Structure**:

```java
public class Transaction {
    private final int transactionID;      // Immutable - unique identifier
    private final Account sender;         // Immutable - source account
    private final Account receiver;       // Immutable - destination account
    private final double amount;          // Immutable - transaction amount
    private final String comment;         // Immutable - transaction description
    private final LocalDateTime date;     // Immutable - transaction timestamp
}
```

**Usage Example**:

```java
// Create accounts
Account senderAccount = new Account(1, 12345, 1000.0, false);
Account receiverAccount = new Account(2, 67890, 500.0, false);

// Create transaction without ID (for new transactions)
Transaction newTransaction = new Transaction(
        senderAccount,
        receiverAccount,
        250.0,
        "Payment for services",
        LocalDateTime.now()
);

// Create transaction with ID (from database)
Transaction existingTransaction = new Transaction(
        101,                    // transaction ID
        senderAccount,
        receiverAccount,
        250.0,
        "Payment for services",
        LocalDateTime.of(2024, 1, 15, 14, 30)
);

// Access transaction information
int id = existingTransaction.getTransactionID();
Account sender = existingTransaction.getSender();
Account receiver = existingTransaction.getReceiver();
double amount = existingTransaction.getAmount();
String description = existingTransaction.getComment();
LocalDateTime timestamp = existingTransaction.getDate();

// Transaction details
System.out.

println("Transaction "+id +": "+
        sender.getAccountNumber() +" → "+
        receiver.

getAccountNumber() +
        " ($"+amount +")");
```

**Important Methods**:

- `getTransactionID()`: Unique transaction identifier
- `getSender()`: Source account reference
- `getReceiver()`: Destination account reference
- `getAmount()`: Transaction amount
- `getComment()`: Transaction description/memo
- `getDate()`: Transaction timestamp

**Design Notes**:

- Completely immutable for audit integrity
- All fields are final and set in constructor
- No setter methods to prevent modification
- Represents a complete transaction record

---

### 📞 Contact

**Purpose**: Simple contact information for quick reference to frequently used accounts.

**Key Features**:

- Contact name storage
- Account number reference
- Mutable for easy updates
- Simple data structure

**Class Structure**:

```java
public class Contact {
    private String name;           // Mutable - contact display name
    private int accountNumber;     // Mutable - referenced account number
}
```

**Usage Example**:

```java
// Create new contact
Contact contact = new Contact("John Doe", 12345);

// Access contact information
String name = contact.getName();           // "John Doe"
int accountNum = contact.getAccountNumber(); // 12345

// Update contact information
contact.

setName("John Smith");             // Update name
contact.

setAccountNumber(67890);           // Update account reference

// Use in transaction context
Account targetAccount = accountManager.loadAccount(contact.getAccountNumber());
if(targetAccount !=null){
// Perform transaction to contact's account
Transaction payment = new Transaction(
        myAccount,
        targetAccount,
        100.0,
        "Payment to " + contact.getName(),
        LocalDateTime.now()
);
}
```

**Important Methods**:

- `getName()`: Get contact's display name
- `setName(String)`: Update contact's name
- `getAccountNumber()`: Get referenced account number
- `setAccountNumber(int)`: Update account reference

**Design Notes**:

- Simple mutable data structure
- Provides convenient reference to accounts
- No direct account object reference (uses account number)
- Lightweight for contact management

---

## 📊 Class Summary

| Aspect            | User         | Account       | Transaction   | Contact         |
|-------------------|--------------|---------------|---------------|-----------------|
| **Mutability**    | Partial      | Partial       | Immutable     | Mutable         |
| **Primary Key**   | userID       | accountID     | transactionID | N/A             |
| **Business Key**  | email        | accountNumber | N/A           | name            |
| **Collections**   | accounts     | transactions  | N/A           | N/A             |
| **Validation**    | Email format | Balance ops   | Constructor   | Basic           |
| **Relationships** | → Account    | → Transaction | → Account     | → Account (ref) |

---

*This documentation provides comprehensive coverage of the banking model package. Each class is designed to represent
real-world banking entities with appropriate data encapsulation, business rules, and relationship management. For
implementation details and advanced usage, refer to the individual class files and their JavaDoc comments.*