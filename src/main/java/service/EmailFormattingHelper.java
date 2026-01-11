package service;

import dto.ThesisDetailsDTO;
import model.AcademicStaff;
import model.Student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Helper klasa za formatiranje imena, datuma i drugih podataka za email
 */
public class EmailFormattingHelper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Formatira puno ime akademskog osoblja sa titulom
     */
    public static String formatStaffFullName(AcademicStaff staff) {
        if (staff == null) {
            return "N/A";
        }

        String title = (staff.getTitle() != null && !staff.getTitle().isBlank())
                ? staff.getTitle() + " "
                : "";

        return title + staff.getFirstName() + " " + staff.getLastName();
    }

    /**
     * Formatira puno ime studenta
     */
    public static String formatStudentFullName(Student student) {
        if (student == null) {
            return "N/A";
        }

        return student.getFirstName() + " " + student.getLastName();
    }

    /**
     * Formatira datum u dd.MM.yyyy format
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "";
        }

        return date.format(DATE_FORMATTER);
    }

    /**
     * Formatira naslov rada ili vraća N/A
     */
    public static String formatThesisTitle(ThesisDetailsDTO thesisDetails) {
        if (thesisDetails == null || thesisDetails.getTitle() == null) {
            return "N/A";
        }

        return thesisDetails.getTitle();
    }

    /**
     * Generiše filename za PDF dokument
     */
    public static String generatePdfFileName(String documentType, String documentNumber, Integer documentId) {
        String baseFileName = documentType
                .replaceAll("[^a-zA-Z0-9čćđšžČĆĐŠŽ _-]", "")
                .replace(" ", "_");

        String suffix = (documentNumber != null && !documentNumber.isBlank())
                ? documentNumber
                : String.valueOf(documentId);

        return baseFileName + "_" + suffix + ".pdf";
    }
}
