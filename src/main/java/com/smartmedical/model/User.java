package com.smartmedical.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private String role;
    private boolean active;
    private Timestamp lastLogin;
    private Timestamp createdAt;

    public User() {}

    // Getters & Setters
    public int getUserId()               { return userId; }
    public void setUserId(int userId)    { this.userId = userId; }

    public String getUsername()                  { return username; }
    public void setUsername(String username)     { this.username = username; }

    public String getPasswordHash()                      { return passwordHash; }
    public void setPasswordHash(String passwordHash)     { this.passwordHash = passwordHash; }

    public String getRole()              { return role; }
    public void setRole(String role)     { this.role = role; }

    public boolean isActive()                { return active; }
    public void setActive(boolean active)    { this.active = active; }

    public Timestamp getLastLogin()                  { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin)    { this.lastLogin = lastLogin; }

    public Timestamp getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(Timestamp createdAt)    { this.createdAt = createdAt; }
}
