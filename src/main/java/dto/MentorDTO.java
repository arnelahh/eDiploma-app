package dto;

import model.AcademicStaff;

public class MentorDTO {
    private AcademicStaff mentor;
    private int studentCount;

    public MentorDTO(AcademicStaff mentor, int studentCount) {
        this.mentor = mentor;
        this.studentCount = studentCount;
    }

    public AcademicStaff getMentor() {
        return mentor;
    }

    public void setMentor(AcademicStaff mentor) {
        this.mentor = mentor;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }
}
