package com.shop.db;

import com.shop.service.PasswordSecurity;
import com.shop.AppPaths;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {

    private static boolean initialized;

    public static Connection getConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            Path database = AppPaths.databaseFile();
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + database);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys=ON");
                stmt.execute("PRAGMA busy_timeout=5000");
            }
            synchronized (DBConnection.class) {
                if (!initialized) {
                    initializeDatabase(conn);
                    initialized = true;
                }
            }
            return conn;
        } catch (Exception e) {
            throw new IllegalStateException("Could not open the shop database", e);
        }
    }

    private static void initializeDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE CHECK(length(username) BETWEEN 3 AND 32),
                    password TEXT NOT NULL,
                    role TEXT NOT NULL CHECK(lower(role) IN ('admin','employee')),
                    active INTEGER NOT NULL DEFAULT 1 CHECK(active IN (0,1)),
                    profile_photo_path TEXT,
                    last_login TIMESTAMP,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            addColumnIfMissing(conn, "users", "active", "INTEGER DEFAULT 1");
            addColumnIfMissing(conn, "users", "profile_photo_path", "TEXT");
            addColumnIfMissing(conn, "users", "last_login", "TIMESTAMP");
            addColumnIfMissing(conn, "users", "created_at", "TIMESTAMP");
            stmt.execute("UPDATE users SET created_at=CURRENT_TIMESTAMP WHERE created_at IS NULL");

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
                    expires_at TIMESTAMP,
                    used_at TIMESTAMP,
                    active INTEGER DEFAULT 1,
                    FOREIGN KEY(user_id) REFERENCES users(id)
                )
            """);

            addColumnIfMissing(conn, "admin_recovery_tokens", "expires_at", "TIMESTAMP");

            stmt.execute("""
            CREATE TABLE IF NOT EXISTS products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    price REAL NOT NULL CHECK(price >= 0),
                    quantity INTEGER NOT NULL CHECK(quantity >= 0),
                    category TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT,
                    email TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER REFERENCES customers(id),
                    total REAL NOT NULL CHECK(total >= 0),
                    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS sale_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    sale_id INTEGER NOT NULL REFERENCES sales(id),
                    product_id INTEGER NOT NULL REFERENCES products(id),
                    quantity INTEGER NOT NULL CHECK(quantity > 0),
                    price REAL NOT NULL CHECK(price >= 0)
                )
            """);

            migratePlainTextPasswords(conn);
        } catch (Exception e) {
            throw new IllegalStateException("Could not initialize the shop database", e);
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
