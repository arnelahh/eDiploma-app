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
    // Document number components
    private String documentNumberPrefix;  // "11-403-103-"
    private String userInputNumbers;      // 4 cifre koje korisnik unosi
    private String documentNumberSuffix;  // "/25"

    // Date
    private LocalDate decisionDate;

    // Student
    private String studentFullName;  // "ČORIĆ AMEL"

    // Commission members
    private String chairmanFullName;     // "V.prof.dr. Nevzudin Buzađija"
    private String member1FullName;      // "Prof. dr. Edin Berberović"
    private String mentorFullName;       // "Prof.dr. Samir Lemeš"
    private String secretaryFullName;    // "V.ass.mr. Edin Tabak"

    // Dean info
    private String deanFullName;         // "Prof.dr.sc. Samir Lemeš"
    private boolean isDean;              // Za provjeru da li je mentor dekan
}