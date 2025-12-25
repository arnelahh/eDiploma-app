package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

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
    private LocalDate defenseDate;

    // Student
    private String studentFullName;

    // Thesis title
    private String thesisTitle;

    // Mentor
    private String mentorFullName;

    // Final grade
    private Integer finalGrade;
}