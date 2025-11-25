# ERP System

## Prerequisites

- Java 17+
- Maven

## Running the Application

**From the project root (`ap-erp-system/`), navigate to the `erp-system` directory:**

```bash
cd erp-system
mvn clean compile
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main"
```

**Or run directly from `erp-system/` directory:**

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main"
```

Please ensure that 'mysql' and 'mysqldump' are added to your system's PATH environment variable for the Backup/Restore features to work.