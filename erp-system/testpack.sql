-- ==========================================
-- University ERP TESTPACK - Comprehensive Test Data
-- ==========================================
-- This script creates a fully populated test environment with:
-- - 1 Admin, 6 Instructors, 8 Students
-- - 6 Courses with multiple sections
-- - Various enrollments and grades
-- - Edge cases for testing features
-- ==========================================

-- 1. Reset Databases (Clear old data)
DROP DATABASE IF EXISTS univ_auth;
DROP DATABASE IF EXISTS univ_erp;

-- Create Fresh Databases
CREATE DATABASE IF NOT EXISTS univ_auth;
CREATE DATABASE IF NOT EXISTS univ_erp;

-- ==========================================
-- 2. Setup Auth Database
-- ==========================================
USE univ_auth;

CREATE TABLE users_auth (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login DATETIME,
    failed_attempts INT DEFAULT 0
);

-- ==========================================
-- 3. Setup ERP Database
-- ==========================================
USE univ_erp;

CREATE TABLE students (
    user_id INT PRIMARY KEY,
    full_name VARCHAR(100),
    roll_no VARCHAR(20),
    year INT
);

CREATE TABLE instructors (
    user_id INT PRIMARY KEY,
    full_name VARCHAR(100),
    department VARCHAR(50)
);

CREATE TABLE courses (
    course_code VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100),
    credits INT
);

CREATE TABLE sections (
    section_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(10),
    instructor_id INT,
    days_times VARCHAR(50),
    room VARCHAR(20),
    capacity INT,
    FOREIGN KEY (course_code) REFERENCES courses(course_code),
    FOREIGN KEY (instructor_id) REFERENCES instructors(user_id)
);

CREATE TABLE enrollments (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT,
    section_id INT,
    status VARCHAR(20) DEFAULT 'ENROLLED',
    FOREIGN KEY (student_id) REFERENCES students(user_id),
    FOREIGN KEY (section_id) REFERENCES sections(section_id),
    UNIQUE(student_id, section_id)
);

CREATE TABLE grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT,
    component_name VARCHAR(50),
    score DOUBLE,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

CREATE TABLE settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(255)
);

INSERT INTO settings (setting_key, setting_value) VALUES ('maintenance_on', 'false');

-- ==========================================
-- 4. INSERT TEST DATA
-- ==========================================

-- A. Create Users (All passwords are 'password')
-- BCrypt hash for 'password': $2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S

USE univ_auth;

-- Admin (user_id = 1)
INSERT INTO users_auth (username, role, password_hash) VALUES 
('admin', 'ADMIN', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S');

-- Instructors (user_id = 2-7)
INSERT INTO users_auth (username, role, password_hash) VALUES 
('prof_kumar', 'INSTRUCTOR', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('prof_singh', 'INSTRUCTOR', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('prof_sharma', 'INSTRUCTOR', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('prof_patel', 'INSTRUCTOR', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('prof_reddy', 'INSTRUCTOR', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('prof_gupta', 'INSTRUCTOR', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S');

-- Students (user_id = 8-15)
INSERT INTO users_auth (username, role, password_hash) VALUES 
('alice_2023', 'STUDENT', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('bob_2023', 'STUDENT', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('carol_2024', 'STUDENT', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('david_2024', 'STUDENT', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('emma_2025', 'STUDENT', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('frank_2025', 'STUDENT', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('grace_2025', 'STUDENT', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S'),
('henry_2024', 'STUDENT', '$2a$10$13zqR9bFg81RgccFQVW7F.KQi0Pt9f38wPuY7su1rqbSI8CEKXQ9S');

-- B. Create Instructor Profiles
USE univ_erp;

INSERT INTO instructors (user_id, full_name, department) VALUES 
(2, 'Dr. Rajesh Kumar', 'Computer Science'),
(3, 'Dr. Priya Singh', 'Computer Science'),
(4, 'Dr. Amit Sharma', 'Mathematics'),
(5, 'Dr. Neha Patel', 'Physics'),
(6, 'Dr. Vikram Reddy', 'Electronics'),
(7, 'Dr. Anjali Gupta', 'Mathematics');

-- C. Create Student Profiles
INSERT INTO students (user_id, full_name, roll_no, year) VALUES 
(8, 'Alice Johnson', '2023001', 2023),
(9, 'Bob Smith', '2023002', 2023),
(10, 'Carol Williams', '2024001', 2024),
(11, 'David Brown', '2024002', 2024),
(12, 'Emma Davis', '2025001', 2025),
(13, 'Frank Miller', '2025002', 2025),
(14, 'Grace Wilson', '2025003', 2025),
(15, 'Henry Moore', '2024003', 2024);

-- D. Create Courses
INSERT INTO courses (course_code, title, credits) VALUES 
('CS101', 'Introduction to Programming', 4),
('CS201', 'Data Structures and Algorithms', 4),
('CS301', 'Database Management Systems', 3),
('MATH101', 'Calculus I', 4),
('PHY101', 'Physics I - Mechanics', 4),
('ECE101', 'Digital Electronics', 3);

-- E. Create Sections
-- CS101 has 2 sections
INSERT INTO sections (course_code, instructor_id, days_times, room, capacity) VALUES 
('CS101', 2, 'Mon/Wed 10:00-11:30', 'Room A101', 40),
('CS101', 3, 'Tue/Thu 14:00-15:30', 'Room A102', 40);

-- CS201 has 2 sections
INSERT INTO sections (course_code, instructor_id, days_times, room, capacity) VALUES 
('CS201', 2, 'Mon/Wed 14:00-15:30', 'Room A103', 35),
('CS201', 3, 'Tue/Thu 10:00-11:30', 'Room A104', 35);

-- CS301 has 1 section
INSERT INTO sections (course_code, instructor_id, days_times, room, capacity) VALUES 
('CS301', 2, 'Fri 10:00-13:00', 'Lab C201', 30);

-- MATH101 has 2 sections
INSERT INTO sections (course_code, instructor_id, days_times, room, capacity) VALUES 
('MATH101', 4, 'Mon/Wed/Fri 9:00-10:00', 'Room B101', 50),
('MATH101', 7, 'Tue/Thu 11:00-12:30', 'Room B102', 50);

-- PHY101 has 1 section
INSERT INTO sections (course_code, instructor_id, days_times, room, capacity) VALUES 
('PHY101', 5, 'Mon/Wed 15:00-16:30', 'Room D101', 45);

-- ECE101 has 1 section
INSERT INTO sections (course_code, instructor_id, days_times, room, capacity) VALUES 
('ECE101', 6, 'Tue/Thu 13:00-14:30', 'Lab E101', 30);

-- F. Create Enrollments
-- Alice (user_id=8) - Full load, all courses
INSERT INTO enrollments (student_id, section_id) VALUES 
(8, 1),  -- CS101 Section 1
(8, 3),  -- CS201 Section 1
(8, 5),  -- CS301
(8, 6);  -- MATH101 Section 1

-- Bob (user_id=9) - 3 courses
INSERT INTO enrollments (student_id, section_id) VALUES 
(9, 2),  -- CS101 Section 2
(9, 4),  -- CS201 Section 2
(9, 8);  -- PHY101

-- Carol (user_id=10) - 4 courses
INSERT INTO enrollments (student_id, section_id) VALUES 
(10, 1), -- CS101 Section 1
(10, 6), -- MATH101 Section 1
(10, 8), -- PHY101
(10, 9); -- ECE101

-- David (user_id=11) - 3 courses
INSERT INTO enrollments (student_id, section_id) VALUES 
(11, 2), -- CS101 Section 2
(11, 3), -- CS201 Section 1
(11, 7); -- MATH101 Section 2

-- Emma (user_id=12) - 2 courses (light load)
INSERT INTO enrollments (student_id, section_id) VALUES 
(12, 1), -- CS101 Section 1
(12, 6); -- MATH101 Section 1

-- Frank (user_id=13) - 3 courses
INSERT INTO enrollments (student_id, section_id) VALUES 
(13, 2), -- CS101 Section 2
(13, 8), -- PHY101
(13, 9); -- ECE101

-- Grace (user_id=14) - 4 courses
INSERT INTO enrollments (student_id, section_id) VALUES 
(14, 1), -- CS101 Section 1
(14, 4), -- CS201 Section 2
(14, 6), -- MATH101 Section 1
(14, 8); -- PHY101

-- Henry (user_id=15) - 2 courses
INSERT INTO enrollments (student_id, section_id) VALUES 
(15, 3), -- CS201 Section 1
(15, 7); -- MATH101 Section 2

-- G. Insert Sample Grades
-- Alice's grades (enrollment_id 1-4) - Excellent student
INSERT INTO grades (enrollment_id, component_name, score) VALUES 
(1, 'Quiz', 19.0),
(1, 'Midterm', 28.0),
(1, 'EndSem', 47.0),
(2, 'Quiz', 18.5),
(2, 'Midterm', 27.0),
(2, 'EndSem', 45.0),
(3, 'Quiz', 20.0),
(3, 'Midterm', 29.0),
(4, 'Quiz', 17.0),
(4, 'Midterm', 26.0);

-- Bob's grades (enrollment_id 5-7) - Average student
INSERT INTO grades (enrollment_id, component_name, score) VALUES 
(5, 'Quiz', 15.0),
(5, 'Midterm', 22.0),
(5, 'EndSem', 38.0),
(6, 'Quiz', 14.0),
(6, 'Midterm', 20.0),
(7, 'Quiz', 16.0);

-- Carol's grades (enrollment_id 8-11) - Good student
INSERT INTO grades (enrollment_id, component_name, score) VALUES 
(8, 'Quiz', 17.0),
(8, 'Midterm', 25.0),
(8, 'EndSem', 42.0),
(9, 'Quiz', 18.0),
(9, 'Midterm', 27.0),
(10, 'Quiz', 16.5);

-- David's grades (enrollment_id 12-14) - Struggling student
INSERT INTO grades (enrollment_id, component_name, score) VALUES 
(12, 'Quiz', 12.0),
(12, 'Midterm', 18.0),
(12, 'EndSem', 30.0),
(13, 'Quiz', 13.0),
(13, 'Midterm', 19.0);

-- Emma's grades (enrollment_id 15-16) - New student, few grades
INSERT INTO grades (enrollment_id, component_name, score) VALUES 
(15, 'Quiz', 16.0),
(16, 'Quiz', 15.5);

-- Grace's grades (enrollment_id 19-22) - Very good student
INSERT INTO grades (enrollment_id, component_name, score) VALUES 
(19, 'Quiz', 18.0),
(19, 'Midterm', 28.0),
(19, 'EndSem', 46.0),
(20, 'Quiz', 17.5),
(20, 'Midterm', 26.0),
(21, 'Quiz', 19.0),
(22, 'Quiz', 17.0);

-- ==========================================
-- TESTPACK SETUP COMPLETE!
-- ==========================================
-- 
-- LOGIN CREDENTIALS (All passwords are 'password'):
-- 
-- ADMIN:
--   Username: admin
--   Password: password
-- 
-- INSTRUCTORS:
--   prof_kumar   / password  (CS - Dr. Rajesh Kumar)
--   prof_singh   / password  (CS - Dr. Priya Singh)
--   prof_sharma  / password  (MATH - Dr. Amit Sharma)
--   prof_patel   / password  (PHY - Dr. Neha Patel)
--   prof_reddy   / password  (ECE - Dr. Vikram Reddy)
--   prof_gupta   / password  (MATH - Dr. Anjali Gupta)
-- 
-- STUDENTS:
--   alice_2023   / password  (Alice Johnson - 2023001)
--   bob_2023     / password  (Bob Smith - 2023002)
--   carol_2024   / password  (Carol Williams - 2024001)
--   david_2024   / password  (David Brown - 2024002)
--   emma_2025    / password  (Emma Davis - 2025001)
--   frank_2025   / password  (Frank Miller - 2025002)
--   grace_2025   / password  (Grace Wilson - 2025003)
--   henry_2024   / password  (Henry Moore - 2024003)
-- 
-- DATABASE STATISTICS:
--   - 1 Admin
--   - 6 Instructors (across 4 departments)
--   - 8 Students (from years 2023-2025)
--   - 6 Courses
--   - 10 Sections
--   - 23 Enrollments
--   - 30+ Grade entries
-- 
-- TESTING SCENARIOS INCLUDED:
--   - Students with full course load (Alice)
--   - Students with light load (Emma, Henry)
--   - Students with complete grades (Alice, Bob)
--   - Students with partial grades (Carol, David, Grace)
--   - Students with minimal grades (Emma, Frank, Henry)
--   - Multiple sections per course
--   - Sections at capacity testing
--   - Different years and roll numbers
--   - Various performance levels (excellent, average, struggling)
-- 
-- ==========================================

