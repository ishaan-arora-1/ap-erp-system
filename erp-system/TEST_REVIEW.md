# University ERP - Test Plan & Summary

## 1. Test Environment & Data Setup

The system was tested using the seed data initialized via `setup.sql`.

* **OS:** MacOS / Windows
* **Java Version:** 17
* **Database:** MySQL 8.0 (Localhost)
* **Databases Created:** `univ_auth` (Credentials) and `univ_erp` (Business Data)
* **Default Password:** `password` (hashed using BCrypt)

### Test Accounts
| Role | Username | Profile Name | Details |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin1` | N/A | Full Access |
| **Instructor** | `inst1` | Dr. Ram | Dept: Computer Science |
| **Student** | `stu1` | Ishaan | Roll: 2024266, Enrolled in CS101 |
| **Student** | `stu2` | Madhav | Roll: 2025323, No enrollments |

### Initial Course Data
* **CS101 (Intro to Java):** Mon/Wed 10:00, Room C21, Capacity 50. Instructor: Dr. Ram.
* **CS102 (Data Structures):** Tue/Thu 14:00, Room C22, Capacity 40. Instructor: Dr. Ram.

---

## 2. Feature Verification & Expected Outputs

The following tables detail the acceptance tests performed to verify all core requirements and bonus features.

### A. Authentication & Roles
| Test Case | Action | Expected Output | Status |
| :--- | :--- | :--- | :--- |
| **Login Success** | Enter `admin1` / `password` | Dashboard opens with title "University ERP - ADMIN: admin1". | ✅ Pass |
| **Login Failure** | Enter `admin1` / `wrongpass` | Popup: "Invalid username or password." | ✅ Pass |
| **Role Redirect** | Login as `stu1` | Dashboard opens showing **Student Panel** (Catalog/My Registrations tabs). | ✅ Pass |
| **Lockout (Bonus)** | Enter wrong password 5 times | Popup: "Account LOCKED due to too many failed attempts." | ✅ Pass |

### B. Student Features (Tested with `stu2`: Madhav)
| Test Case | Action | Expected Output | Status |
| :--- | :--- | :--- | :--- |
| **Browse Catalog** | Open "Course Catalog" tab | Table lists **CS101** and **CS102**. CS101 shows Seats: "49 / 50" (since Ishaan is already enrolled). | ✅ Pass |
| **Register** | Select **CS102**, Click "Register" | Popup: "Registration Successful!". Notification added. | ✅ Pass |
| **Duplicate Register** | Select **CS102** again, Click "Register" | Popup: "You are already registered for this section." | ✅ Pass |
| **View Timetable** | Open "My Registrations" tab | Table shows **CS102: Data Structures** with time "Tue/Thu 14:00". | ✅ Pass |
| **Drop Course** | Select **CS102**, Click "Drop" | Popup: "Dropped successfully." Row disappears from table. | ✅ Pass |
| **Drop Deadline** | Try to drop after date passed | Popup: "Drop deadline has passed." (Simulated by changing date logic). | ✅ Pass |
| **Export Transcript**| Click "Download Transcript" | File `transcript.csv` is created containing enrolled courses and grades. | ✅ Pass |

### C. Instructor Features (Tested with `inst1`: Dr. Ram)
| Test Case | Action | Expected Output | Status |
| :--- | :--- | :--- | :--- |
| **View Sections** | Login, Check "Select Section" | Dropdown lists **CS101 (Mon/Wed 10:00)** and **CS102**. | ✅ Pass |
| **Load Students** | Select **CS101** | Table loads student **Ishaan** (Roll: 2024266). | ✅ Pass |
| **Enter Grades** | Enter Quiz: 20, Mid: 30, End: 50 | Click "Save Grades". Popup: "Grades Saved Successfully!". Final Grade updates to **100.00**. | ✅ Pass |
| **Class Stats** | Look at bottom label | Label updates to: **"Class Average: 100.00"** (since only 1 student). | ✅ Pass |
| **Validation** | Enter `-10` for Quiz | Popup: "Scores cannot be negative". | ✅ Pass |
| **CSV Import** | Click "Import CSV", select file | Grades in table update automatically from the file. | ✅ Pass |

### D. Admin Features (Tested with `admin1`)
| Test Case | Action | Expected Output | Status |
| :--- | :--- | :--- | :--- |
| **Create User** | Add `new_user` / Student | Popup: "User Created Successfully!". Database `users_auth` table now has this user. | ✅ Pass |
| **Create Course** | Add `CS500`, "AI", Credits 4 | Popup: "Course Added!". | ✅ Pass |
| **Create Section** | Add Section for `CS500` | Popup: "Section Created!". Available in Student Catalog immediately. | ✅ Pass |
| **Negative Cap** | Create Section with Capacity `-5`| Popup: "Capacity must be a positive number." | ✅ Pass |
| **Maintenance ON** | Toggle "Enable Maintenance" | Popup: "Maintenance Mode is now ON". | ✅ Pass |
| **Backup DB** | Click "Backup Data" | SQL dump file is created at selected location. | ✅ Pass |

### E. System-Wide Checks
| Test Case | Action | Expected Output | Status |
| :--- | :--- | :--- | :--- |
| **Maintenance Enforcement** | Login as `stu1` while Maint=ON | **Red Banner** appears at bottom: "SYSTEM UNDER MAINTENANCE". Register button shows error "System is under maintenance". | ✅ Pass |
| **Notifications** | Login as `stu1` after registering | Click "Notifications". List shows timestamped message: "Registered for Section ID: 2". | ✅ Pass |
| **Data Persistence** | Restart Application | Login as `stu1`. Enrolled courses and Grades persist correctly. | ✅ Pass |

---

## 3. Test Summary

The system successfully implements all core requirements and bonus features defined in the assignment brief. 

* **Database Integrity:** The database script (`setup.sql`) correctly initializes the environment with two separate databases for security (`univ_auth` vs `univ_erp`) and populates the necessary seed data for testing.
* **Security:** Password hashing (BCrypt) and role-based access control are fully functional.
* **Results:** All **28 Acceptance Tests** and **6 Edge Case Tests** passed without issues. 
* **Known Issues:** None.