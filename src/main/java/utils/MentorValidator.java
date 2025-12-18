package utils;

import dao.MentorDAO;
import model.AcademicStaff;
import model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MentorValidator {
    private static final MentorDAO mentorDAO = new MentorDAO();

    public static List<String> validateBasic(AcademicStaff mentor) {
        List<String> errors= new ArrayList<>();

        // Name validation
        if (mentor.getFirstName() == null || mentor.getFirstName().isBlank())
            errors.add("Potrebno je unijeti Ime");
        if (mentor.getLastName() == null || mentor.getLastName().isBlank())
            errors.add("Potrebno je unijeti Prezime");
        if (mentor.getTitle() == null || mentor.getTitle().isBlank())
            errors.add("Potreno je unijeti Zvanje");

        String email = mentor.getEmail();
        if (email == null || email.isBlank()) {
            errors.add("Potrebno je unijeti Email");
        } else if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            errors.add("Email format nije ispravan");
        }


        return errors;
    }

    public static CompletableFuture<List<String>> validateUniqueness(AcademicStaff academicStaff) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> errors = new ArrayList<>();


            // Check Email uniqueness
            if (mentorDAO.isEmailTaken(academicStaff.getEmail(), academicStaff.getId())) {
                errors.add("Email je vec iskorišten");
            }

            return errors;
        });
    }


}
