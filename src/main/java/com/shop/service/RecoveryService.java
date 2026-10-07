package com.shop.service;

import com.shop.dao.UserDAO;
import com.shop.db.DBConnection;
import com.shop.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Properties;

public class RecoveryService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final UserDAO userDAO = new UserDAO();

    public boolean createRecoveryFile(User admin, File file) {
        if (admin == null || !admin.isAdmin() || file == null) {
            return false;
        }

        String token = generateToken();
        String tokenHash = PasswordSecurity.hash(token);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement deactivate = conn.prepareStatement("UPDATE admin_recovery_tokens SET active=0 WHERE user_id=? AND active=1")) {
                deactivate.setInt(1, admin.getId());
                deactivate.executeUpdate();
            }

            try (PreparedStatement insert = conn.prepareStatement("INSERT INTO admin_recovery_tokens(user_id, token_hash, active) VALUES (?, ?, 1)")) {
                insert.setInt(1, admin.getId());
                insert.setString(2, tokenHash);
                insert.executeUpdate();
            }

            writeRecoveryFile(admin, token, file);
            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                DBConnection.getConnection().setAutoCommit(true);
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    public boolean resetAdminPasswordWithFile(String username, File file, String newPassword) {
        User admin = userDAO.findByUsername(username);
        if (admin == null || !admin.isAdmin() || file == null || newPassword == null || newPassword.length() < 6) {
            return false;
        }

        String token = readToken(file);
        if (token == null || token.isBlank()) {
            return false;
        }

        String sql = "SELECT id, token_hash FROM admin_recovery_tokens WHERE user_id=? AND active=1 AND used_at IS NULL ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, admin.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (PasswordSecurity.verify(token, rs.getString("token_hash"))) {
                        if (userDAO.setPassword(admin.getId(), newPassword)) {
                            markTokenUsed(conn, rs.getInt("id"));
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void writeRecoveryFile(User admin, String token, File file) throws Exception {
        Properties properties = new Properties();
        properties.setProperty("app", "ShopManagementSystem");
        properties.setProperty("type", "ADMIN_PASSWORD_RECOVERY");
        properties.setProperty("username", admin.getUsername());
        properties.setProperty("createdAt", Instant.now().toString());
        properties.setProperty("token", token);
        properties.setProperty("notice", "This file contains a recovery token, not the account password. Keep it private.");

        try (FileOutputStream out = new FileOutputStream(file)) {
            properties.store(out, "Shop Management admin recovery token");
        }
    }

    private String readToken(File file) {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            properties.load(in);
            if (!"ADMIN_PASSWORD_RECOVERY".equals(properties.getProperty("type"))) {
                return null;
            }
            return properties.getProperty("token");
        } catch (Exception e) {
            return null;
        }
    }

    private void markTokenUsed(Connection conn, int tokenId) throws SQLException {
        String sql = "UPDATE admin_recovery_tokens SET active=0, used_at=CURRENT_TIMESTAMP WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tokenId);
            stmt.executeUpdate();
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}