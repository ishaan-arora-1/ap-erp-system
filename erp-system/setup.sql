-- ==========================================
-- University ERP Setup Script
-- ==========================================

-- 1. Create Databases
CREATE DATABASE IF NOT EXISTS univ_auth;
CREATE DATABASE IF NOT EXISTS univ_erp;

-- ==========================================
-- 2. Setup Auth Database
-- ==========================================
USE univ_auth;

DROP TABLE IF EXISTS users_auth;
CREATE TABLE users_auth (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'ADMIN', 'INSTRUCTOR', 'STUDENT'
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login DATETIME,
    failed_attempts INT DEFAULT 0
);

-- ==========================================
-- 3. Setup ERP Database
-- ==========================================
USE univ_erp;

-- Drop tables in reverse order of dependencies
DROP TABLE IF EXISTS grades;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS sections;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS instructors;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS settings;

-- Create Tables
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
    UNIQUE(student_id, section_id) -- Prevent duplicate enrollments at DB level
);

CREATE TABLE grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id INT,
    component_name VARCHAR(50), -- 'Quiz', 'Midterm', 'EndSem'
    score DOUBLE,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments(enrollment_id)
);

CREATE TABLE settings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(255)
);

-- Initialize Settings
INSERT INTO settings (setting_key, setting_value) VALUES ('maintenance_on', 'false');

-- ==========================================
-- 4. Insert Sample Data (Seed)
-- ==========================================

-- A. Users (Password is 'password' for everyone)
-- Hash: $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRkgVduWkyvM7qmIv2p8e.0TbK2 is BCrypt hash for 'password'
USE univ_auth;
INSERT INTO users_auth (username, role, password_hash) VALUES 
('admin1', 'ADMIN', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRkgVduWkyvM7qmIv2p8e.0TbK2'),
('inst1', 'INSTRUCTOR', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRkgVduWkyvM7qmIv2p8e.0TbK2'),
('stu1', 'STUDENT', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRkgVduWkyvM7qmIv2p8e.0TbK2'),
('stu2', 'STUDENT', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRkgVduWkyvM7qmIv2p8e.0TbK2');

-- NOTE: All users have password 'password' (without quotes)
-- If you need to generate a new hash, use: BCrypt.hashpw("password", BCrypt.gensalt())

-- B. Profiles (Link to Auth IDs)
-- Assuming IDs are 1=admin, 2=inst1, 3=stu1, 4=stu2
USE univ_erp;

INSERT INTO instructors (user_id, full_name, department) VALUES 
(2, 'Dr. Alice Smith', 'Computer Science');

INSERT INTO students (user_id, full_name, roll_no, year) VALUES 
(3, 'Bob Jones', '2023001', 2023),
(4, 'Charlie Brown', '2023002', 2023);

-- C. Courses & Sections
INSERT INTO courses (course_code, title, credits) VALUES 
('CS101', 'Intro to Java', 4),
('CS102', 'Data Structures', 4);

INSERT INTO sections (course_code, instructor_id, days_times, room, capacity) VALUES 
('CS101', 2, 'Mon/Wed 10:00', 'C21', 50),
('CS102', 2, 'Tue/Thu 14:00', 'C22', 40);

-- D. Enrollments
-- Enroll stu1 (user_id=3) into CS101 (section_id=1)
INSERT INTO enrollments (student_id, section_id) VALUES (3, 1);

-- ==========================================
-- Setup Complete!
-- ==========================================
-- Default Login Credentials:
--   Admin:      username='admin1'  password='password'
--   Instructor: username='inst1'   password='password'
--   Student 1:  username='stu1'    password='password'
--   Student 2:  username='stu2'    password='password'
-- ==========================================

