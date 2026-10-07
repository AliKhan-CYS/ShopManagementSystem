package com.shop.model;

public class AccountRequest {

    private final int id;
    private final String username;
    private final String requestType;
    private final String requestedRole;
    private final String notes;
    private final String status;
    private final String adminResponse;
    private final String createdAt;
    private final String resolvedAt;

    public AccountRequest(int id, String username, String requestType, String requestedRole,
                          String notes, String status, String adminResponse,
                          String createdAt, String resolvedAt) {
        this.id = id;
        this.username = username;
        this.requestType = requestType;
        this.requestedRole = requestedRole;
        this.notes = notes;
        this.status = status;
        this.adminResponse = adminResponse;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getRequestType() { return requestType; }
    public String getRequestedRole() { return requestedRole; }
    public String getNotes() { return notes; }
    public String getStatus() { return status; }
    public String getAdminResponse() { return adminResponse; }
    public String getCreatedAt() { return createdAt; }
    public String getResolvedAt() { return resolvedAt; }

    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }
}