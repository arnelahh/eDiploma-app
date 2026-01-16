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
public class DefenseReportDTO {
    // Commission members
    private String chairmanFullName;
    private String member1FullName;
    private String member2FullName;
    private String secretaryFullName;

    // Dates
    private LocalDate defenseDate; // Datum odbrane
    private LocalDate defenseReportDate; // NOVO: Datum zapisnika sa odbrane

    // Student
    private String studentFullName;

    // Thesis title (original full title)
    private String thesisTitle;

    // Thesis title split into two lines for PDF
    private String thesisTitleLine1;  // First ~40 chars with opening quote
    private String thesisTitleLine2;  // Remaining text with closing quote

    // Mentor
    private String mentorFullName;

    // Final grade
    private Integer finalGrade;
}
