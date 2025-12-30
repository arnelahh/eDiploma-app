package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Thesis {
    private int id;
    private int studentId;
    private int academicStaffId;
    private int departmentId;
    private int secretaryId; // Ovo je sada ID iz AcademicStaff tabele (preko AppUser.AcademicStaffId)
    private int subjectId;
    private int statusId;

    private String title;
    private LocalDate applicationDate;
    private LocalDate approvalDate;
    private LocalDate defenseDate;
    private Integer grade;
    private boolean isActive;
    private Department department;
    private Student student;
    private Subject subject;
    private ThesisStatus status;
    private AcademicStaff secretary; // PROMJENA: Sada je AcademicStaff umjesto AppUser
    private AcademicStaff mentor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private String literature;
    private String structure;
}