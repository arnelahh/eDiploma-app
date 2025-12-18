package utils;

import dto.CreateSecretaryDTO;

public class SecretaryValidator {
    public ValidationResult validate(CreateSecretaryDTO dto) {
        ValidationResult vr = new ValidationResult();

        if (isBlank(dto.getFirstName())) vr.add("First name is required.");
        if (isBlank(dto.getLastName())) vr.add("Last name is required.");
        if (isBlank(dto.getEmail())) vr.add("Email is required.");
        if (!isBlank(dto.getEmail()) && !isValidEmail(dto.getEmail())) vr.add("Email format is not valid.");

        if (isBlank(dto.getUsername())) vr.add("Username is required.");

        if (isBlank(dto.getRawPassword())) vr.add("Password is required.");
        if (!isBlank(dto.getRawPassword()) && dto.getRawPassword().length() < 8)
            vr.add("Password must be at least 8 characters.");

        // Optional: title can be empty, but you can validate if you want:
        // if (isBlank(dto.getTitle())) vr.add("Title is required.");

        return vr;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidEmail(String email) {
        // simple practical check (you can improve later)
        return email.contains("@") && email.contains(".") && email.length() >= 6;
    }
}
