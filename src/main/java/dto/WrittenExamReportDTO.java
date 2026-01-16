package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WrittenExamReportDTO {
    // Commission members
    private String chairmanFullName;
    private String member1FullName;
    private String member2FullName;
    private String secretaryFullName;

    // Dates
    private LocalDate submissionDate; // Datum odbrane/odobrenja
    private LocalDate writtenReportDate; // NOVO: Datum zapisnika sa pismenog dijela

    // Student
    private String studentFullName;

    // Thesis title (original full title)
    private String thesisTitle;

    // Thesis title split into two lines for PDF - NOVO!
    private String thesisTitleLine1;  // First ~40 chars with opening quote
    private String thesisTitleLine2;  // Remaining text with closing quote

    // Mentor
    private String mentorFullName;

    // Faculty decision number (editable)
    private String facultyDecisionNumber;

    // Proposed grade
    private Integer proposedGrade;
}
