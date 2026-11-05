-- ===================================================
-- Library Management System Database Setup Script
-- ===================================================

-- Create database if it doesn't already exist
CREATE DATABASE IF NOT EXISTS library_management_db;

-- Switch to library_management_db
USE library_management_db;

-- 1. Create books table
CREATE TABLE IF NOT EXISTS books (
    id INT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    author VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity >= 0)
);

-- 2. Create members table
CREATE TABLE IF NOT EXISTS members (
    id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL
);

-- 3. Create issue_records table with Foreign Keys
CREATE TABLE IF NOT EXISTS issue_records (
    issue_id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    member_id INT NOT NULL,
    issue_date DATE NOT NULL,
    return_date DATE DEFAULT NULL,
    fine DOUBLE DEFAULT 0.0,
    status VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
    CONSTRAINT fk_issue_book FOREIGN KEY (book_id) 
        REFERENCES books(id) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_issue_member FOREIGN KEY (member_id) 
        REFERENCES members(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- ===================================================
-- Insert Sample Records for Testing
-- ===================================================

-- Sample Books
INSERT INTO books (id, title, author, category, quantity) VALUES
(101, 'Effective Java', 'Joshua Bloch', 'Programming', 5),
(102, 'Clean Code', 'Robert C. Martin', 'Software Engineering', 3),
(103, 'Introduction to Algorithms', 'Thomas H. Cormen', 'Computer Science', 2),
(104, 'Head First Design Patterns', 'Eric Freeman', 'Software Architecture', 4)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Sample Members
INSERT INTO members (id, name, email, phone) VALUES
(201, 'David Miller', 'david.miller@example.com', '9876543210'),
(202, 'Sarah Connor', 'sarah.connor@example.com', '9876543211'),
(203, 'John Doe', 'john.doe@example.com', '9876543212')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Sample Issue Records (One active issued 10 days ago to test fine calculation)
INSERT INTO issue_records (issue_id, book_id, member_id, issue_date, status) VALUES
(1, 101, 201, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'ISSUED')
ON DUPLICATE KEY UPDATE status=VALUES(status);

-- Update quantity for the sample issued book
UPDATE books SET quantity = quantity - 1 WHERE id = 101 AND quantity > 0;

-- Verification
SELECT * FROM books;
SELECT * FROM members;
SELECT * FROM issue_records;

