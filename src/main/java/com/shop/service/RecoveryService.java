package com.shop.service;

import com.shop.dao.UserDAO;
import com.shop.db.DBConnection;
import com.shop.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Properties;

public class RecoveryService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long TOKEN_VALID_DAYS = 30;
    private final UserDAO userDAO = new UserDAO();

    public boolean createRecoveryFile(User admin, File file) {
        User current = admin == null ? null : userDAO.findById(admin.getId());
        if (current == null || !current.isAdmin() || !current.isActive() || file == null) {
            return false;
        }

        String token = generateToken();
        String tokenHash = PasswordSecurity.hash(token);

        Connection conn = null;
        boolean previousAutoCommit = true;
        try {
            conn = DBConnection.getConnection();
            previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement deactivate = conn.prepareStatement("UPDATE admin_recovery_tokens SET active=0 WHERE user_id=? AND active=1")) {
                deactivate.setInt(1, admin.getId());
                deactivate.executeUpdate();
            }

            try (PreparedStatement insert = conn.prepareStatement("""
                    INSERT INTO admin_recovery_tokens(user_id, token_hash, expires_at, active)
                    VALUES (?, ?, datetime('now', '+' || ? || ' days'), 1)
                    """)) {
                insert.setInt(1, admin.getId());
                insert.setString(2, tokenHash);
                insert.setLong(3, TOKEN_VALID_DAYS);
                insert.executeUpdate();
            }

            writeRecoveryFile(current, token, file);
            conn.commit();
            return true;
        } catch (Exception e) {
            rollback(conn);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(previousAutoCommit);
                } catch (Exception ignored) {
                }
            }
        }

        return false;
    }

    public boolean resetAdminPasswordWithFile(String username, File file, String newPassword) {
        User admin = userDAO.findByUsername(username);
        if (admin == null || !admin.isAdmin() || !admin.isActive() || file == null
                || !PasswordSecurity.isValidNewPassword(newPassword)) {
            return false;
        }

        String token = readProperty(file, "token");
        String recoveryUsername = readProperty(file, "username");
        if (token == null || token.isBlank() || !username.equalsIgnoreCase(recoveryUsername)) {
            return false;
        }

        String sql = """
                SELECT id, token_hash
                FROM admin_recovery_tokens
                WHERE user_id=?
                  AND active=1
                  AND used_at IS NULL
                  AND expires_at > CURRENT_TIMESTAMP
                ORDER BY created_at DESC
                """;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, admin.getId());
                int matchedTokenId = -1;
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        if (PasswordSecurity.verify(token, rs.getString("token_hash"))) {
                            matchedTokenId = rs.getInt("id");
                            break;
                        }
                    }
                }
                if (matchedTokenId > 0) {
                    try (PreparedStatement updatePassword = conn.prepareStatement(
                            "UPDATE users SET password=? WHERE id=? AND lower(role)='admin' AND active=1")) {
                        updatePassword.setString(1, PasswordSecurity.hash(newPassword));
                        updatePassword.setInt(2, admin.getId());
                        if (updatePassword.executeUpdate() == 1) {
                            markTokenUsed(conn, matchedTokenId);
                            try (PreparedStatement deactivateOthers = conn.prepareStatement(
                                    "UPDATE admin_recovery_tokens SET active=0 WHERE user_id=?")) {
                                deactivateOthers.setInt(1, admin.getId());
                                deactivateOthers.executeUpdate();
                            }
                            conn.commit();
                            return true;
                        }
                    }
                }
            }
            conn.rollback();
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    private void writeRecoveryFile(User admin, String token, File file) throws Exception {
        Instant createdAt = Instant.now();
        Properties properties = new Properties();
        properties.setProperty("app", "ShopManagementSystem");
        properties.setProperty("type", "ADMIN_PASSWORD_RECOVERY");
        properties.setProperty("username", admin.getUsername());
        properties.setProperty("createdAt", createdAt.toString());
        properties.setProperty("expiresAt", createdAt.plus(TOKEN_VALID_DAYS, ChronoUnit.DAYS).toString());
        properties.setProperty("token", token);
        properties.setProperty("notice", "This file contains a one-time recovery token, not the account password. Keep it private.");

        try (OutputStream out = Files.newOutputStream(file.toPath(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            properties.store(out, "Shop Management admin recovery token");
        }

        restrictToOwner(file);
    }

    private String readProperty(File file, String property) {
        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            properties.load(in);
            if (!"ADMIN_PASSWORD_RECOVERY".equals(properties.getProperty("type"))) {
                return null;
            }
            return properties.getProperty(property);
        } catch (Exception e) {
            return null;
        }
    }

    private void restrictToOwner(File file) {
        try {
            file.setReadable(false, false);
            file.setWritable(false, false);
            file.setExecutable(false, false);
            file.setReadable(true, true);
            file.setWritable(true, true);
        } catch (SecurityException ignored) {
        }
    }

    private void markTokenUsed(Connection conn, int tokenId) throws SQLException {
        String sql = "UPDATE admin_recovery_tokens SET active=0, used_at=CURRENT_TIMESTAMP WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tokenId);
            stmt.executeUpdate();
        }
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
