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

import java.time.LocalDateTime;
import java.util.List;

public class StudentFormController {

    private enum Mode { CREATE, EDIT }

    private Mode mode;
    private Student student;

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
        statusComboBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(StudentStatus s) {
                return s != null ? s.getName() : "";
            }
            public StudentStatus fromString(String s) { return null; }
        });
    }

    public void initCreate() {
        this.mode = Mode.CREATE;
    }

    public void initEdit(Student student) {
        this.mode = Mode.EDIT;
        this.student = student;
        fillFields();
    }

    private void fillFields() {
        firstNameField.setText(student.getFirstName());
        lastNameField.setText(student.getLastName());
        fatherNameField.setText(student.getFatherName());
        emailField.setText(student.getEmail());
        birthDatePicker.setValue(student.getBirthDate());
        birthPlaceField.setText(student.getBirthPlace());
        municipalityField.setText(student.getMunicipality());
        countryField.setText(student.getCountry());
        indexNumberField.setText(String.valueOf(student.getIndexNumber()));
        studyProgramField.setText(student.getStudyProgram());
        ectsField.setText(String.valueOf(student.getECTS()));
        cycleField.setText(String.valueOf(student.getCycle()));
        cycleDurationField.setText(String.valueOf(student.getCycleDuration()));
        statusComboBox.setValue(student.getStatus());
    }

    @FXML
    private void handleSave() {
        try {
            if (mode == Mode.CREATE) {
                studentDAO.insertStudent(buildStudent());
            } else {
                updateStudent();
                studentDAO.updateStudent(student);
            }
            back();
        } catch (Exception e) {
            show("Greška: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Student buildStudent() {
        return new Student(
                0,
                firstNameField.getText(),
                lastNameField.getText(),
                fatherNameField.getText(),
                Integer.parseInt(indexNumberField.getText()),
                birthDatePicker.getValue(),
                birthPlaceField.getText(),
                municipalityField.getText(),
                countryField.getText(),
                studyProgramField.getText(),
                Integer.parseInt(ectsField.getText()),
                Integer.parseInt(cycleField.getText()),
                Integer.parseInt(cycleDurationField.getText()),
                statusComboBox.getValue(),
                emailField.getText(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private void updateStudent() {
        student.setFirstName(firstNameField.getText());
        student.setLastName(lastNameField.getText());
        student.setFatherName(fatherNameField.getText());
        student.setEmail(emailField.getText());
        student.setBirthDate(birthDatePicker.getValue());
        student.setBirthPlace(birthPlaceField.getText());
        student.setMunicipality(municipalityField.getText());
        student.setCountry(countryField.getText());
        student.setIndexNumber(Integer.parseInt(indexNumberField.getText()));
        student.setStudyProgram(studyProgramField.getText());
        student.setECTS(Integer.parseInt(ectsField.getText()));
        student.setCycle(Integer.parseInt(cycleField.getText()));
        student.setCycleDuration(Integer.parseInt(cycleDurationField.getText()));
        student.setStatus(statusComboBox.getValue());
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.STUDENTS);
        SceneManager.show("/app/dashboard.fxml", "Dashboard");
    }

    private void show(String msg, Alert.AlertType type) {
        new Alert(type, msg).showAndWait();
    }
}
