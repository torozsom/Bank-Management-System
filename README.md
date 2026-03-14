<div align="center">
   
# 🏦 Bank Management System

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.java.net/)
[![SQLite](https://img.shields.io/badge/SQLite-3.50.3.0-blue.svg)](https://www.sqlite.org/)
[![JUnit](https://img.shields.io/badge/JUnit-Jupiter-green.svg)](https://junit.org/junit5/)

A comprehensive **Java-based Bank Management System** featuring modern architecture, advanced security, and intuitive user interface. Built with enterprise-grade patterns and best practices for reliable financial operations.

</div>

---

## 🌟 Features

### 💳 **Account Management**
- **Multi-Account Support**: Users can create and manage multiple bank accounts
- **Account Operations**: Deposits, withdrawals, and inter-account transfers
- **Security Controls**: Account freezing/unfreezing for enhanced security
- **Balance Tracking**: Real-time balance updates and validation
- **Account Lifecycle**: Complete account creation and closure workflows

### 👤 **User Management**
- **Secure Registration**: Email validation with comprehensive format checking
- **Advanced Authentication**: SHA-256 password hashing with cryptographic salt
- **Session Management**: Secure user sessions with proper state management
- **Profile Management**: User data persistence and retrieval

### 💸 **Transaction System**
- **Complete Transaction History**: Detailed audit trail for all operations
- **Transaction Types**: Deposits, withdrawals, transfers with comments
- **Real-time Processing**: Immediate transaction validation and execution
- **Data Integrity**: Immutable transaction records for audit compliance
- **Advanced Querying**: Optimized database queries with JOIN operations

### 📞 **Contact Management**
- **Quick Transfers**: Save frequently used account numbers
- **Contact Search**: Filter and find contacts by name or account number
- **JSON Storage**: Lightweight file-based contact persistence
- **Duplicate Prevention**: Automatic validation to prevent duplicate entries

### 🔒 **Security Features**
- **Password Security**: SHA-256 hashing with unique salt per password
- **Database Security**: Foreign key constraints and connection validation
- **Transaction Safety**: Atomic operations preventing race conditions
- **Thread Safety**: Synchronized database operations
- **Input Validation**: Comprehensive validation and sanitization

---

## 🏗️ Architecture

The system follows a **4-tier layered architecture** with clear separation of concerns:


| Layer | Component 1  | Component 2 | Component 3        | Component 4    |
|-------|--------------|----------|--------------------|----------------|
| 🖥️ UI Layer | LoginWindow  | RegistrationWindow | MainWindow         | ContactPanel   |
| 🏢 Service Layer | LoginService | RegistrationService | MainService        | ContactService |
| 🏦 Model Layer | User         | Account  | Transaction        | Contact        |
| 📊 Data Layer | UserManager  | AccountManager | TransactionManager | ContactManager |

*In the data layer there is a generic DatabaseManager class which uses a thread-safe, singleton 
approach for secure database access.*

### **Design Patterns Used**
- **DAO Pattern**: Clean data access abstraction
- **Service Layer Pattern**: Business logic encapsulation
- **Singleton Pattern**: Database connection management
- **Result Pattern**: Consistent operation outcomes
- **MVC Pattern**: Clear separation of concerns

---

## 🚀 Getting Started

### **Prerequisites**
- **Java 22** or higher
- **Maven 3.6+** for dependency management
- **SQLite** (included via JDBC driver)

### **Installation**

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/banking-management-system.git
   cd banking-management-system
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn exec:java
   ```

### **Database Setup**
The application automatically creates the SQLite database (`config/Banking.db`) on first run with all necessary tables and constraints.

---

## 💻 Usage

### **First Time Setup**
1. **Launch the application** - The login window will appear
2. **Register a new account** - Click "Register" and fill in your details
3. **Login** - Use your registered email and password
4. **Create bank accounts** - Use the "Open Account" feature in the main window

### **Daily Operations**
- **Deposits**: Select account → Enter amount → Confirm
- **Withdrawals**: Select account → Enter amount → Confirm (validates sufficient balance)
- **Transfers**: Select source account → Enter recipient account number → Enter amount → Add comment
- **View History**: Select account → View transaction history with full details
- **Manage Contacts**: Add frequently used account numbers for quick transfers

### **Account Management**
- **Freeze Account**: Temporarily disable all operations for security
- **Unfreeze Account**: Re-enable account operations
- **Close Account**: Permanently close account (requires zero balance)

---

## 🛠️ Technology Stack

| Component | Technology    | Version  | Purpose |
|-----------|---------------|----------|---------|
| **Language** | Java          | 25       | Core application development |
| **Database** | SQLite        | 3.50.3.0 | Data persistence |
| **UI Framework** | JavaFX    | 25.0.1   | Modern user interface |
| **JSON Processing** | Gson          | 2.13.1   | Contact data serialization |
| **Testing** | JUnit Jupiter | 5.13.4   | Unit testing framework |
| **Build Tool** | Maven         | 3.6+     | Dependency management |

---

## 📚 Documentation

### **Package Documentation**
Comprehensive documentation is available for each package:

- **[📊 Data Package](src/main/java/banking/data/README.md)** - Database operations, security features, and data persistence
- **[🏦 Model Package](src/main/java/banking/model/README.md)** - Domain entities, relationships, and business rules  
- **[🏢 Service Package](src/main/java/banking/service/README.md)** - Business logic, validation, and workflow management

### **Key Classes**
- **DatabaseManager**: Thread-safe singleton for database connections
- **UserManager**: Secure user authentication with SHA-256 hashing
- **AccountManager**: Atomic financial operations with race condition prevention
- **TransactionManager**: Optimized transaction history with JOIN queries
- **MainService**: Complete banking operations with comprehensive validation

---

## 🔒 Security

### **Password Security**
- **SHA-256 Hashing**: Industry-standard cryptographic hashing
- **Unique Salt**: 16-byte cryptographically secure salt per password
- **Backward Compatibility**: Supports legacy password migration

### **Database Security**
- **Foreign Key Constraints**: Ensures referential integrity
- **Connection Validation**: Automatic health checks and recovery
- **Thread Safety**: Synchronized operations prevent race conditions
- **Atomic Transactions**: All-or-nothing operations for data consistency

### **Application Security**
- **Input Validation**: Comprehensive sanitization and validation
- **Session Management**: Secure user session handling
- **Account Controls**: Freeze/unfreeze capabilities for security incidents

---

## 🧪 Testing

The project includes comprehensive unit tests covering all critical functionality:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserManagerTest
mvn test -Dtest=AccountManagerTest
mvn test -Dtest=TransactionManagerTest
```

**Test Coverage**:
- ✅ User registration and authentication
- ✅ Account creation and management
- ✅ Financial operations (deposit, withdraw, transfer)
- ✅ Transaction history and persistence
- ✅ Database operations and error handling
