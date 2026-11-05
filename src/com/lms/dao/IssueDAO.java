package com.lms.dao;

import com.lms.model.IssueRecord;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for book issuing and return operations.
 * Implements transaction management (ACID) to ensure book quantities and issue records stay in sync.
 */
public class IssueDAO {

    /**
     * Checks if a member already has an active (unreturned) issue of this book.
     */
    public boolean isBookAlreadyIssuedToMember(int bookId, int memberId) {
        String sql = "SELECT issue_id FROM issue_records WHERE book_id = ? AND member_id = ? AND status = 'ISSUED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ps.setInt(2, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Error checking existing issuance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Issues a book to a member.
     * Uses a database transaction:
     * 1. Inserts a new row in issue_records with status 'ISSUED'.
     * 2. Decrements the available quantity in books by 1.
     * 
     * @param bookId ID of the book
     * @param memberId ID of the member
     * @return true if issuance succeeded, false otherwise
     */
    public boolean issueBook(int bookId, int memberId) {
        String insertSql = "INSERT INTO issue_records (book_id, member_id, issue_date, status) VALUES (?, ?, CURDATE(), 'ISSUED')";
        String updateBookSql = "UPDATE books SET quantity = quantity - 1 WHERE id = ? AND quantity > 0";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            // Start transaction
            conn.setAutoCommit(false);

            // 1. Insert issue record
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setInt(1, bookId);
                psInsert.setInt(2, memberId);
                int rowsInserted = psInsert.executeUpdate();
                if (rowsInserted == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Decrement book stock
            try (PreparedStatement psUpdate = conn.prepareStatement(updateBookSql)) {
                psUpdate.setInt(1, bookId);
                int rowsUpdated = psUpdate.executeUpdate();
                if (rowsUpdated == 0) {
                    // Out of stock or book not found
                    conn.rollback();
                    return false;
                }
            }

            // Commit transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to issue book: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Retrieves an active (unreturned) issue record by issue ID.
     */
    public IssueRecord getActiveIssueById(int issueId) {
        String sql = "SELECT ir.issue_id, ir.book_id, ir.member_id, ir.issue_date, ir.return_date, ir.fine, ir.status, " +
                     "b.title AS book_title, m.name AS member_name " +
                     "FROM issue_records ir " +
                     "JOIN books b ON ir.book_id = b.id " +
                     "JOIN members m ON ir.member_id = m.id " +
                     "WHERE ir.issue_id = ? AND ir.status = 'ISSUED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, issueId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToIssueRecord(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to retrieve issue record: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves an active issue record by book ID and member ID.
     */
    public IssueRecord getActiveIssueByBookAndMember(int bookId, int memberId) {
        String sql = "SELECT ir.issue_id, ir.book_id, ir.member_id, ir.issue_date, ir.return_date, ir.fine, ir.status, " +
                     "b.title AS book_title, m.name AS member_name " +
                     "FROM issue_records ir " +
                     "JOIN books b ON ir.book_id = b.id " +
                     "JOIN members m ON ir.member_id = m.id " +
                     "WHERE ir.book_id = ? AND ir.member_id = ? AND ir.status = 'ISSUED'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            ps.setInt(2, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToIssueRecord(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to retrieve issue record: " + e.getMessage());
        }
        return null;
    }

    /**
     * Processes a book return.
     * Uses a database transaction:
     * 1. Updates issue_records with return_date, fine, and status='RETURNED'.
     * 2. Increments the quantity in books by 1.
     * 
     * @param issueId The issue record ID
     * @param bookId The book ID being returned
     * @param returnDate The date returned
     * @param fine The fine assessed
     * @return true if returned successfully, false otherwise
     */
    public boolean returnBook(int issueId, int bookId, LocalDate returnDate, double fine) {
        String updateIssueSql = "UPDATE issue_records SET return_date = ?, fine = ?, status = 'RETURNED' WHERE issue_id = ?";
        String updateBookSql = "UPDATE books SET quantity = quantity + 1 WHERE id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Update issue record
            try (PreparedStatement psIssue = conn.prepareStatement(updateIssueSql)) {
                psIssue.setDate(1, Date.valueOf(returnDate));
                psIssue.setDouble(2, fine);
                psIssue.setInt(3, issueId);
                int rows = psIssue.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2. Increment book quantity
            try (PreparedStatement psBook = conn.prepareStatement(updateBookSql)) {
                psBook.setInt(1, bookId);
                int rows = psBook.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Commit transaction
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to return book: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Retrieves all currently issued books that have not yet been returned.
     */
    public List<IssueRecord> getCurrentlyIssuedBooks() {
        List<IssueRecord> list = new ArrayList<>();
        String sql = "SELECT ir.issue_id, ir.book_id, ir.member_id, ir.issue_date, ir.return_date, ir.fine, ir.status, " +
                     "b.title AS book_title, m.name AS member_name " +
                     "FROM issue_records ir " +
                     "JOIN books b ON ir.book_id = b.id " +
                     "JOIN members m ON ir.member_id = m.id " +
                     "WHERE ir.status = 'ISSUED' " +
                     "ORDER BY ir.issue_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToIssueRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to fetch currently issued books: " + e.getMessage());
        }
        return list;
    }

    private IssueRecord mapRowToIssueRecord(ResultSet rs) throws SQLException {
        Date issueDateSql = rs.getDate("issue_date");
        Date returnDateSql = rs.getDate("return_date");

        LocalDate issueDate = issueDateSql != null ? issueDateSql.toLocalDate() : null;
        LocalDate returnDate = returnDateSql != null ? returnDateSql.toLocalDate() : null;

        IssueRecord record = new IssueRecord(
            rs.getInt("issue_id"),
            rs.getInt("book_id"),
            rs.getInt("member_id"),
            issueDate,
            returnDate,
            rs.getDouble("fine"),
            rs.getString("status")
        );
        record.setBookTitle(rs.getString("book_title"));
        record.setMemberName(rs.getString("member_name"));
        return record;
    }
}

