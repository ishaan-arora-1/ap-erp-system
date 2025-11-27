# University ERP System

## Prerequisites
- Java 17+
- Maven
- MySQL Server 8.0+

## 1. Database Setup (CRITICAL)
Before running the application, you must initialize the database.

1.  Open your terminal or MySQL Workbench.
2.  Run the provided `setup.sql` script located in the project root.
    - **Command Line:** `mysql -u root -p < setup.sql`
    - **Workbench:** Open `setup.sql` and click the lightning bolt icon to run all.

**Default Credentials used in Code:**
- **User:** `root`
- **Password:** `Punya@52` (default fallback - see below for custom password instructions)

The script creates two databases (`univ_auth`, `univ_erp`) and inserts sample users:
- **Admin:** `admin1` / `password`
- **Instructor:** `inst1` / `password`
- **Student:** `stu1` / `password`

## 2. Environment Setup for Bonus Features
To use the **Backup & Restore** features, ensure that the MySQL tools are accessible:
- Add the path to `mysql` and `mysqldump` to your system's **PATH** environment variable.
- The application attempts to auto-detect them, but adding them to PATH ensures compatibility.

## 3. Running the Application
**From the project root (`ap-erp-system/`), navigate to the `erp-system` directory:**

### Option 1: Using Default Password (Punya@52)
```bash
cd erp-system
mvn clean compile
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main"
```

### Option 2: Using a Custom MySQL Password
If you have a different MySQL password (e.g., 'password' or empty), pass the `-Ddb.password` flag:

```bash
# Example: Password is "mySecretPass"
mvn clean compile
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password="mySecretPass"

# Example: No password (empty)
mvn clean compile
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password=""
```

**Note:** Ensure `mysql` and `mysqldump` are in your System PATH for Backup/Restore features.