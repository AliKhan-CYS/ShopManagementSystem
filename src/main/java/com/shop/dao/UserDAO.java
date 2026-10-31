package com.shop.dao;

import com.shop.db.DBConnection;
import com.shop.model.User;
import com.shop.service.PasswordSecurity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt("active") == 1 && PasswordSecurity.verify(password, rs.getString("password"))) {
                    int id = rs.getInt("id");
                    String storedPassword = rs.getString("password");

                    if (PasswordSecurity.needsRehash(storedPassword)) {
                        storedPassword = PasswordSecurity.hash(password);
                        updatePasswordHash(conn, id, storedPassword);
                    }

                    updateLastLogin(conn, id);
                    return new User(
                            id,
                            rs.getString("username"),
                            storedPassword,
                            rs.getString("role"),
                            rs.getInt("active") == 1,
                            rs.getString("profile_photo_path"),
                            rs.getString("last_login"),
                            rs.getString("created_at")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addUser(User u) {
        if (u == null || !isValidUsername(u.getUsername())
                || !PasswordSecurity.isValidNewPassword(u.getPassword())
                || !isSupportedRole(u.getRole())) return false;
        String sql = "INSERT INTO users(username, password, role, active) VALUES (?, ?, ?, ?)";
        String securePassword = PasswordSecurity.isHashed(u.getPassword())
                ? u.getPassword()
                : PasswordSecurity.hash(u.getPassword());

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, u.getUsername());
            stmt.setString(2, securePassword);
            stmt.setString(3, u.getRole());
            stmt.setInt(4, u.isActive() ? 1 : 0);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            if (!e.getMessage().toLowerCase().contains("constraint")) {
                e.printStackTrace();
            }
        }

        return false;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapUser(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        User user = findById(userId);
        if (user == null || !PasswordSecurity.verify(currentPassword, user.getPassword())) {
            return false;
        }

        return setPassword(userId, newPassword);
    }

    public boolean setPassword(int userId, String newPassword) {
        if (userId <= 0 || !PasswordSecurity.isValidNewPassword(newPassword)) return false;
        String sql = "UPDATE users SET password=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, PasswordSecurity.hash(newPassword));
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean updateProfilePhoto(int userId, String photoPath) {
        String sql = "UPDATE users SET profile_photo_path=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, photoPath);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean setActive(int id, boolean active) {
        if (id <= 0) return false;
        User target = findById(id);
        if (target == null) return false;
        if (!active && target.isAdmin() && countActiveAdmins() <= 1) return false;
        String sql = "UPDATE users SET active=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, active ? 1 : 0);
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public long countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE lower(role)='admin' AND active=1";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public long countUsers() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException e) {
            return -1;
        }
    }

    public boolean createInitialAdmin(String username, String password) {
        if (!isValidUsername(username) || !PasswordSecurity.isValidNewPassword(password)) return false;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement check = conn.createStatement(); ResultSet rs = check.executeQuery("SELECT COUNT(*) FROM users")) {
                if (!rs.next() || rs.getLong(1) != 0) {
                    conn.rollback();
                    return false;
                }
            }
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO users(username,password,role,active) VALUES(?,?, 'admin',1)")) {
                insert.setString(1, username.trim());
                insert.setString(2, PasswordSecurity.hash(password));
                boolean created = insert.executeUpdate() == 1;
                conn.commit();
                return created;
            } catch (SQLException e) {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean isValidUsername(String username) {
        return username != null && username.trim().matches("[A-Za-z0-9._-]{3,32}");
    }

    private static boolean isSupportedRole(String role) {
        return "admin".equalsIgnoreCase(role) || "employee".equalsIgnoreCase(role);
    }

    private void updatePasswordHash(Connection conn, int id, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password=? WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, passwordHash);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    private void updateLastLogin(Connection conn, int id) throws SQLException {
        String sql = "UPDATE users SET last_login=CURRENT_TIMESTAMP WHERE id=?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role"),
                rs.getInt("active") == 1,
                rs.getString("profile_photo_path"),
                rs.getString("last_login"),
                rs.getString("created_at")
        );
    }
}
