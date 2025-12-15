package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
@AllArgsConstructor
@Data
@Builder
public class Student {
    private int Id;
    private String FirstName;
    private String LastName;
    private String FatherName;
    private int IndexNumber;
    private LocalDate BirthDate;
    private String BirthPlace;
    private String Municipality;
    private String Country;
    private String StudyProgram;
    private int ECTS;
    private int Cycle;
    private int CycleDuration;
    private StudentStatus Status;
    private String Email;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
}