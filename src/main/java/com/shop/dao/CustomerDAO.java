package com.shop.dao;

import com.shop.db.DBConnection;
import com.shop.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    private static final String SELECT_COLUMNS = "SELECT id, name, email, phone, address FROM customers";

    public boolean addCustomer(Customer c) {
        if (!isValid(c)) return false;
        String sql = "INSERT INTO customers(name, email, phone, address) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindFields(stmt, c);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCustomer(Customer c) {
        if (c == null || c.getId() <= 0 || !isValid(c)) return false;
        String sql = "UPDATE customers SET name=?, email=?, phone=?, address=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindFields(stmt, c);
            stmt.setInt(5, c.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCustomer(int id) {
        String sql = "DELETE FROM customers WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_COLUMNS + " ORDER BY name COLLATE NOCASE")) {
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Customer> searchByName(String keyword) {
        List<Customer> list = new ArrayList<>();
        String sql = SELECT_COLUMNS + " WHERE name LIKE ? ORDER BY name COLLATE NOCASE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCustomer(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private static boolean isValid(Customer c) {
        return c != null && c.getName() != null && !c.getName().isBlank() && c.getName().length() <= 120
                && length(c.getEmail()) <= 120 && length(c.getPhone()) <= 40 && length(c.getAddress()) <= 250;
    }

    private static int length(String value) {
        return value == null ? 0 : value.length();
    }

    private static void bindFields(PreparedStatement stmt, Customer c) throws SQLException {
        stmt.setString(1, c.getName());
        stmt.setString(2, c.getEmail());
        stmt.setString(3, c.getPhone());
        stmt.setString(4, c.getAddress());
    }

    private static Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address")
        );
    }
}
