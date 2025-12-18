package utils;

import dto.ThesisDetailsDTO;

public class ThesisValidator {

    public ValidationResult validate(ThesisDetailsDTO dto) {
        ValidationResult vr = new ValidationResult();

        if (isBlank(dto.getTitle()))
            vr.add("Potrebno je unijeti Naslov.");

        if (isNull(dto.getApplicationDate()))
            vr.add("Potrebno je unijeti Datum prijave.");

        if (isNull(dto.getStudent()))
            vr.add("Potrebno je unijeti Studenta.");

        if (isNull(dto.getMentor()))
            vr.add("Potrebno je unijeti Mentora.");

        if (isNull(dto.getDepartment()))
            vr.add("Potrebno je unijeti Katedru.");

        if (isNull(dto.getSubject()))
            vr.add("Potrebno je unijeti Predmet.");

        // U DTO-u status je String, pa koristimo isBlank
        if (isBlank(dto.getStatus()))
            vr.add("Potrebno je unijeti Status.");

        if (isNull(dto.getSecretary()))
            vr.add("Potrebno je unijeti Sekretara.");

        return vr;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isNull(Object o) {
        return o == null;
    }
}