package utils;

import dao.MentorDAO;
import model.AcademicStaff;

/**
 * Service class for retrieving current dean information.
 * Provides centralized access to dean data for document generation.
 */
public class DeanService {
    
    private static final MentorDAO mentorDAO = new MentorDAO();
    
    /**
     * Gets the full formatted name of the current dean.
     * Format: "Prof. dr. sc. FirstName LastName"
     * 
     * @return Formatted dean name, or error message if no dean is set
     */
    public static String getCurrentDeanFullName() {
        try {
            AcademicStaff dean = mentorDAO.getCurrentDean();
            
            if (dean == null) {
                return "GREŠKA - Dekan nije postavljen";
            }
            
            return formatDeanName(dean);
            
        } catch (Exception e) {
            e.printStackTrace();
            return "GREŠKA - Dekan nije postavljen";
        }
    }
    
    /**
     * Formats the dean's name with title.
     * 
     * @param dean AcademicStaff object representing the dean
     * @return Formatted string: "Title FirstName LastName"
     */
    private static String formatDeanName(AcademicStaff dean) {
        StringBuilder name = new StringBuilder();
        
        if (dean.getTitle() != null && !dean.getTitle().isEmpty()) {
            name.append(dean.getTitle()).append(" ");
        }
        
        if (dean.getFirstName() != null && !dean.getFirstName().isEmpty()) {
            name.append(dean.getFirstName()).append(" ");
        }
        
        if (dean.getLastName() != null && !dean.getLastName().isEmpty()) {
            name.append(dean.getLastName());
        }
        
        return name.toString().trim();
    }
}
