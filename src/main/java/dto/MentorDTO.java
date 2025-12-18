package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.AcademicStaff;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class MentorDTO {
    private int id;
    private AcademicStaff mentor;
    private int studentCount;

    public String getDisplayName() {
        return (mentor.getTitle() != null && !mentor.getTitle().isEmpty() ? mentor.getTitle() + " " : "")
                + mentor.getFirstName() + " " + mentor.getLastName();
    }

}
