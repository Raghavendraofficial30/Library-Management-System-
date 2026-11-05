# Library Management System (Console-based Core Java & JDBC Project)

A complete console-based Library Management System developed with **Core Java**, **JDBC**, and **MySQL**.

---

## Project Structure

```
LibraryManagementSystem/
├── sql/
│   └── schema.sql                  # MySQL database & relational tables with Foreign Keys
├── lib/
│   └── mysql-connector-j-8.x.xx.jar # MySQL JDBC Driver
└── src/
    └── com/
        └── lms/
            ├── model/
            │   ├── Book.java        # Book entity
            │   ├── Member.java      # Member entity
            │   └── IssueRecord.java # Book issuance & return record
            ├── util/
            │   └── DBConnection.java# JDBC connection utility
            ├── dao/
            │   ├── BookDAO.java     # Book CRUD and search operations
            │   ├── MemberDAO.java   # Member CRUD and lookup operations
            │   └── IssueDAO.java    # Book issue, return, and transaction logic
            └── main/
                └── Main.java        # Menu-driven console UI and validation
```

---

## Features

1. **Book Management**:
   - Add new books with ID, title, author, category, and quantity.
   - Update existing book details.
   - Delete book by ID.
   - Search books by title, author, or category keyword.
   - View all books in inventory.

2. **Member Management**:
   - Register members with unique ID, name, unique email, and phone number.
   - Update member information.
   - Delete member by ID.
   - Search and list members.

3. **Issue a Book**:
   - Out-of-stock check: Prevents issuing if available quantity is 0.
   - Prevents issuing duplicate active copies to the same member.
   - Database transactions: Atomically inserts issue record and decrements book stock.

4. **Return a Book & Fine Calculation**:
   - 7 days allowed lending period.
   - Automatically calculates late fee of ₹5/day for days beyond 7 days.
   - Restores book inventory quantity atomically.

5. **Reports & Status Tracking**:
   - View currently issued books, members who borrowed them, issue date, and return status.

---

## Step-by-Step Setup Guide

### 1. Database Setup
Run the SQL script [`sql/schema.sql`](file:///c:/Users/RAGAVENDRA%20%20A%20S/OneDrive/Raghavendra%20A%20S/LibraryManagementSystem/sql/schema.sql) in MySQL Workbench or MySQL Command Line:
```sql
SOURCE sql/schema.sql;
```

### 2. Configure Database Credentials
Open [`src/com/lms/util/DBConnection.java`](file:///c:/Users/RAGAVENDRA%20%20A%20S/OneDrive/Raghavendra%20A%20S/LibraryManagementSystem/src/com/lms/util/DBConnection.java) and enter your MySQL password:
```java
private static final String USERNAME = "root";
private static final String PASSWORD = "your_mysql_password";
```

### 3. Add MySQL Driver
Place `mysql-connector-j-8.x.xx.jar` in the `LibraryManagementSystem/lib/` folder.

---

## Compilation & Execution

### Windows (PowerShell / Command Prompt):
```powershell
cd LibraryManagementSystem
mkdir bin

# Compile all classes
javac -cp "lib/*" -d bin src/com/lms/model/*.java src/com/lms/util/*.java src/com/lms/dao/*.java src/com/lms/main/*.java

# Run application
java -cp "bin;lib/*" com.lms.main.Main
```

### Linux / macOS:
```bash
cd LibraryManagementSystem
mkdir -p bin

# Compile
javac -cp "lib/*" -d bin src/com/lms/model/*.java src/com/lms/util/*.java src/com/lms/dao/*.java src/com/lms/main/*.java

# Run
java -cp "bin:lib/*" com.lms.main.Main
```

