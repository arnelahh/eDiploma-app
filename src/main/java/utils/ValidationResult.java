package utils;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private final List<String> errors = new ArrayList<>();

    public void add(String msg) { errors.add(msg); }
    public boolean isValid() { return errors.isEmpty(); }
    public List<String> getErrors() { return errors; }

    public String joined(String delimiter) {
        return String.join(delimiter, errors);
    }
}
