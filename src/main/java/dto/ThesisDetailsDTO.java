package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThesisDetailsDTO {
    // Basic thesis info
    private int id;
    private String title;
    private LocalDate applicationDate;
    private LocalDate approvalDate;
    private LocalDate defenseDate;
    private BigDecimal grade;
    private String status;

    // Student info
    private Student student;

    // Mentor info
    private AcademicStaff mentor;

    // Secretary info
    private AcademicStaff secretary;

    // Department and subject
    private Department department;
    private Subject subject;
}