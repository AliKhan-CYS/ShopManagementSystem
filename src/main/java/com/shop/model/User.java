package com.shop.model;

public class User {

    private static final String USERNAME_PATTERN = "[A-Za-z0-9._-]{3,32}";

    private int id;
    private String username;
    private String password;
    private String role;
    private boolean active;
    private String profilePhotoPath;
    private String lastLogin;
    private String createdAt;

    public User(int id, String username, String password, String role, boolean active,
                String profilePhotoPath, String lastLogin, String createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
        this.profilePhotoPath = profilePhotoPath;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public String getProfilePhotoPath() { return profilePhotoPath; }
    public String getLastLogin() { return lastLogin; }
    public String getCreatedAt() { return createdAt; }

    public static boolean isValidUsername(String username) {
        return username != null && username.trim().matches(USERNAME_PATTERN);
    }

    public boolean isAdmin() {
        return role != null && role.equalsIgnoreCase("admin");
    }
}