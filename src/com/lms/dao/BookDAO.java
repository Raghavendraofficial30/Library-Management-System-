package com.lms.dao;

import com.lms.model.Book;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Book operations.
 * Handles persistence, search, stock checking, and CRUD for books using PreparedStatement.
 */
public class BookDAO {

    /**
     * Checks if a book with the given ID exists.
     */
    public boolean isBookExists(int id) {
        String sql = "SELECT id FROM books WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Error checking book existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new book to the database.
     */
    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (id, title, author, category, quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, book.getId());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getQuantity());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to add book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing book's details by ID.
     */
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, category = ?, quantity = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getTitle());
            ps.setString(2, book.getAuthor());
            ps.setString(3, book.getCategory());
            ps.setInt(4, book.getQuantity());
            ps.setInt(5, book.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to update book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a book record by ID.
     */
    public boolean deleteBook(int id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to delete book: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a book by ID.
     */
    public Book getBookById(int id) {
        String sql = "SELECT id, title, author, category, quantity FROM books WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBook(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to get book by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Searches books by title, author, or category containing the keyword.
     */
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, category, quantity FROM books " +
                     "WHERE LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?) OR LOWER(category) LIKE LOWER(?) " +
                     "ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String term = "%" + keyword + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, term);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(mapRowToBook(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to search books: " + e.getMessage());
        }
        return books;
    }

    /**
     * Retrieves all books ordered by ID.
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, category, quantity FROM books ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                books.add(mapRowToBook(rs));
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to fetch all books: " + e.getMessage());
        }
        return books;
    }

    /**
     * Gets the current available quantity of a book.
     */
    public int getBookQuantity(int id) {
        String sql = "SELECT quantity FROM books WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity");
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to retrieve quantity: " + e.getMessage());
        }
        return -1;
    }

    private Book mapRowToBook(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("category"),
            rs.getInt("quantity")
        );
    }
}

