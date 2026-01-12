package service;

import dto.ThesisDetailsDTO;
import model.AcademicStaff;
import model.Commission;
import model.Student;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper klasa za prikupljanje email recipijenata za različite tipove dokumenata
 */
public class EmailRecipientCollector {

    /**
     * Prikuplja osnovne recipijente: Student, Mentor, Sekretar
     */
    public static List<String> collectBasicRecipients(ThesisDetailsDTO thesisDetails) {
        Set<String> recipients = new LinkedHashSet<>();

        Student student = thesisDetails.getStudent();
        AcademicStaff mentor = thesisDetails.getMentor();
        AcademicStaff secretary = thesisDetails.getSecretary();

        if (student != null && isValidEmail(student.getEmail())) {
            recipients.add(student.getEmail());
        }

        if (mentor != null && isValidEmail(mentor.getEmail())) {
            recipients.add(mentor.getEmail());
        }

        if (secretary != null && isValidEmail(secretary.getEmail())) {
            recipients.add(secretary.getEmail());
        }

        return new java.util.ArrayList<>(recipients);
    }

    /**
     * Prikuplja recipijente za komisiju: predsjednik i član
     */
    public static List<String> collectCommissionRecipients(ThesisDetailsDTO thesisDetails, Commission commission) {
        Set<String> recipients = new LinkedHashSet<>();

        // Dodaj osnovne recipijente
        recipients.addAll(collectBasicRecipients(thesisDetails));

        // Dodaj članove komisije
        if (commission != null) {
            AcademicStaff chairman = commission.getMember1();
            AcademicStaff member = commission.getMember2();

            if (chairman != null && isValidEmail(chairman.getEmail())) {
                recipients.add(chairman.getEmail());
            }

            if (member != null && isValidEmail(member.getEmail())) {
                recipients.add(member.getEmail());
            }
        }

        return new java.util.ArrayList<>(recipients);
    }

    /**
     * Prikuplja sve recipijente uključujući zamjenskog člana (za Obavijest)
     */
    public static List<String> collectAllCommissionRecipients(ThesisDetailsDTO thesisDetails, Commission commission) {
        Set<String> recipients = new LinkedHashSet<>();

        // Dodaj osnovne recipijente
        recipients.addAll(collectBasicRecipients(thesisDetails));

        // Dodaj sve članove komisije
        if (commission != null) {
            AcademicStaff chairman = commission.getMember1();
            AcademicStaff member = commission.getMember2();
            AcademicStaff substitute = commission.getMember3();

            if (chairman != null && isValidEmail(chairman.getEmail())) {
                recipients.add(chairman.getEmail());
            }

            if (member != null && isValidEmail(member.getEmail())) {
                recipients.add(member.getEmail());
            }

            if (substitute != null && isValidEmail(substitute.getEmail())) {
                recipients.add(substitute.getEmail());
            }
        }

        return new java.util.ArrayList<>(recipients);
    }

    /**
     * Prikuplja samo studenta (za Uvjerenje o završenom ciklusu)
     */
    public static List<String> collectStudentOnly(ThesisDetailsDTO thesisDetails) {
        Student student = thesisDetails.getStudent();

        if (student != null && isValidEmail(student.getEmail())) {
            return List.of(student.getEmail());
        }

        return List.of();
    }

    /**
     * Validira email adresu
     */
    private static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        // Jednostavna email validacija
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
