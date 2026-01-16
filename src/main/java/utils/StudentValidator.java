package utils;

import dao.StudentDAO;
import model.Student;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

public class StudentValidator {

    private static final StudentDAO studentDAO = new StudentDAO();

    /**
     * Synchronous validation: checks basic fields, formats, nulls.
     */
    public static ValidationResult validateBasic(Student student) {
        ValidationResult vr = new ValidationResult();

        // Name validation
        requireText(vr, student.getFirstName(), "Potrebno je unijeti Ime");
        requireText(vr, student.getLastName(), "Potrebno je unijeti Prezime");
        requireText(vr, student.getFatherName(), "Potrebno je unijeti Ime Oca");

        // Birth Date + age
        validateBirthDate(vr, student.getBirthDate());

        // Location fields
        requireText(vr, student.getBirthPlace(), "Potrebno je unijeti Mjesto Rođenja");
        requireText(vr, student.getMunicipality(), "Potrebno je unijeti Opštinu");
        requireText(vr, student.getCountry(), "Potrebno je unijeti Državu");

        // Academic info
        requireText(vr, student.getStudyProgram(), "Potrebno je unijeti Studijski Program");
        //requirePositive(vr, student.getCycle(), "Ciklus mora biti 1 ili više");
        requirePositive(vr, student.getCycleDuration(), "Trajanje mora biti pozitivno");
        requireNonNegative(vr, student.getECTS(), "ECTS bodovi moraju biti pozitivni");

        // Email
        validateEmail(vr, student.getEmail());

        // Status
        if (student.getStatus() == null) {
            vr.add("Potrebno je unijeti Status");
        }

        // Index number
        requirePositive(vr, student.getIndexNumber(), "INDEX mora biti pozitivan");

        return vr;
    }

    /**
     * Asynchronous uniqueness check for index number and email.
     */
    public static CompletableFuture<ValidationResult> validateUniqueness(Student student) {
        return CompletableFuture.supplyAsync(() -> {
            ValidationResult vr = new ValidationResult();

            if (studentDAO.isIndexNumberTaken(student.getIndexNumber(), student.getId())) {
                vr.add("Index je vec iskorišten");
            }

            if (studentDAO.isEmailTaken(student.getEmail(), student.getId())) {
                vr.add("Email je vec iskorišten");
            }

            return vr;
        });
    }

    // -------------------------
    // Private helper methods
    // -------------------------

    private static void requireText(ValidationResult vr, String value, String msg) {
        if (value == null || value.isBlank()) vr.add(msg);
    }

    private static void requirePositive(ValidationResult vr, int value, String msg) {
        if (value <= 0) vr.add(msg);
    }

    private static void requireNonNegative(ValidationResult vr, int value, String msg) {
        if (value < 0) vr.add(msg);
    }

    private static void validateEmail(ValidationResult vr, String email) {
        if (email == null || email.isBlank()) {
            vr.add("Potrebno je unijeti Email");
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            vr.add("Email format nije ispravan");
        }
    }

    private static void validateBirthDate(ValidationResult vr, LocalDate birth) {
        if (birth == null) {
            vr.add("Potrebno je unijeti Datum Rođenja");
            return;
        }

        LocalDate today = LocalDate.now();

        if (birth.isAfter(today)) {
            vr.add("Datum Rođenja ne može biti u budućnosti");
            return;
        }

        int age = today.getYear() - birth.getYear();
        if (birth.plusYears(age).isAfter(today)) age--;

        if (age < 16) {
            vr.add("Student mora imati više od 16 godina");
        }
    }
}
