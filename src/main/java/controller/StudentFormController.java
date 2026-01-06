package controller;

import dao.StudentDAO;
import dao.StudentStatusDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import model.Student;
import model.StudentStatus;
import utils.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class StudentFormController {

    private enum Mode { CREATE, EDIT }

    private Mode mode;
    private Student student;

    private final StudentDAO studentDAO = new StudentDAO();
    private final StudentStatusDAO statusDAO = new StudentStatusDAO();

    @FXML private Text formTitle;
    @FXML private Text formSubtitle;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField fatherNameField;
    @FXML private TextField emailField;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField birthPlaceField;
    @FXML private TextField municipalityField;
    @FXML private TextField countryField;
    @FXML private TextField indexNumberField;
    @FXML private ComboBox<String> studyProgramComboBox;
    @FXML private TextField ectsField;
    @FXML private ComboBox<String> cycleComboBox;
    @FXML private TextField cycleDurationField;
    @FXML private ComboBox<StudentStatus> statusComboBox;
    @FXML private Button deleteButton;
    @FXML private ProgressIndicator loader;

    @FXML
    public void initialize() {
        studyProgramComboBox.getItems().addAll(
                "Softversko inženjerstvo",
                "Proizvodni biznis",
                "Građevinarstvo"
        );

        cycleComboBox.getItems().addAll(
                "Prvi ciklus",
                "Drugi ciklus",
                "Treći ciklus"
        );

        List<StudentStatus> statuses = statusDAO.getAllStatuses();
        statusComboBox.getItems().addAll(statuses);
        statusComboBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(StudentStatus s) { return s != null ? s.getName() : ""; }
            public StudentStatus fromString(String s) { return null; }
        });

        if (loader != null) loader.setVisible(false);
    }

    public void initCreate() {
        this.mode = Mode.CREATE;
        formTitle.setText("Dodaj novog studenta");
        formSubtitle.setText("Unesite podatke o novom studentu");
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
    }

    public void initEdit(Student student) {
        this.mode = Mode.EDIT;
        this.student = student;
        formTitle.setText("Uredi studenta");
        formSubtitle.setText("Uredite podatke o studentu");
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);
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
        studyProgramComboBox.setValue(student.getStudyProgram());
        ectsField.setText(String.valueOf(student.getECTS()));
        String cycleStr = student.getCycle() == 1 ? "Prvi ciklus" :
                student.getCycle() == 2 ? "Drugi ciklus" : "Treći ciklus";
        cycleComboBox.setValue(cycleStr);
        cycleDurationField.setText(String.valueOf(student.getCycleDuration()));
        statusComboBox.setValue(student.getStatus());
    }

    @FXML
    private void handleSave() {
        final Student studentToSave;

        try {
            studentToSave = buildStudent();
        } catch (Exception e) {
            GlobalErrorHandler.error("Greška: Provjerite unos brojeva (INDEX/ECTS/Trajanje).");
            return;
        }

        ValidationResult basicVr = StudentValidator.validateBasic(studentToSave);
        if (!basicVr.isValid()) {
            GlobalErrorHandler.error(basicVr.joined("\n"));
            return;
        }

        setBusy(true);

        StudentValidator.validateUniqueness(studentToSave).thenAccept(uniqVr -> {
            if (!uniqVr.isValid()) {
                Platform.runLater(() -> {
                    setBusy(false);
                    GlobalErrorHandler.error(uniqVr.joined("\n"));
                });
                return;
            }

            // Korištenje AsyncHelper umjesto direktnog Task-a
            AsyncHelper.executeAsyncVoid(
                () -> {
                    if (mode == Mode.CREATE) {
                        studentDAO.insertStudent(studentToSave);
                    } else {
                        Platform.runLater(() -> updateStudent());
                        studentDAO.updateStudent(studentToSave);
                    }
                },
                () -> {
                    setBusy(false);
                    back();
                },
                error -> {
                    setBusy(false);
                    GlobalErrorHandler.error("Greška pri snimanju.", error);
                }
            );

        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                setBusy(false);
                GlobalErrorHandler.error("Greška pri validaciji.", ex);
            });
            return null;
        });
    }

    private void setBusy(boolean busy) {
        if (loader != null) loader.setVisible(busy);

        if (emailField != null) emailField.setDisable(busy);
        if (indexNumberField != null) indexNumberField.setDisable(busy);
        if (deleteButton != null) deleteButton.setDisable(busy);
    }

    @FXML
    private void handleDelete() {
        if (student == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrda brisanja");
        confirm.setHeaderText("Obrisati studenta?");
        confirm.setContentText("Da li ste sigurni da želite obrisati studenta " +
                student.getFirstName() + " " + student.getLastName() + "?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                studentDAO.deleteStudent(student.getId());
                back();
            } catch (Exception e) {
                GlobalErrorHandler.error("Greška pri brisanju.", e);
            }
        }
    }

    private int getCycleFromComboBox() {
        String selected = cycleComboBox.getValue();
        if (selected == null) return 1;
        return switch (selected) {
            case "Drugi ciklus" -> 2;
            case "Treći ciklus" -> 3;
            default -> 1;
        };
    }

    private Student buildStudent() {
        return new Student(
                mode == Mode.EDIT ? student.getId() : 0,
                firstNameField.getText(),
                lastNameField.getText(),
                fatherNameField.getText(),
                Integer.parseInt(indexNumberField.getText()),
                birthDatePicker.getValue(),
                birthPlaceField.getText(),
                municipalityField.getText(),
                countryField.getText(),
                studyProgramComboBox.getValue(),
                Integer.parseInt(ectsField.getText()),
                getCycleFromComboBox(),
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
        student.setStudyProgram(studyProgramComboBox.getValue());
        student.setECTS(Integer.parseInt(ectsField.getText()));
        student.setCycle(getCycleFromComboBox());
        student.setCycleDuration(Integer.parseInt(cycleDurationField.getText()));
        student.setStatus(statusComboBox.getValue());
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.STUDENTS);
        SceneManager.show("/app/dashboard.fxml", "Dashboard");
    }
}