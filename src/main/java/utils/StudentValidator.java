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
            errors.add("Potrebno je unijeti Ime");
        if (student.getLastName() == null || student.getLastName().isBlank())
            errors.add("Potrebno je unijeti Prezime");
        if (student.getFatherName() == null || student.getFatherName().isBlank())
            errors.add("Potreno je unijeti Ime Oca");

        // Birth Date
        LocalDate birth = student.getBirthDate();
        if (birth == null) {
            errors.add("Potrebno je unijeti Datum Rođenja");
        } else {
            if (birth.isAfter(LocalDate.now())) {
                errors.add("Datum Rođenja ne može biti u budućnosti");
            }

            int age = LocalDate.now().getYear() - birth.getYear();
            if (birth.plusYears(age).isAfter(LocalDate.now())) {
                age--; // adjust if birthday hasn't occurred yet this year
            }
            if (age < 16) {
                errors.add("Student mora imati više od 16 godina");
            }
        }


        // Location fields
        if (student.getBirthPlace() == null || student.getBirthPlace().isBlank())
            errors.add("Potrebno je unijeti Mjesto Rođenja");
        if (student.getMunicipality() == null || student.getMunicipality().isBlank())
            errors.add("Potrebno je unijeti Opštinu");
        if (student.getCountry() == null || student.getCountry().isBlank())
            errors.add("Potrebno je unijeti Državu");

        // Academic info
        if (student.getStudyProgram() == null || student.getStudyProgram().isBlank())
            errors.add("Potrebno je unijeti Studijski Program");
        if (student.getCycle() <= 0)
            errors.add("Ciklus mora biti 1 ili više");
        if (student.getCycleDuration() <= 0)
            errors.add("Trajanje mora biti pozitivno");
        if (student.getECTS() < 0)
            errors.add("ECTS bodovi moraju biti pozitivni");

        // Email format
        String email = student.getEmail();
        if (email == null || email.isBlank()) {
            errors.add("Potrebno je unijeti Email");
        } else if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errors.add("Email format nije ispravan");
        }

        // Status
        if (student.getStatus() == null)
            errors.add("Potrebno je unijeti Status");

        // Index number
        if (student.getIndexNumber() <= 0)
            errors.add("INDEX mora biti pozitivan");

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
                errors.add("Index je vec iskorišten");
            }

            // Check Email uniqueness
            if (studentDAO.isEmailTaken(student.getEmail(), student.getId())) {
                errors.add("Email je vec iskorišten");
            }

            return errors;
        });
    }
}
