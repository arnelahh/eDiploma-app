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
public class NoticeDTO {
    private LocalDate documentDate;
    private String studentFullName;
    private String thesisTitle;
    private String commissionDecisionNumber;
    private LocalDate commissionDecisionDate;
    private LocalDate commissionMeetingDate;
    private LocalDate defenseDate;
    private String defenseTime;
    private String defenseLocation;
    private String deanFullName;
}