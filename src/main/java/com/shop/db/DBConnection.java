package com.shop.db;

import com.shop.service.PasswordSecurity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    private static Connection conn;

    public static Connection getConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                String url = "jdbc:sqlite:" + System.getProperty("user.dir") + "/shop.db";
                conn = DriverManager.getConnection(url);
                initializeDatabase(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }

    private static void initializeDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE,
                    password TEXT,
                    role TEXT,
                    active INTEGER DEFAULT 1,
                    profile_photo_path TEXT,
                    last_login TIMESTAMP,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            addColumnIfMissing(conn, "users", "active", "INTEGER DEFAULT 1");
            addColumnIfMissing(conn, "users", "profile_photo_path", "TEXT");
            addColumnIfMissing(conn, "users", "last_login", "TIMESTAMP");
            addColumnIfMissing(conn, "users", "created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS account_requests (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    request_type TEXT NOT NULL,
                    requested_role TEXT,
                    notes TEXT,
                    status TEXT DEFAULT 'PENDING',
                    admin_response TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    resolved_at TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS admin_recovery_tokens (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    token_hash TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    used_at TIMESTAMP,
                    active INTEGER DEFAULT 1,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    price REAL,
                    quantity INTEGER,
                    category TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT,
                    phone TEXT,
                    email TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER,
                    total REAL,
                    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER,
                    product_id INTEGER,
                    quantity INTEGER,
                    price REAL
                )
            """);

            migratePlainTextPasswords(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addColumnIfMissing(Connection conn, String table, String column, String definition) throws Exception {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            if (!rs.next()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                }
            }
        }
    }

    private static void migratePlainTextPasswords(Connection conn) throws Exception {
        List<PasswordUpdate> updates = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, password FROM users")) {
            while (rs.next()) {
                String password = rs.getString("password");
                if (password != null && !password.isBlank() && !PasswordSecurity.isHashed(password)) {
                    updates.add(new PasswordUpdate(rs.getInt("id"), PasswordSecurity.hash(password)));
                }
            }
        }

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE users SET password=? WHERE id=?")) {
            for (PasswordUpdate update : updates) {
                stmt.setString(1, update.passwordHash());
                stmt.setInt(2, update.userId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private record PasswordUpdate(int userId, String passwordHash) {
    }
}