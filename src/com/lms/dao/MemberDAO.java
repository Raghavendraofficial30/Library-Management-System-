package com.lms.dao;

import com.lms.model.Member;
import com.lms.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Member operations.
 * Handles persistence, unique validation, and retrieval of library members.
 */
public class MemberDAO {

    /**
     * Checks if a member exists by ID.
     */
    public boolean isMemberExists(int id) {
        String sql = "SELECT id FROM members WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Error checking member ID: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if an email is already registered to another member.
     */
    public boolean isEmailExists(String email, int excludeId) {
        String sql = "SELECT id FROM members WHERE email = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Error checking member email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a new member to the database.
     */
    public boolean addMember(Member member) {
        String sql = "INSERT INTO members (id, name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, member.getId());
            ps.setString(2, member.getName());
            ps.setString(3, member.getEmail());
            ps.setString(4, member.getPhone());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to add member: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates an existing member's details.
     */
    public boolean updateMember(Member member) {
        String sql = "UPDATE members SET name = ?, email = ?, phone = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setInt(4, member.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to update member: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deletes a member by ID.
     */
    public boolean deleteMember(int id) {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to delete member: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves a member by ID.
     */
    public Member getMemberById(int id) {
        String sql = "SELECT id, name, email, phone FROM members WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToMember(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to get member by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Searches members by name, email, or phone.
     */
    public List<Member> searchMembers(String keyword) {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT id, name, email, phone FROM members " +
                     "WHERE LOWER(name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?) OR phone LIKE ? " +
                     "ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String term = "%" + keyword + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, term);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRowToMember(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to search members: " + e.getMessage());
        }
        return members;
    }

    /**
     * Retrieves all registered members.
     */
    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT id, name, email, phone FROM members ORDER BY id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                members.add(mapRowToMember(rs));
            }
        } catch (SQLException e) {
            System.err.println("[Database Error] Failed to fetch members: " + e.getMessage());
        }
        return members;
    }

    private Member mapRowToMember(ResultSet rs) throws SQLException {
        return new Member(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("phone")
        );
    }
}

