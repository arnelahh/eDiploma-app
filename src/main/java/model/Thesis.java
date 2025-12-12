package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Thesis {
    private int id;
    private String title;
    private LocalDate applicationDate;
    private LocalDate approvalDate;
    private LocalDate defenseDate;
    private BigDecimal grade;
    private boolean isActive;
    private Department department;
    private Student student;
    private Subject subject;
    private ThesisStatus status;
    private AppUser secretary;
    private AcademicStaff mentor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
