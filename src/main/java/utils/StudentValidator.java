package utils;

import dao.StudentDAO;
import model.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StudentValidator {

    private static final StudentDAO studentDAO = new StudentDAO();

    /**
     * Synchronous validation: checks basic fields, formats, nulls.
     */
    public static List<String> validateBasic(Student student) {
        List<String> errors = new ArrayList<>();

        // Name validation
        if (student.getFirstName() == null || student.getFirstName().isBlank())
            errors.add("First name is required");
        if (student.getLastName() == null || student.getLastName().isBlank())
            errors.add("Last name is required");
        if (student.getFatherName() == null || student.getFatherName().isBlank())
            errors.add("Father's name is required");

        // Birth Date
        LocalDate birth = student.getBirthDate();
        if (birth == null) {
            errors.add("Birth date is required");
        } else {
            if (birth.isAfter(LocalDate.now())) {
                errors.add("Birth date cannot be in the future");
            }

            int age = LocalDate.now().getYear() - birth.getYear();
            if (birth.plusYears(age).isAfter(LocalDate.now())) {
                age--; // adjust if birthday hasn't occurred yet this year
            }
            if (age < 16) {
                errors.add("Student must be at least 16 years old");
            }
        }


        // Location fields
        if (student.getBirthPlace() == null || student.getBirthPlace().isBlank())
            errors.add("Birth place is required");
        if (student.getMunicipality() == null || student.getMunicipality().isBlank())
            errors.add("Municipality is required");
        if (student.getCountry() == null || student.getCountry().isBlank())
            errors.add("Country is required");

        // Academic info
        if (student.getStudyProgram() == null || student.getStudyProgram().isBlank())
            errors.add("Study program is required");
        if (student.getCycle() <= 0)
            errors.add("Cycle must be positive");
        if (student.getCycleDuration() <= 0)
            errors.add("Cycle duration must be positive");
        if (student.getECTS() < 0)
            errors.add("ECTS must be non-negative");

        // Email format
        String email = student.getEmail();
        if (email == null || email.isBlank()) {
            errors.add("Email is required");
        } else if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errors.add("Email format is invalid");
        }

        // Status
        if (student.getStatus() == null)
            errors.add("Student status must be selected");

        // Index number
        if (student.getIndexNumber() <= 0)
            errors.add("Index number must be positive");

        return errors;
    }

    /**
     * Asynchronous uniqueness check for index number and email
     */
    public static CompletableFuture<List<String>> validateUniqueness(Student student) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> errors = new ArrayList<>();

            // Check IndexNumber uniqueness
            if (studentDAO.isIndexNumberTaken(student.getIndexNumber(), student.getId())) {
                errors.add("Index number is already taken");
            }

            // Check Email uniqueness
            if (studentDAO.isEmailTaken(student.getEmail(), student.getId())) {
                errors.add("Email is already used");
            }

            return errors;
        });
    }
}
