# 🏢 Banking Service Package

Welcome to the **Service Package** of the Banking Application! This package contains all the business logic layer
classes that handle the core application workflows and operations between the UI and data layers.

## 🏗️ Package Overview

The `banking.service` package implements the **Service Layer** pattern, providing a clean separation between the user
interface and data access layers. These services encapsulate business logic, validation rules, and coordinate operations
across multiple data managers.

**Key Characteristics**:

- **Business Logic Encapsulation**: Each service handles specific business workflows
- **Result Pattern**: Consistent result objects for operation outcomes
- **Validation & Error Handling**: Comprehensive input validation and error management
- **UI-Data Coordination**: Bridges user interface components with data persistence
- **Stateful Operations**: Manages application state and user sessions

---

## 📋 Class Directory

| Class                                          | Purpose                        | Primary Operations               | Dependencies                                    |
|------------------------------------------------|--------------------------------|----------------------------------|-------------------------------------------------|
| [`LoginService`](#-loginservice)               | Authentication & navigation    | Login, window navigation         | UserManager                                     |
| [`RegistrationService`](#-registrationservice) | User registration & validation | User signup, email validation    | UserManager, AccountManager                     |
| [`MainService`](#-mainservice)                 | Core banking operations        | Transactions, account management | UserManager, AccountManager, TransactionManager |
| [`ContactService`](#-contactservice)           | Contact management             | Contact CRUD operations          | ContactManager                                  |

---

## 🔗 Service Relationships

```
LoginService
    ↓ authenticates user
    ↓ navigates to
MainService ←→ User Session
    ↓ manages
    ├── Account Operations
    ├── Transaction Operations
    └── Navigation

RegistrationService
    ↓ creates user & account
    ↓ navigates to
LoginService

ContactService (Independent)
    ↓ manages
Contact Data (JSON)
```

**Workflow Dependencies**:

- **Login → Main**: Successful authentication leads to main banking operations
- **Registration → Login**: New user registration flows back to login
- **Main ↔ Login**: Users can logout and return to login
- **Contact**: Independent service for contact management across the application

---

## 📚 Detailed Class Documentation

### 🔐 LoginService

**Purpose**: Handles user authentication and application navigation between login, registration, and main windows.

**Key Features**:

- User credential validation
- Database authentication
- Window navigation management
- Error handling for authentication failures
- Result-based operation feedback

**Class Structure**:

```java
public class LoginService {
    private final UserManager userManager;

    // Authentication & Navigation Methods
    public AuthenticationResult authenticateUser(String email, String password)

    public NavigationResult navigateToMainWindow(String email)

    public NavigationResult navigateToRegistrationWindow()
}
```

**Usage Example**:

```java
try{
LoginService loginService = new LoginService();

// Authenticate user
AuthenticationResult authResult = loginService.authenticateUser("user@example.com", "password");
    if(authResult.

success()){
// Navigate to main window
NavigationResult navResult = loginService.navigateToMainWindow("user@example.com");
        if(navResult.

success()){
        System.out.

println("Login successful!");
        }
                }else{
                System.out.

println("Login failed: "+authResult.message());
        }

// Navigate to registration
NavigationResult regNav = loginService.navigateToRegistrationWindow();
}catch(
SQLException e){
        System.err.

println("Service initialization failed: "+e.getMessage());
        }
```

**Important Methods**:

- `authenticateUser(String, String)`: Validates user credentials
- `navigateToMainWindow(String)`: Opens main banking interface
- `navigateToRegistrationWindow()`: Opens user registration interface

**Result Classes**:

- `AuthenticationResult(boolean success, String message)`: Authentication outcome
- `NavigationResult(boolean success, String errorMessage)`: Navigation outcome

---

### 📝 RegistrationService

**Purpose**: Comprehensive user registration service with validation, account creation, and email format verification.

**Key Features**:

- Email format validation with regex patterns
- Password strength validation
- Password confirmation matching
- Automatic account creation upon registration
- Unique account number generation
- Database transaction coordination

**Class Structure**:

```java
public class RegistrationService {
    private final UserManager userManager;
    private final AccountManager accountManager;

    // Validation Methods
    public boolean isValidEmailAddress(String email)

    public ValidationResult validatePassword(String password)

    public ValidationResult validatePasswordMatch(String password, String confirmPassword)

    // Registration & Navigation
    public RegistrationResult registerUser(String email, String password, String confirmPassword)

    public NavigationResult navigateToLoginWindow()
}
```

**Usage Example**:

```java
try{
RegistrationService regService = new RegistrationService();

// Validate email format
boolean validEmail = regService.isValidEmailAddress("user@example.com");

// Validate password
ValidationResult passValidation = regService.validatePassword("myPassword123");
    if(!passValidation.

success()){
        System.out.

println("Password issue: "+passValidation.message());
        return;
        }

// Register new user
RegistrationResult result = regService.registerUser(
        "user@example.com",
        "myPassword123",
        "myPassword123"
);
    
    if(result.

success()){
        System.out.

println("Registration successful!");
// Navigate back to login
        regService.

navigateToLoginWindow();
    }else{
            System.out.

println("Registration failed: "+result.message());
        }
        }catch(
SQLException e){
        System.err.

println("Registration service error: "+e.getMessage());
        }
```

**Validation Rules**:

- **Email Format**: Must match `username@service.domain` pattern
- **Password Length**: Between 5-15 characters
- **Password Match**: Confirmation must match original
- **Unique Email**: Email must not already exist in system

**Important Methods**:

- `isValidEmailAddress(String)`: Validates email format using regex
- `validatePassword(String)`: Checks password strength requirements
- `validatePasswordMatch(String, String)`: Ensures password confirmation
- `registerUser(String, String, String)`: Complete registration process
- `navigateToLoginWindow()`: Returns to login interface

**Result Classes**:

- `ValidationResult(boolean success, String message)`: Validation outcome
- `RegistrationResult(boolean success, String message)`: Registration outcome
- `NavigationResult(boolean success, String errorMessage)`: Navigation outcome

---

### 🏦 MainService

**Purpose**: Comprehensive banking operations service managing all core financial transactions, account operations, and
user session state.

**Key Features**:

- Complete banking transaction suite (deposit, withdraw, transfer)
- Account lifecycle management (create, freeze, unfreeze, close)
- Transaction history and audit trail
- User session and account selection management
- Comprehensive validation and error handling
- Automatic transaction logging

**Class Structure**:

```java
public class MainService {
    private final UserManager userManager;
    private final AccountManager accountManager;
    private final TransactionManager transactionManager;
    private final User currentUser;
    private Account selectedAccount;

    // Session Management
    public User getCurrentUser()

    public Account getSelectedAccount()

    public void setSelectedAccount(Account account)

    public AccountListResult getUserAccounts()

    // Financial Operations
    public TransactionResult handleDeposit(String amountText)

    public TransactionResult handleWithdraw(String amountText)

    public TransactionResult handleTransfer(String accountNumberText, String amountText, String comment)

    // Account Management
    public AccountResult handleOpenAccount()

    public AccountResult handleFreezeAccount()

    public AccountResult handleUnfreezeAccount()

    public AccountResult handleCloseAccount()

    // History & Navigation
    public TransactionListResult getTransactions()

    public NavigationResult navigateToLoginWindow()
}
```

**Usage Example**:

```java
try{
MainService mainService = new MainService("user@example.com");

// Get user accounts
AccountListResult accountsResult = mainService.getUserAccounts();
    if(accountsResult.

success() &&!accountsResult.

accounts().

isEmpty()){
// Select first account
Account account = accountsResult.accounts().get(0);
        mainService.

setSelectedAccount(account);

// Perform deposit
TransactionResult depositResult = mainService.handleDeposit("500.00");
        if(depositResult.

success()){
        System.out.

println("Deposit successful!");
        }

// Perform transfer
TransactionResult transferResult = mainService.handleTransfer(
        "12345678", "100.00", "Payment for services"
);

// Get transaction history
TransactionListResult historyResult = mainService.getTransactions();
        if(historyResult.

success()){
List<Transaction> transactions = historyResult.transactions();
            System.out.

println("Found "+transactions.size() +" transactions");
        }

// Create new account
AccountResult newAccountResult = mainService.handleOpenAccount();
        if(newAccountResult.

success()){
        System.out.

println(newAccountResult.message()); // Shows new account number
        }
        }
        }catch(
SQLException e){
        System.err.

println("Main service error: "+e.getMessage());
        }
```

**Financial Operations**:

- **Deposits**: Add money to selected account with validation
- **Withdrawals**: Remove money with balance verification
- **Transfers**: Move money between accounts with recipient validation
- **Account Creation**: Generate unique account numbers automatically
- **Account Security**: Freeze/unfreeze accounts for security
- **Account Closure**: Close accounts with balance verification

**Important Methods**:

- `handleDeposit(String)`: Process money deposits
- `handleWithdraw(String)`: Process money withdrawals
- `handleTransfer(String, String, String)`: Process money transfers
- `handleOpenAccount()`: Create new bank accounts
- `handleFreezeAccount()` / `handleUnfreezeAccount()`: Account security
- `handleCloseAccount()`: Account closure with validation
- `getTransactions()`: Retrieve transaction history
- `getUserAccounts()`: Get all user's accounts

**Result Classes**:

- `TransactionResult(boolean success, String message)`: Financial operation outcome
- `AccountResult(boolean success, String message)`: Account operation outcome
- `AccountListResult(boolean success, String errorMessage, List<Account> accounts)`: Account list outcome
- `TransactionListResult(boolean success, String errorMessage, List<Transaction> transactions)`: Transaction list
  outcome
- `NavigationResult(boolean success, String errorMessage)`: Navigation outcome

---

### 📞 ContactService

**Purpose**: Contact management service for storing and retrieving frequently used account information for quick
transfers.

**Key Features**:

- Contact creation with validation
- Contact list management
- Contact search and filtering
- Duplicate prevention
- JSON-based persistence
- Input validation and sanitization

**Class Structure**:

```java
public class ContactService {
    // Contact Operations
    public ContactResult saveContact(String name, String accountNumberText)

    public ContactListResult loadContacts()

    public ContactListResult filterContacts(String query)
}
```

**Usage Example**:

```java
ContactService contactService = new ContactService();

// Save new contact
ContactResult saveResult = contactService.saveContact("John Doe", "12345678");
if(saveResult.

success()){
        System.out.

println("Contact saved successfully!");
}else{
        System.out.

println("Save failed: "+saveResult.message());
        }

// Load all contacts
ContactListResult loadResult = contactService.loadContacts();
if(loadResult.

success()){
List<Contact> contacts = loadResult.contacts();
    System.out.

println("Loaded "+contacts.size() +" contacts");
        }

// Filter contacts
ContactListResult filterResult = contactService.filterContacts("John");
if(filterResult.

success()){
List<Contact> filtered = filterResult.contacts();
    System.out.

println("Found "+filtered.size() +" matching contacts");
        }
```

**Validation Rules**:

- **Name**: Cannot be empty or null
- **Account Number**: Must be valid integer format
- **Duplicates**: Prevents duplicate names or account numbers
- **Input Sanitization**: Trims whitespace from inputs

**Important Methods**:

- `saveContact(String, String)`: Add new contact with validation
- `loadContacts()`: Retrieve all stored contacts
- `filterContacts(String)`: Search contacts by name or account number

**Result Classes**:

- `ContactResult(boolean success, String message)`: Contact operation outcome
- `ContactListResult(boolean success, String errorMessage, List<Contact> contacts)`: Contact list outcome

---

## 🔧 Design Patterns Used

### **Result Pattern**

All services use consistent result objects for operation outcomes:

```java
public record TransactionResult(boolean success, String message) {
}

public record ValidationResult(boolean success, String message) {
}

public record NavigationResult(boolean success, String errorMessage) {
}
```

### **Service Layer Pattern**

- Clear separation between UI, business logic, and data access
- Services coordinate multiple data managers
- Encapsulate complex business workflows

### **State Management**

```java
// MainService manages user session state
private final User currentUser;
private Account selectedAccount;

// Provides controlled access to state
public User getCurrentUser() {
    return currentUser;
}

public void setSelectedAccount(Account account) {
    this.selectedAccount = account;
}
```

### **Validation Chain**

```java
// RegistrationService chains multiple validations
ValidationResult passwordValidation = validatePassword(password);
if(!passwordValidation.

success()){
        return new

RegistrationResult(false,passwordValidation.message());
        }

ValidationResult matchValidation = validatePasswordMatch(password, confirmPassword);
if(!matchValidation.

success()){
        return new

RegistrationResult(false,matchValidation.message());
        }
```

---

## 📊 Service Summary

| Aspect                | LoginService   | RegistrationService         | MainService             | ContactService     |
|-----------------------|----------------|-----------------------------|-------------------------|--------------------|
| **Primary Purpose**   | Authentication | User Registration           | Banking Operations      | Contact Management |
| **State Management**  | Stateless      | Stateless                   | Stateful (User/Account) | Stateless          |
| **Data Dependencies** | UserManager    | UserManager, AccountManager | All Managers            | ContactManager     |
| **Validation Level**  | Basic          | Comprehensive               | Extensive               | Moderate           |
| **Result Types**      | 2              | 3                           | 8                       | 2                  |
| **Navigation**        | Yes            | Yes                         | Yes                     | No                 |
| **Error Handling**    | Database       | Database + Validation       | Database + Business     | File I/O           |

---

## 🔄 Service Interaction Flow

```
1. User Registration:
   RegistrationService → UserManager + AccountManager → Database
   
2. User Login:
   LoginService → UserManager → Database → MainService
   
3. Banking Operations:
   MainService → AccountManager + TransactionManager → Database
   
4. Contact Management:
   ContactService → ContactManager → JSON File
   
5. Navigation:
   Any Service → UI Window Creation → New Service Instance
```

---

*This documentation provides comprehensive coverage of the banking service package. Each service is designed to handle
specific business workflows with proper validation, error handling, and result management. For implementation details
and advanced usage patterns, refer to the individual class files and their comprehensive JavaDoc comments.*