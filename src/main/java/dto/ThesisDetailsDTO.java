package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.*;

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
    private Integer grade;
    private String status;
    private String description;
    private String literature;

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