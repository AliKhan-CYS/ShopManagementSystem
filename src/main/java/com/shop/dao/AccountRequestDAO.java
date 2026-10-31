package com.shop.dao;

import com.shop.db.DBConnection;
import com.shop.model.AccountRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountRequestDAO {

    public boolean createRequest(String username, String requestType, String requestedRole, String notes) {
        if (username == null || !username.trim().matches("[A-Za-z0-9._-]{3,32}")
                || (!"PASSWORD_RESET".equals(requestType) && !"NEW_ACCOUNT".equals(requestType))
                || (notes != null && notes.length() > 1000)
                || ("NEW_ACCOUNT".equals(requestType) && !"employee".equalsIgnoreCase(requestedRole))) return false;
        String sql = "INSERT INTO account_requests(username, request_type, requested_role, notes, status) VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, requestType);
            stmt.setString(3, requestedRole);
            stmt.setString(4, notes);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<AccountRequest> getAllRequests() {
        List<AccountRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM account_requests ORDER BY CASE status WHEN 'PENDING' THEN 0 ELSE 1 END, created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                requests.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public int countPendingRequests() {
        String sql = "SELECT COUNT(*) FROM account_requests WHERE status='PENDING'";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean resolveRequest(int id, String status, String adminResponse) {
        if (id <= 0 || (!"APPROVED".equals(status) && !"REJECTED".equals(status))
                || (adminResponse != null && adminResponse.length() > 1000)) return false;
        String sql = "UPDATE account_requests SET status=?, admin_response=?, resolved_at=CURRENT_TIMESTAMP WHERE id=? AND status='PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setString(2, adminResponse);
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private AccountRequest mapRequest(ResultSet rs) throws SQLException {
        return new AccountRequest(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("request_type"),
                rs.getString("requested_role"),
                rs.getString("notes"),
                rs.getString("status"),
                rs.getString("admin_response"),
                rs.getString("created_at"),
                rs.getString("resolved_at")
        );
    }
}
