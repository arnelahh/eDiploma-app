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
    private LocalDate approvalDate; // Datum sjednice komisije (commission meeting date)
    private LocalDate defenseDate;
    private LocalDate finalThesisApprovalDate;
    private LocalDate commisionDate; // Datum komisije
    private LocalDate noticeDate; // NOVO: Datum rješenja obavijesti
    private LocalDate writtenReportDate; // NOVO: Datum zapisnika sa pismenog dijela
    private LocalDate defenseReportDate; // NOVO: Datum zapisnika sa odbrane
    private String commisionTime; // NOVO: Vrijeme sjednice komisije
    private LocalDate cycleCompletionDate;
    private Integer grade;
    private String status;
    private String description;
    private String literature;
    private String structure;
    private boolean passedSubjects; // Da li je student položio sve ispite

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
