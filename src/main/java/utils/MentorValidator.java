package utils;

import dao.MentorDAO;
import model.AcademicStaff;

import java.util.concurrent.CompletableFuture;

public class MentorValidator {

    // Nije static, omogućava lakše testiranje i injection ako zatreba
    private final MentorDAO mentorDAO = new MentorDAO();

    public ValidationResult validate(AcademicStaff mentor) {
        ValidationResult vr = new ValidationResult();

        if (isBlank(mentor.getFirstName()))
            vr.add("Potrebno je unijeti Ime.");

        if (isBlank(mentor.getLastName()))
            vr.add("Potrebno je unijeti Prezime.");

        if (isBlank(mentor.getTitle()))
            vr.add("Potrebno je unijeti Zvanje.");

        if (isBlank(mentor.getEmail())) {
            vr.add("Potrebno je unijeti Email.");
        } else if (!isValidEmail(mentor.getEmail())) {
            vr.add("Email format nije ispravan.");
        }

        return vr;
    }

    // Asinhrona provjera koja takođe vraća ValidationResult
    public CompletableFuture<ValidationResult> validateUniqueness(AcademicStaff mentor) {
        return CompletableFuture.supplyAsync(() -> {
            ValidationResult vr = new ValidationResult();

            // Provjera u bazi
            if (mentorDAO.isEmailTaken(mentor.getEmail(), mentor.getId())) {
                vr.add("Email je već iskorišten.");
            }

            return vr;
        });
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        // Zadržao sam tvoj regex jer je precizniji od jednostavne provjere
        return email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }
}