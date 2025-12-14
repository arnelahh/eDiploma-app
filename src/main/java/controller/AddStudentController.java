package controller;

import dao.StudentDAO;
import dao.StudentStatusDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Student;
import model.StudentStatus;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AddStudentController {
    private final StudentDAO studentDAO = new StudentDAO();
    private final StudentStatusDAO statusDAO = new StudentStatusDAO();

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField fatherNameField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField birthPlaceField;
    @FXML private TextField municipalityField;
    @FXML private TextField countryField;
    @FXML private TextField indexNumberField;
    @FXML private TextField studyProgramField;
    @FXML private TextField ectsField;
    @FXML private TextField cycleField;
    @FXML private TextField cycleDurationField;
    @FXML private ComboBox<StudentStatus> statusComboBox;

    @FXML
    public void initialize() {
        List<StudentStatus> statuses = statusDAO.getAllStatuses();
        statusComboBox.getItems().addAll(statuses);
        statusComboBox.setConverter(new javafx.util.StringConverter<StudentStatus>() {
            @Override
            public String toString(StudentStatus status) {
                return status != null ? status.getName() : "";
            }

            @Override
            public StudentStatus fromString(String string) {
                return null;
            }
        });
        if (!statuses.isEmpty()) {
            statusComboBox.setValue(statuses.get(0));
        }
    }

    @FXML
    private void handleSave() {
        try {
            if (firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty() || indexNumberField.getText().trim().isEmpty()) {
                showError("Ime, prezime i broj indeksa su obavezna polja!");
                return;
            }
            Student student = new Student(0, firstNameField.getText().trim(), lastNameField.getText().trim(), fatherNameField.getText().trim(), 0, birthDatePicker.getValue(), birthPlaceField.getText().trim(), municipalityField.getText().trim(), countryField.getText().trim(), studyProgramField.getText().trim(), 0, 0, 0, statusComboBox.getValue(), emailField.getText().trim(), LocalDateTime.now(), LocalDateTime.now()
            );

            try {
                student.setIndexNumber(Integer.parseInt(indexNumberField.getText().trim()));
            } catch (NumberFormatException e) {
                showError("Broj indeksa mora biti broj!");
                return;
            }

            try {
                student.setECTS(ectsField.getText().trim().isEmpty() ? 0 : Integer.parseInt(ectsField.getText().trim()));
                student.setCycle(cycleField.getText().trim().isEmpty() ? 0 : Integer.parseInt(cycleField.getText().trim()));
                student.setCycleDuration(cycleDurationField.getText().trim().isEmpty() ? 0 : Integer.parseInt(cycleDurationField.getText().trim()));
            } catch (NumberFormatException e) {
                showError("ECTS, ciklus i trajanje moraju biti brojevi!");
                return;
            }
            studentDAO.insertStudent(student);

            showSuccess("Student je uspješno dodan!");
            handleBack();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Greška pri dodavanju studenta: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        NavigationContext.setTargetView(DashboardView.STUDENTS);
        SceneManager.show("/app/dashboard.fxml", "Dashboard");
    }

    @FXML
    private void handleCancel() {
        handleBack();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
