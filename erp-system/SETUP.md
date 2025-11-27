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
- **Password:** `Punya@52` (See `src/main/java/edu/univ/erp/data/DatabaseFactory.java` to change this if needed).

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

```bash
cd erp-system
mvn clean compile
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main"