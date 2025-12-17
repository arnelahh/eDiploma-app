package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ThesisDTO {
    private int id;
    private String title;
    private int cycle;
    private String studentFullName;
    private String mentorFullName;
    private String status;
    private LocalDate applicationDate;
}
