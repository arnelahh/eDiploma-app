package utils;

import dto.UpdateSecretaryDTO;

public class UpdateSecretaryValidator {

    public ValidationResult validate(UpdateSecretaryDTO dto) {
        ValidationResult vr = new ValidationResult();

        if (dto == null) {
            vr.add("Podaci nisu validni.");
            return vr;
        }

        if (dto.getAppUserId() <= 0) vr.add("Nedostaje AppUser ID.");
        if (dto.getAcademicStaffId() <= 0) vr.add("Nedostaje AcademicStaff ID.");

        if (isBlank(dto.getFirstName())) vr.add("Potrebno je unijeti Ime.");
        if (isBlank(dto.getLastName())) vr.add("Potrebno je unijeti Prezime.");
        if (isBlank(dto.getEmail())) vr.add("Potrebno je unijeti Email.");
        if (isBlank(dto.getUsername())) vr.add("Potrebno je unijeti Username.");

        if (!isBlank(dto.getEmail()) && !dto.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            vr.add("Email format nije ispravan.");
        }

        if (!isBlank(dto.getRawPassword())) {
            if (dto.getRawPassword().length() < 6) {
                vr.add("Lozinka mora imati najmanje 6 karaktera.");
            }
        }

        return vr;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
