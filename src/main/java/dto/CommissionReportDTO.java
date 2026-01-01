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
public class CommissionReportDTO {
    private LocalDate decisionDate;
    private String studentFullName;
    private String chairmanFullName;
    private String member1FullName;
    private String mentorFullName;
    private String secretaryFullName;
    private String deanFullName;
}