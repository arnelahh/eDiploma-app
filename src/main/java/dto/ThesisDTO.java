package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ThesisDTO {
    private int id;
    private String title;
    private int cycle;
    private String studentFullName;
    private String mentorFullName;
}
