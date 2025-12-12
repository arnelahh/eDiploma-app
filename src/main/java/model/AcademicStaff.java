package model;

import java.time.LocalDateTime;

public class AcademicStaff {
    private int Id;
    private String Title;
    private String FirstName;
    private String LastName;
    private String Email;
    private boolean IsDean;
    private boolean IsActive;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;

    public AcademicStaff(){}

    public AcademicStaff(int id, String title, String firstName, String lastName, String email, boolean isDean, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Id = id;
        Title = title;
        FirstName = firstName;
        LastName = lastName;
        Email = email;
        IsDean = isDean;
        IsActive = isActive;
        CreatedAt = createdAt;
        UpdatedAt = updatedAt;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getLastName() {
        return LastName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public boolean isDean() {
        return IsDean;
    }

    public void setDean(boolean dean) {
        IsDean = dean;
    }

    public boolean isActive() {
        return IsActive;
    }

    public void setActive(boolean active) {
        IsActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return CreatedAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        CreatedAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return UpdatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        UpdatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "AcademicStaff{" +
                "Id=" + Id +
                ", Title='" + Title + '\'' +
                ", FirstName='" + FirstName + '\'' +
                ", LastName='" + LastName + '\'' +
                ", Email='" + Email + '\'' +
                ", IsDean=" + IsDean +
                ", IsActive=" + IsActive +
                '}';
    }
}
