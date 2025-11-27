# University ERP System - Test Pack Guide

## What is the Test Pack?

The **Test Pack** (`testpack.sql`) is a comprehensive database setup script designed for thorough testing of all system features. Unlike the minimal `setup.sql`, the test pack provides a fully populated environment with realistic data.

---

## Test Pack Contents

### Users (Total: 15)

**1 Administrator:**
- `admin` / `password`

**6 Instructors (Multiple Departments):**
- `prof_kumar` / `password` - Dr. Rajesh Kumar (Computer Science)
- `prof_singh` / `password` - Dr. Priya Singh (Computer Science)
- `prof_sharma` / `password` - Dr. Amit Sharma (Mathematics)
- `prof_patel` / `password` - Dr. Neha Patel (Physics)
- `prof_reddy` / `password` - Dr. Vikram Reddy (Electronics)
- `prof_gupta` / `password` - Dr. Anjali Gupta (Mathematics)

**8 Students (Various Years):**
- `alice_2023` / `password` - Alice Johnson (Roll: 2023001, Year: 2023)
- `bob_2023` / `password` - Bob Smith (Roll: 2023002, Year: 2023)
- `carol_2024` / `password` - Carol Williams (Roll: 2024001, Year: 2024)
- `david_2024` / `password` - David Brown (Roll: 2024002, Year: 2024)
- `emma_2025` / `password` - Emma Davis (Roll: 2025001, Year: 2025)
- `frank_2025` / `password` - Frank Miller (Roll: 2025002, Year: 2025)
- `grace_2025` / `password` - Grace Wilson (Roll: 2025003, Year: 2025)
- `henry_2024` / `password` - Henry Moore (Roll: 2024003, Year: 2024)

### Academic Data

**6 Courses:**
1. CS101 - Introduction to Programming (4 credits)
2. CS201 - Data Structures and Algorithms (4 credits)
3. CS301 - Database Management Systems (3 credits)
4. MATH101 - Calculus I (4 credits)
5. PHY101 - Physics I - Mechanics (4 credits)
6. ECE101 - Digital Electronics (3 credits)

**10 Course Sections:**
- Multiple sections for popular courses (CS101, CS201, MATH101)
- Different instructors for different sections
- Various schedules and room assignments
- Realistic capacity limits (30-50 students)

**23 Enrollments:**
- Students enrolled in 2-4 courses each
- Mix of full and light course loads
- Realistic distribution across sections

**30+ Grade Entries:**
- Complete grades (Quiz, Midterm, EndSem) for some students
- Partial grades (Quiz, Midterm only) for others
- Minimal grades (Quiz only) for new enrollments
- Performance variety: excellent, average, struggling students

---

## How to Use the Test Pack

### Step 1: Run the Test Pack Script

**Command Line:**
```bash
# From the erp-system directory
mysql -u root -p < testpack.sql
```

**MySQL Workbench:**
1. Open MySQL Workbench
2. Connect to your local MySQL server
3. Open `testpack.sql`
4. Click the lightning bolt icon (⚡) to execute
5. Verify `univ_auth` and `univ_erp` databases are created

### Step 2: Run the Application

```bash
# Build the application
mvn clean compile

# Run with your MySQL password
mvn exec:java -Dexec.mainClass="edu.univ.erp.Main" -Ddb.password="YOUR_PASSWORD"
```

### Step 3: Start Testing!

Login with any of the credentials above and explore the system.

---

## What to Test with This Pack

### Admin Testing (`admin` / `password`)

✅ **User Management:**
- Create new users (students, instructors)
- View existing users from different departments

✅ **Course Management:**
- Add new courses
- View existing 6 courses

✅ **Section Management:**
- Create new sections
- View existing sections with different instructors
- Test capacity limits (try to enroll beyond capacity)

✅ **System Settings:**
- Toggle maintenance mode
- Test backup and restore functionality

### Instructor Testing (Any `prof_*` account)

✅ **Section Management:**
- View assigned sections
- Switch between multiple sections (if applicable)
- View class lists with multiple students

✅ **Grade Management:**
- Enter grades for students with no grades
- Update existing grades (Alice, Bob, Grace have full grades)
- Test grade validation (negative numbers, exceeding max)
- View class averages with complete data

✅ **CSV Import:**
- Export student list
- Prepare CSV with grades
- Import grades in bulk
- Verify calculations

**Recommended Test Accounts:**
- `prof_kumar` - Has 3 sections (CS101-1, CS201-1, CS301) with multiple students
- `prof_singh` - Has 2 sections (CS101-2, CS201-2) with good distribution

### Student Testing (Any `*_20XX` account)

✅ **Course Catalog:**
- Browse available courses and sections
- View seat availability
- Test sorting and filtering

✅ **Registration:**
- Register for new sections
- Test duplicate registration prevention
- Test capacity limits
- Check notifications after registration

✅ **My Registrations:**
- View enrolled sections
- Check grades (varying levels of completion)
- Test drop functionality
- Test drop deadline validation

✅ **Transcript:**
- Download transcript
- Verify grade calculations
- Test with complete vs partial grades

**Recommended Test Accounts:**
- `alice_2023` - 4 courses, complete grades (excellent performance)
- `bob_2023` - 3 courses, complete grades (average performance)
- `emma_2025` - 2 courses, minimal grades (new student)
- `david_2024` - 3 courses, struggling grades

---

## Testing Scenarios by Feature

### 🧪 Registration System

| Test Case | Use Account | Expected Result |
|-----------|-------------|-----------------|
| Register for available course | `henry_2024` (only 2 courses) | Success, notification sent |
| Register for duplicate course | `alice_2023` (try CS101 again) | Error: already registered |
| Register for full section | Check section capacity first | Error: section is full |
| Drop before deadline | Any student with enrollments | Success, grades deleted |
| Drop after deadline | Any student (deadline Dec 31, 2025) | Error or success based on date |

### 📊 Grading System

| Test Case | Use Account | Expected Result |
|-----------|-------------|-----------------|
| Enter grades for new enrollment | `prof_kumar` - Section CS301 | Success, saved to database |
| Update existing grades | `prof_kumar` - Alice's grades | Success, grades updated |
| Negative grade validation | Any instructor | Error: cannot be negative |
| Exceeding max score | Any instructor | Error: exceeds maximum |
| Class average calculation | `prof_kumar` - CS101 section | Shows average based on enrolled students |
| CSV import | `prof_singh` | Bulk grades imported successfully |

### 👨‍💼 Admin Functions

| Test Case | Use Account | Expected Result |
|-----------|-------------|-----------------|
| Create instructor | `admin` | New instructor added to instructors table |
| Create student | `admin` | New student added to students table |
| Create course with sections | `admin` | Course and section created successfully |
| Maintenance mode toggle | `admin` | System locks for non-admins |
| Database backup | `admin` | SQL dump file created |
| Database restore | `admin` | Data restored from backup file |

### 🔐 Security & Access Control

| Test Case | Use Account | Expected Result |
|-----------|-------------|-----------------|
| Maintenance mode as student | `alice_2023` (after admin enables) | Read-only, no registrations |
| Maintenance mode as instructor | `prof_kumar` (after admin enables) | No grade modifications allowed |
| Access other instructor's section | `prof_sharma` try CS101 | Error: access denied |
| Failed login attempts | Create bad password attempts | Account locked after 5 failures |
| Password change | Any account | Password updated successfully |

### 📈 Edge Cases

| Test Case | Use Account | Expected Result |
|-----------|-------------|-----------------|
| Empty grade components | `emma_2025` (only Quiz grades) | Final grade calculated with 0 for missing |
| No enrollments in section | Create new section | Empty class list, average N/A |
| Student with no courses | Create new student account | Empty registration list |
| Transcript with incomplete grades | `carol_2024` | Shows all courses with calculated grades |

---

## Database Statistics

After running `testpack.sql`, your database will contain:

```
Users:           15 total (1 admin, 6 instructors, 8 students)
Departments:     4 (Computer Science, Mathematics, Physics, Electronics)
Courses:         6 courses
Sections:        10 sections
Enrollments:     23 student enrollments
Grade Records:   30+ individual grade entries
Years:           2023, 2024, 2025
```

---

## Switching Between Setup and Test Pack

### To Use Basic Setup (Minimal Data):
```bash
mysql -u root -p < setup.sql
```

### To Use Test Pack (Full Data):
```bash
mysql -u root -p < testpack.sql
```

⚠️ **Warning**: Running either script will **DROP and recreate** the databases, deleting all existing data!

---

## Tips for Effective Testing

1. **Start with Admin** - Create additional test data if needed
2. **Test Each Role** - Login as different instructors and students
3. **Check Edge Cases** - Try to break the system with invalid inputs
4. **Test Workflows** - Complete end-to-end scenarios (register → enroll → grade → transcript)
5. **Monitor Notifications** - Check if notifications are sent properly
6. **Validate Calculations** - Verify grade calculations (20% + 30% + 50%)
7. **Test Concurrency** - Have multiple users try to register for the last seat
8. **Backup/Restore** - Test the backup feature before needing it in production

---

## Quick Reference - Sample Workflows

### Workflow 1: Complete Student Journey
```
1. Login as: emma_2025 / password
2. Browse course catalog
3. Register for PHY101 (section 8)
4. View my registrations
5. Logout

6. Login as: prof_patel / password
7. Select PHY101 section
8. Find Emma in class list
9. Enter grades: Quiz=16, Midterm=24, EndSem=40
10. Save grades

11. Login as: emma_2025 / password
12. View grades in PHY101
13. Download transcript
```

### Workflow 2: Instructor Grade Management
```
1. Login as: prof_kumar / password
2. Select CS101 Section 1
3. View class list (Alice, Carol, Emma, Grace)
4. Note existing grades vs missing grades
5. Update Grace's EndSem grade
6. Check class average updates
7. Export CSV
8. Modify CSV with new grades
9. Import CSV
10. Verify changes
```

### Workflow 3: Admin Operations
```
1. Login as: admin / password
2. Create new instructor: prof_new / CS Dept
3. Create new course: CS401 / Advanced Algorithms / 4 credits
4. Create section: CS401 with prof_new
5. Test backup database
6. Toggle maintenance mode ON
7. Logout and verify students see banner
8. Login as admin, toggle maintenance OFF
```

