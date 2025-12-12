package model;

import java.time.LocalDateTime;

public class AppUser {
    private int id;
    private UserRole role;     // povezano preko RoleId
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private String AppPassword;

    public AppUser() {}

    public AppUser(int id, UserRole role, String username, String email,
                   String passwordHash, LocalDateTime createdAt,
                   LocalDateTime updatedAt, boolean isActive, String appPassword) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
        this.AppPassword = appPassword;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }


    public String getAppPassword() {return AppPassword; }

    public void setAppPassword(String appPassword) { AppPassword = appPassword; }

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", role=" + role +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
