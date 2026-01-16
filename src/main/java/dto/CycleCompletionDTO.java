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
public class CycleCompletionDTO {
    private String studentFullName;
    private String studentGenitiveForm;
    private String fatherName;
    private LocalDate birthDate;
    private String birthPlace;
    private String municipality;
    private String country;
    private String studyProgram;
    private String cycle;
    private String cycleDuration;
    private String ects;
    private String academicTitle;
    private LocalDate cycleCompletionDate; // CycleCompletionDate - header datum
    private LocalDate defenseDate; // DefenseDate - datum u tekstu
    private String deanFullName;
}
