package model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppUser {
    private int id;
    private UserRole role;     // povezano preko RoleId
    private String username;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isActive;
    private String appPassword;
    private AcademicStaff academicStaff; // povezano preko AcademicStaffId

    public AppUser(int id, UserRole role, String username, String email, String passwordHash, LocalDateTime createdAt, LocalDateTime updatedAt, boolean isActive, String appPassword, AcademicStaff academicStaff) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isActive = isActive;
        this.appPassword = appPassword;
        this.academicStaff = academicStaff;
    }
}
