package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.StudentStatus;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FinalThesisApprovalDTO {
    // Header
    private String decisionNumber;
    private LocalDate decisionDate;

    // Student & Grammatical variations
    private String studentFullName;      // "Amra (Mersudin) Drijenčić"
    private String studentNameGenitive;  // "Drijenčić Amre" (User input)
    private String studentStatusNominative; // Ovdje ćemo pretvoriti (npr. "apsolvent")
    private String studentStatusGenitive;   // Ovo vučemo direktno iz baze (npr. "apsolventa")
    private String studentFirstName;
    private String studentLastName;
    // Thesis Info
    private String departmentName;
    private String thesisTitle;
    private String subjectName;
    private String mentorFullNameAndTitle;

    // Content (HTML formatted text)
    private String description;
    private String literature;
    private String structure;
    private String studentCycle;
    private LocalDate applicationDate;
}
