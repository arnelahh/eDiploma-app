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
    private String AppPassword;
}
