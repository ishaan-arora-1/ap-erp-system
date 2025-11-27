# University ERP System - Setup Guide

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17 or higher** - [Download here](https://www.oracle.com/java/technologies/downloads/)
- **Apache Maven 3.6+** - [Download here](https://maven.apache.org/download.cgi)
- **MySQL Server 8.0+** - [Download here](https://dev.mysql.com/downloads/mysql/)

### Verify Prerequisites

```bash
# Check Java version (should be 17+)
java -version

# Check Maven version (should be 3.6+)
mvn -version

# Check MySQL is running
mysql --version
```

---

## Step 1: Clone and Navigate to Project

```bash
cd /path/to/ap-erp-system/erp-system
```

---

## Step 2: Database Setup (REQUIRED)

⚠️ **IMPORTANT**: You MUST complete this step before running the application!

### 2.1: Start MySQL Server

Make sure your MySQL server is running on `localhost:3306`.

### 2.2: Choose Your Database Setup

You have **two options** for setting up the database:

#### Option 1: Basic Setup (Recommended for First-Time Users)

The `setup.sql` script provides minimal data for quick testing:
- 1 admin, 1 instructor, 2 students
- 2 courses with 2 sections
- 1 sample enrollment

**Run Basic Setup:**

```bash
# Using Command Line
mysql -u root -p < setup.sql
```

Or in **MySQL Workbench**: Open `setup.sql` → Click ⚡ to execute

#### Option 2: Test Pack (Recommended for Comprehensive Testing)

The `testpack.sql` script provides a fully populated test environment:
- 1 admin, 6 instructors, 8 students
- 6 courses with 10 sections
- 23 enrollments with various grades
- Multiple departments and edge cases

**Run Test Pack:**

```bash
# Using Command Line
mysql -u root -p < testpack.sql
```

Or in **MySQL Workbench**: Open `testpack.sql` → Click ⚡ to execute

📖 **For detailed information about the test pack**, including all credentials and testing scenarios, see **`TESTPACK.md`**

⚠️ **Note**: Running either script will **drop and recreate** the databases, deleting any existing data!

### 2.3: Verify Database Setup

```bash
mysql -u root -p -e "SHOW DATABASES LIKE 'univ_%';"
```

You should see:
```
univ_auth
univ_erp
```

---

## Step 3: Build the Application

```bash
# From the erp-system directory
mvn clean compile
```

This will download all dependencies and compile the Java code.

---

## Step 4: Run the Application

⚠️ **IMPORTANT**: Replace `YOUR_MYSQL_PASSWORD` with your actual MySQL root password!

### If your MySQL root password is set:

```bash
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password="YOUR_MYSQL_PASSWORD"
```

**Examples:**
```bash
# If your password is "password"
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password="password"

# If your password is "root123"
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password="root123"

# If your password is "Punya@52" (the default fallback in code)
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password="Punya@52"
```

### If your MySQL root has NO password (empty password):

```bash
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password=""
```

---

## Step 5: Login to the Application

Once the application window opens, use these **default credentials**:

### If you used `setup.sql` (Basic Setup):

| Role       | Username | Password   |
|------------|----------|------------|
| Admin      | admin1   | password   |
| Instructor | inst1    | password   |
| Student    | stu1     | password   |
| Student    | stu2     | password   |

### If you used `testpack.sql` (Test Pack):

| Role       | Example Username | Password   |
|------------|------------------|------------|
| Admin      | admin            | password   |
| Instructor | prof_kumar       | password   |
| Instructor | prof_singh       | password   |
| Student    | alice_2023       | password   |
| Student    | bob_2023         | password   |

📖 **See `TESTPACK.md` for all 15 user accounts and detailed testing instructions**

---

## Optional: Enable Backup & Restore Features

The admin panel includes database backup and restore functionality. For these features to work:

### Windows:
Add MySQL bin directory to your PATH:
```
C:\Program Files\MySQL\MySQL Server 8.0\bin
```

### macOS/Linux:
MySQL tools are usually already in PATH. If not, add to your `.bashrc` or `.zshrc`:
```bash
export PATH=$PATH:/usr/local/mysql/bin
```

Verify:
```bash
which mysqldump
which mysql
```

---

## Troubleshooting

### Error: "Failed to initialize database connections"

**Cause**: Cannot connect to MySQL database.

**Solutions**:
1. Verify MySQL is running: `mysql -u root -p`
2. Check that you ran `setup.sql` to create the databases
3. Verify you're using the correct password in `-Ddb.password="..."`
4. Ensure MySQL is running on `localhost:3306`

### Error: "Access denied for user 'root'@'localhost'"

**Cause**: Wrong MySQL password.

**Solution**: Use the correct password in the `-Ddb.password` flag:
```bash
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password="YOUR_ACTUAL_PASSWORD"
```

### Error: "Unknown database 'univ_auth'" or "Unknown database 'univ_erp'"

**Cause**: Setup script not run.

**Solution**: Run the setup script:
```bash
mysql -u root -p < setup.sql
```

### Application builds but doesn't start

**Cause**: Database connection fails during startup.

**Solution**: 
1. Check MySQL is running
2. Verify databases exist: `mysql -u root -p -e "SHOW DATABASES;"`
3. Check password is correct in the run command

---

## Quick Start Summary

### Option 1: Basic Setup (Minimal Data)
```bash
# 1. Navigate to project
cd /path/to/ap-erp-system/erp-system

# 2. Setup databases (basic)
mysql -u root -p < setup.sql

# 3. Build project
mvn clean compile

# 4. Run application (replace with your MySQL password)
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password="YOUR_PASSWORD"

# 5. Login with: admin1 / password (or inst1, stu1, stu2)
```

### Option 2: Test Pack (Full Test Data)
```bash
# 1. Navigate to project
cd /path/to/ap-erp-system/erp-system

# 2. Setup databases (test pack)
mysql -u root -p < testpack.sql

# 3. Build project
mvn clean compile

# 4. Run application (replace with your MySQL password)
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password="YOUR_PASSWORD"

# 5. Login with: admin / password (or prof_kumar, alice_2023, etc.)
# 6. See TESTPACK.md for all 15 accounts and testing guide
```
