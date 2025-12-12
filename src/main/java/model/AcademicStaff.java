package model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
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
}
