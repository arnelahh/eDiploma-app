package model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Thesis {
    private int Id;
    private String Title;
    private LocalDate ApplicationDate;
    private LocalDate ApprovalDate;
    private LocalDate DefenseDate;
    private BigDecimal Grade;
    private boolean IsActive;
    private Department Department;
    private Student Student;
    private Subject Subject;
    private ThesisStatus Status;
    private AppUser Secretary;
    private AcademicStaff Mentor;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
}
