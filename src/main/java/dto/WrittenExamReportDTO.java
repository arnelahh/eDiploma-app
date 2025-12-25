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
    private LocalDate submissionDate;

    // Student
    private String studentFullName;

    // Thesis title
    private String thesisTitle;

    // Mentor
    private String mentorFullName;

    // Faculty decision number (editable)
    private String facultyDecisionNumber;

    // Proposed grade
    private Integer proposedGrade;
}