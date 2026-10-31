package com.shop.db;

import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {

        try (
                Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()
        ) {

            // ================= USERS =================
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        role TEXT NOT NULL
                    )
                    """);

            // ================= PRODUCTS =================
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS products (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        price REAL NOT NULL,
                        quantity INTEGER NOT NULL,
                        category TEXT NOT NULL
                    )
                    """);

            // ================= CUSTOMERS =================
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS customers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        email TEXT,
                        phone TEXT,
                        address TEXT
                    )
                    """);

            // ================= SALES =================
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS sales (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        customer_id INTEGER,
                        total REAL,
                        sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                        FOREIGN KEY(customer_id)
                        REFERENCES customers(id)
                    )
                    """);

            // ================= SALE ITEMS =================
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS sale_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        sale_id INTEGER,
                        product_id INTEGER,
                        quantity INTEGER,
                        price REAL,

                        FOREIGN KEY(sale_id)
                        REFERENCES sales(id),

                        FOREIGN KEY(product_id)
                        REFERENCES products(id)
                    )
                    """);

            System.out.println("Database initialized.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
