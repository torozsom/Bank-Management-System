# 📊 Banking Data Package

Welcome to the **Data Package** of the Banking Application! This package contains all the data access layer classes
responsible for managing persistent data storage and retrieval operations.

## 🏗️ Package Overview

The `banking.data` package implements the **Data Access Object (DAO)** pattern, providing a clean separation between
business logic and data persistence. It handles two types of storage:

- **Database Storage**: SQLite database for core banking entities (Users, Accounts, Transactions)
- **File Storage**: JSON files for auxiliary data (Contacts)

---

## 📋 Class Directory

| Class                                        | Purpose                          | Storage Type |
|----------------------------------------------|----------------------------------|--------------|
| [`DatabaseManager`](#-databasemanager)       | Database connection management   | SQLite       |
| [`UserManager`](#-usermanager)               | User operations & authentication | SQLite       |
| [`AccountManager`](#-accountmanager)         | Account management & operations  | SQLite       |
| [`TransactionManager`](#-transactionmanager) | Transaction history & processing | SQLite       |
| [`ContactManager`](#-contactmanager)         | Contact storage & retrieval      | JSON File    |

---

## 🔗 Class Relationships

```
DatabaseManager (Singleton)
    ↓ provides connections to
    ├── UserManager
    ├── AccountManager
    └── TransactionManager
            ↑ depends on
    AccountManager (for validation)

ContactManager (Independent - File-based)
```

---

## 📚 Detailed Class Documentation

### 🗄️ DatabaseManager

**Purpose**: Centralized database connection management using the Singleton pattern.

**Key Features**:

- Single point of database access
- **Thread-safe connection management with synchronization**
- **Automatic connection validation and recovery**
- **Foreign key constraints enabled for data integrity**
- **Connection timeout configuration (30 seconds)**
- SQLite database integration
- Thread-safe singleton implementation

**Usage Example**:

```java
try{
DatabaseManager dbManager = DatabaseManager.getInstance();
Connection conn = dbManager.getConnection();
// Use connection for database operations
}catch(
SQLException e){
        System.err.

println("Database connection failed: "+e.getMessage());
        }
```

**Important Methods**:

- `getInstance()`: Gets the singleton instance
- `getConnection()`: Returns the database connection
- `closeConnection()`: Safely closes the connection

---

### 👤 UserManager

**Purpose**: Handles all user-related database operations including registration, authentication, and user management.

**Key Features**:

- User registration and authentication
- **Secure password hashing with SHA-256 and salt**
- **Cryptographically secure random salt generation**
- Email validation with regex patterns
- **Backward compatibility with legacy passwords**
- User data persistence
- Date/time handling for registration

**Usage Example**:

```java
try{
UserManager userManager = new UserManager();

// Register a new user
User newUser = new User("user@example.com", "password123", LocalDateTime.now());
int userId = userManager.saveUser(newUser);

// Authenticate user
boolean isValid = userManager.authenticateUser("user@example.com", "password123");

// Load user data
User user = userManager.loadUser("user@example.com");
}catch(
SQLException e){
        System.err.

println("User operation failed: "+e.getMessage());
        }
```

**Validation Patterns**:

- `USERNAME_REGEX`: `^[a-zA-Z0-9._]+$`
- `SERVICE_REGEX`: `^[a-z]+$`
- `DOMAIN_REGEX`: `^[a-z]{2,}$`

**Important Methods**:

- `saveUser(User)`: Registers a new user
- `authenticateUser(String, String)`: Validates login credentials
- `userExists(String)`: Checks if email is already registered
- `loadUser(String)`: Retrieves user by email
- `deleteUser(String)`: Removes user from database

---

### 💳 AccountManager

**Purpose**: Comprehensive account management including CRUD operations, financial transactions, and account status
management.

**Key Features**:

- Account creation and management
- **Atomic database operations to prevent race conditions**
- **Database-level validation for frozen accounts and balances**
- Money operations (deposit, withdraw, transfer)
- Account status control (freeze/unfreeze)
- **Transaction safety with rollback support**
- Balance validation and security checks
- Multi-account support per user

**Usage Example**:

```java
try{
AccountManager accountManager = new AccountManager();

// Create and save account
Account account = new Account(12345, 1000.0, 1, "Savings Account");
boolean saved = accountManager.saveAccount(account);

// Perform money operations
boolean deposited = accountManager.depositMoney(account, 500.0);
boolean withdrawn = accountManager.withdrawMoney(account, 200.0);

// Transfer between accounts
boolean transferred = accountManager.transferMoney(12345, 67890, 100.0);

// Load user's accounts
List<Account> userAccounts = accountManager.loadAccounts(userId);
}catch(
SQLException e){
        System.err.

println("Account operation failed: "+e.getMessage());
        }
```

**Important Methods**:

- `saveAccount(Account)`: Creates new account
- `loadAccount(int)`: Retrieves account by number
- `loadAccounts(int)`: Gets all accounts for a user
- `depositMoney(Account, double)`: Adds money to account
- `withdrawMoney(Account, double)`: Removes money from account
- `transferMoney(int, int, double)`: Transfers between accounts
- `freezeAccount(Account)`: Disables account operations
- `unfreezeAccount(Account)`: Re-enables account operations

---

### 💸 TransactionManager

**Purpose**: Manages transaction records, providing complete transaction history and processing capabilities.

**Key Features**:

- Transaction persistence and retrieval
- **Optimized JOIN queries to prevent N+1 query problems**
- **Single database query for transaction loading with account data**
- Account validation before processing
- Date/time formatting and parsing
- **Ordered transaction history (newest first)**
- Transaction history by account
- Transaction deletion capabilities

**Usage Example**:

```java
try{
TransactionManager transactionManager = new TransactionManager();

// Create and save transaction
Account sender = accountManager.loadAccount(12345);
Account receiver = accountManager.loadAccount(67890);
Transaction transaction = new Transaction(sender, receiver, 250.0,
        "Payment for services", LocalDateTime.now());
boolean saved = transactionManager.saveTransaction(transaction);

// Load transaction history
List<Transaction> history = transactionManager.loadTransactions(sender);

// Delete transaction
boolean deleted = transactionManager.deleteTransaction(transactionId);
}catch(
SQLException e){
        System.err.

println("Transaction operation failed: "+e.getMessage());
        }
```

**Important Methods**:

- `saveTransaction(Transaction)`: Records new transaction
- `loadTransactions(Account)`: Gets transaction history for account
- `deleteTransaction(int)`: Removes transaction record

**Dependencies**:

- Requires `AccountManager` for account validation
- Uses `DatabaseManager` for database connectivity

---

### 📞 ContactManager

**Purpose**: Manages contact information using JSON file storage for quick access and portability.

**Key Features**:

- JSON-based persistence using Gson
- File-based storage (independent of database)
- Automatic empty list handling
- Simple save/load operations
- Lightweight and portable

**Usage Example**:

```java
try{
// Load existing contacts
List<Contact> contacts = ContactManager.loadContacts();

        // Add new contact
    contacts.

        add(new Contact("John Doe", 12345));

        // Save updated list
        ContactManager.

        saveContacts(contacts);
}catch(
        IOException e){
        System.err.

        println("Contact operation failed: "+e.getMessage());
        }
```

**Storage Details**:

- **File**: `contacts.json` (in project root)
- **Format**: JSON array of contact objects
- **Serialization**: Google Gson library

**Important Methods**:

- `saveContacts(List<Contact>)`: Persists contact list to JSON
- `loadContacts()`: Retrieves contacts from JSON file

---

## 🔒 Security Enhancements

The data package has been enhanced with comprehensive security improvements:

### **Password Security**
- **SHA-256 Hashing**: All passwords are hashed using SHA-256 algorithm
- **Cryptographic Salt**: Each password uses a unique 16-byte salt generated with SecureRandom
- **Salt Storage**: Passwords stored as "salt:hash" format for maximum security
- **Backward Compatibility**: Supports legacy plain text passwords during migration

### **Database Security**
- **Foreign Key Constraints**: Enabled to ensure referential integrity
- **Connection Validation**: Automatic connection health checks with 5-second timeout
- **Connection Timeouts**: 30-second busy timeout to prevent hanging connections
- **Thread Safety**: Synchronized connection management prevents race conditions

### **Transaction Security**
- **Atomic Operations**: All financial operations use atomic SQL queries
- **Race Condition Prevention**: Database-level validation prevents concurrent access issues
- **Query Optimization**: JOIN queries prevent N+1 problems and timing attacks
- **Transaction Rollback**: Automatic rollback on operation failures

### **Data Integrity**
- **Database-Level Validation**: Account frozen status and balance checks in SQL
- **Consistent State**: In-memory objects updated only after successful database operations
- **Ordered Results**: Transaction history ordered by date for audit consistency

---

## 🔧 Configuration

### Database Configuration

- **Database Type**: SQLite
- **Location**: `config/Banking.db`
- **Connection**: Managed by `DatabaseManager`

### File Storage Configuration

- **Contacts File**: `contacts.json`
- **Format**: JSON
- **Library**: Google Gson

---

*This documentation covers the core functionality of the banking data package. For specific implementation details,
refer to the individual class files and their comprehensive JavaDoc comments.*