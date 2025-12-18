package utils;

import dto.CreateSecretaryDTO;

public class SecretaryValidator {
    public ValidationResult validate(CreateSecretaryDTO dto) {
        ValidationResult vr = new ValidationResult();

        if (isBlank(dto.getFirstName())) vr.add("Potrebno je unijeti Ime.");
        if (isBlank(dto.getLastName())) vr.add("Potrebno je unijeti Prezime");
        if (isBlank(dto.getEmail())) vr.add("Potrebno je unijeti email.");
        if (!isBlank(dto.getEmail()) && !isValidEmail(dto.getEmail())) vr.add("Email format nije validan.");

        if (isBlank(dto.getUsername())) vr.add("Potrebno je unijeti korisničko ime.");

        if (isBlank(dto.getRawPassword())) vr.add("Potrebno je unijeti password..");
        if (!isBlank(dto.getRawPassword()) && dto.getRawPassword().length() < 8)
            vr.add("Password mora biti najmanje 8 karaktera.");

        return vr;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() >= 6;
    }
}
