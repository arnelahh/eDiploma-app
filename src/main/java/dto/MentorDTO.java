package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.AcademicStaff;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class MentorDTO {
    private AcademicStaff mentor;
    private int studentCount;
}
