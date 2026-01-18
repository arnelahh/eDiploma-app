package utils;

import model.AcademicStaff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommissionValidator {

    /**
     * Validira da li su svi članovi komisije različiti.
     * Isti profesor ne može biti npr. i predsjednik i član istovremeno.
     *
     * @param chairman Predsjednik komisije (obavezan)
     * @param mentor Mentor (obavezan)
     * @param member Član komisije (obavezan)
     * @param substitute Zamjenski član (opciono)
     * @param secretary Sekretar (obavezan)
     * @return ValidationResult sa listom grešaka
     */
    public ValidationResult validateCommission(
            AcademicStaff chairman,
            AcademicStaff mentor,
            AcademicStaff member,
            AcademicStaff substitute,
            AcademicStaff secretary
    ) {
        ValidationResult result = new ValidationResult();

        // Provjera da li su sva obavezna polja popunjena
        if (chairman == null) {
            result.add("Morate izabrati predsjednika komisije!");
        }
        if (mentor == null) {
            result.add("Morate izabrati mentora!");
        }
        if (member == null) {
            result.add("Morate izabrati člana komisije!");
        }
        if (secretary == null) {
            result.add("Morate izabrati sekretara!");
        }

        // Ako neko od obaveznih polja nedostaje, nema smisla dalje provjeravati
        if (!result.isValid()) {
            return result;
        }

        // Lista svih članova sa njihovim ulogama (osim substitute koji je opcioni)
        List<MemberWithRole> members = new ArrayList<>();
        members.add(new MemberWithRole(chairman, "Predsjednik"));
        members.add(new MemberWithRole(mentor, "Mentor"));
        members.add(new MemberWithRole(member, "Član"));
        members.add(new MemberWithRole(secretary, "Sekretar"));

        if (substitute != null) {
            members.add(new MemberWithRole(substitute, "Zamjenski član"));
        }

        // Provjera duplikata
        Set<Integer> seenIds = new HashSet<>();
        
        for (MemberWithRole m : members) {
            int id = m.staff.getId();
            
            if (seenIds.contains(id)) {
                // Nađen duplikat - pronađi sve uloge za ovog člana
                List<String> roles = new ArrayList<>();
                for (MemberWithRole m2 : members) {
                    if (m2.staff.getId() == id) {
                        roles.add(m2.role);
                    }
                }
                
                String fullName = getFullName(m.staff);
                result.add(String.format(
                    "%s ne može biti višečlan komisije. Trenutno izabran/a kao: %s",
                    fullName,
                    String.join(", ", roles)
                ));
                
                // Prestani provjeravati nakon što pronađeš duplikat
                break;
            }
            
            seenIds.add(id);
        }

        return result;
    }

    /**
     * Helper klasa za čuvanje člana komisije sa ulogom
     */
    private static class MemberWithRole {
        final AcademicStaff staff;
        final String role;

        MemberWithRole(AcademicStaff staff, String role) {
            this.staff = staff;
            this.role = role;
        }
    }

    private String getFullName(AcademicStaff staff) {
        String title = staff.getTitle() != null && !staff.getTitle().trim().isEmpty() 
            ? staff.getTitle() + " " 
            : "";
        return title + staff.getFirstName() + " " + staff.getLastName();
    }
}
