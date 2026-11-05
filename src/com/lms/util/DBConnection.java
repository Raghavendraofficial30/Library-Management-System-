package com.lms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database connection utility for Library Management System.
 * Centralizes JDBC Connection creation and Driver initialization.
 */
public class DBConnection {

    // Database connection URL and credentials
    // IMPORTANT: Update USERNAME and PASSWORD to match your MySQL installation
    private static final String URL = "jdbc:mysql://localhost:3306/library_management_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root"; // <-- Change to your MySQL password

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[Driver Error] MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }
    }

    /**
     * Establishes a new connection to the MySQL database.
     * 
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}

