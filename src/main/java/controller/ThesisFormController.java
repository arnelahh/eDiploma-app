package controller;

import dao.*;
import dto.ThesisDetailsDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import model.*;
import utils.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ThesisFormController {

    private enum Mode { CREATE, EDIT }

    private Mode mode;
    private Thesis thesis;

    private final AtomicInteger loadedCount = new AtomicInteger(0);
    private static final int TOTAL_LOADERS = 5;
    private Integer returnToThesisId = null;

    private final ThesisValidator thesisValidator = new ThesisValidator();

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AcademicStaffDAO mentorDAO = new AcademicStaffDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AppUserDAO secretaryDAO = new AppUserDAO();

    @FXML private Text formTitle;
    @FXML private Text formSubtitle;
    @FXML private TextField titleField;
    @FXML private DatePicker applicationDatePicker;
    @FXML private DatePicker approvalDatePicker;
    @FXML private DatePicker defenseDatePicker;
    @FXML private TextField gradeField;
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private ComboBox<AcademicStaff> mentorComboBox;
    @FXML private ComboBox<Department> departmentComboBox;
    @FXML private ComboBox<Subject> subjectComboBox;
    @FXML private ComboBox<AcademicStaff> secretaryComboBox;
    @FXML private Button deleteButton;
    @FXML private HBox deleteButtonContainer;

    @FXML
    public void initialize() {
        setupComboBoxConverters();
        loadAllData();
    }

    private void loadAllData() {
        loadStudents();
        loadMentors();
        loadDepartments();
        loadSubjects();
        loadSecretaries();
    }

    private void loadStudents() {
        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() throws Exception {
                return studentDAO.getAllStudents();
            }
        };
        task.setOnSucceeded(e -> {
            studentComboBox.getItems().setAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(e -> onDataLoaded());
        new Thread(task).start();
    }

    private void loadMentors() {
        Task<List<AcademicStaff>> task = new Task<>() {
            @Override
            protected List<AcademicStaff> call() throws Exception {
                return mentorDAO.getAllActiveAcademicStaff();
            }
        };
        task.setOnSucceeded(e -> {
            mentorComboBox.getItems().setAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(e -> onDataLoaded());
        new Thread(task).start();
    }

    private void loadDepartments() {
        Task<List<Department>> task = new Task<>() {
            @Override
            protected List<Department> call() throws Exception {
                return departmentDAO.getAllDepartments();
            }
        };
        task.setOnSucceeded(e -> {
            departmentComboBox.getItems().setAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(e -> onDataLoaded());
        new Thread(task).start();
    }

    private void loadSubjects() {
        Task<List<Subject>> task = new Task<>() {
            @Override
            protected List<Subject> call() throws Exception {
                return subjectDAO.getAllSubjects();
            }
        };
        task.setOnSucceeded(e -> {
            subjectComboBox.getItems().setAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(e -> onDataLoaded());
        new Thread(task).start();
    }

    private void loadSecretaries() {
        Task<List<AcademicStaff>> task = new Task<>() {
            @Override
            protected List<AcademicStaff> call() throws Exception {
                return secretaryDAO.getAllSecretariesAsStaff();
            }
        };
        task.setOnSucceeded(e -> {
            secretaryComboBox.getItems().setAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(e -> onDataLoaded());
        new Thread(task).start();
    }

    private void onDataLoaded() {
        int count = loadedCount.incrementAndGet();
        if (count == TOTAL_LOADERS && mode == Mode.EDIT && thesis != null) {
            javafx.application.Platform.runLater(this::fillFields);
        }
    }

    private void setupComboBoxConverters() {
        studentComboBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Student s) {
                return s != null ? s.getFirstName() + " " + s.getLastName() + " (" + s.getIndexNumber() + ")" : "";
            }
            public Student fromString(String s) { return null; }
        });

        mentorComboBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(AcademicStaff m) {
                return m != null ? (m.getTitle() != null ? m.getTitle() + " " : "") + m.getFirstName() + " " + m.getLastName() : "";
            }
            public AcademicStaff fromString(String s) { return null; }
        });

        departmentComboBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Department d) { return d != null ? d.getName() : ""; }
            public Department fromString(String s) { return null; }
        });

        subjectComboBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Subject s) { return s != null ? s.getName() : ""; }
            public Subject fromString(String s) { return null; }
        });

        secretaryComboBox.setConverter(new javafx.util.StringConverter<>() {
            public String toString(AcademicStaff u) {
                return u != null ? (u.getTitle() != null ? u.getTitle() + " " : "") + u.getFirstName() + " " + u.getLastName() : "";
            }
            public AcademicStaff fromString(String s) { return null; }
        });
    }

    public void initCreate() {
        this.mode = Mode.CREATE;
        this.returnToThesisId = null;
        if (formTitle != null) formTitle.setText("Dodaj novi završni rad");
        if (formSubtitle != null) formSubtitle.setText("Unesite podatke o novom završnom radu");
        toggleDeleteButton(false);
    }

    public void initEdit(Thesis thesis, Integer returnToThesisId) {
        this.mode = Mode.EDIT;
        this.thesis = thesis;
        this.returnToThesisId = returnToThesisId;
        if (formTitle != null) formTitle.setText("Uredi završni rad");
        if (formSubtitle != null) formSubtitle.setText("Uredite podatke o završnom radu");
        toggleDeleteButton(true);
    }

    private void toggleDeleteButton(boolean visible) {
        if (deleteButton != null) {
            deleteButton.setVisible(visible);
            deleteButton.setManaged(visible);
        }
        if (deleteButtonContainer != null) {
            deleteButtonContainer.setVisible(visible);
            deleteButtonContainer.setManaged(visible);
        }
    }

    private void fillFields() {
        System.out.println("=== FILLING FIELDS ===");
        System.out.println("Thesis ID: " + thesis.getId());
        System.out.println("Student ID: " + thesis.getStudentId());
        System.out.println("Mentor ID: " + thesis.getAcademicStaffId());
        System.out.println("Secretary ID (AcademicStaff): " + thesis.getSecretaryId());
        System.out.println("Department ID: " + thesis.getDepartmentId());
        System.out.println("Subject ID: " + thesis.getSubjectId());

        titleField.setText(thesis.getTitle());
        applicationDatePicker.setValue(thesis.getApplicationDate());
        approvalDatePicker.setValue(thesis.getApprovalDate());
        defenseDatePicker.setValue(thesis.getDefenseDate());
        if (thesis.getGrade() != null) gradeField.setText(thesis.getGrade().toString());

        selectItemById(studentComboBox, thesis.getStudentId());
        selectItemById(mentorComboBox, thesis.getAcademicStaffId());
        selectItemById(departmentComboBox, thesis.getDepartmentId());
        selectItemById(subjectComboBox, thesis.getSubjectId());

        // ===== KLJUČNA ISPRAVKA: Secretary ID je sada AcademicStaff ID =====
        selectItemById(secretaryComboBox, thesis.getSecretaryId());

        System.out.println("=== FIELDS FILLED ===");
    }

    // ===== ISPRAVLJENA selectItemById metoda =====
    private <T> void selectItemById(ComboBox<T> comboBox, Object id) {
        if (id == null || comboBox == null || comboBox.getItems() == null) {
            System.out.println("WARNING: Cannot select - null value or empty ComboBox");
            return;
        }

        System.out.println("Trying to select ID: " + id + " in " + comboBox.getId());

        for (T item : comboBox.getItems()) {
            try {
                java.lang.reflect.Method m = item.getClass().getMethod("getId");
                Object itemId = m.invoke(item);

                if (itemId != null) {
                    // Poređenje kao String da izbjegnemo Integer/Long probleme
                    if (itemId.toString().equals(id.toString())) {
                        comboBox.setValue(item);
                        System.out.println("✓ Successfully selected item with ID: " + itemId);
                        return;
                    }
                }
            } catch (Exception e) {
                System.err.println("Error selecting item: " + e.getMessage());
            }
        }

        System.out.println("✗ Item with ID " + id + " NOT FOUND in ComboBox");
    }

    @FXML
    private void handleSave() {
        ThesisDetailsDTO dto = extractDtoFromForm();
        ValidationResult result = thesisValidator.validate(dto);
        List<String> errors = result.getErrors();
        validateGradeManually(errors);

        if (!errors.isEmpty()) {
            showErrorList(errors);
            return;
        }

        try {
            if (mode == Mode.CREATE) {
                thesisDAO.insertThesis(buildThesisFromForm());
                show("Završni rad je uspješno dodat!", Alert.AlertType.INFORMATION);
                NavigationContext.setTargetView(DashboardView.THESIS);
                SceneManager.show("/app/dashboard.fxml", "eDiploma");
            } else {
                updateThesisFromForm();
                thesisDAO.updateThesis(thesis);
                show("Završni rad je uspješno ažuriran!", Alert.AlertType.INFORMATION);
                MentorsController.requestRefresh();

                if (returnToThesisId != null) {
                    SceneManager.showWithData("/app/thesisDetails.fxml", "Detalji završnog rada",
                            (ThesisDetailsController controller) -> controller.initWithThesisId(returnToThesisId));
                } else {
                    back();
                }
            }
        } catch (Exception e) {
            show("Greška: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private ThesisDetailsDTO extractDtoFromForm() {
        return ThesisDetailsDTO.builder()
                .title(titleField.getText())
                .applicationDate(applicationDatePicker.getValue())
                .approvalDate(approvalDatePicker.getValue())
                .defenseDate(defenseDatePicker.getValue())
                .student(studentComboBox.getValue())
                .mentor(mentorComboBox.getValue())
                .department(departmentComboBox.getValue())
                .subject(subjectComboBox.getValue())
                .secretary(secretaryComboBox.getValue())
                .build();
    }

    private void validateGradeManually(List<String> errors) {
        if (gradeField.getText() != null && !gradeField.getText().trim().isEmpty()) {
            try {
                double grade = Double.parseDouble(gradeField.getText().trim());
                if (grade < 6.0 || grade > 10.0) {
                    errors.add("Ocjena mora biti između 6 i 10");
                }
            } catch (NumberFormatException e) {
                errors.add("Ocjena mora biti ispravan broj");
            }
        }
    }

    private Thesis buildThesisFromForm() {
        Thesis newThesis = new Thesis();
        fillThesisData(newThesis);
        newThesis.setActive(true);
        return newThesis;
    }

    private void updateThesisFromForm() {
        fillThesisData(this.thesis);
    }

    private void fillThesisData(Thesis t) {
        t.setTitle(titleField.getText().trim());
        t.setApplicationDate(applicationDatePicker.getValue());
        t.setApprovalDate(approvalDatePicker.getValue());
        t.setDefenseDate(defenseDatePicker.getValue());

        if (gradeField.getText() != null && !gradeField.getText().trim().isEmpty()) {
            t.setGrade(new BigDecimal(gradeField.getText().trim()));
        } else {
            t.setGrade(null);
        }

        if (studentComboBox.getValue() != null) t.setStudentId(studentComboBox.getValue().getId());
        if (mentorComboBox.getValue() != null) t.setAcademicStaffId(mentorComboBox.getValue().getId());
        if (departmentComboBox.getValue() != null) t.setDepartmentId(departmentComboBox.getValue().getId());
        if (subjectComboBox.getValue() != null) t.setSubjectId(subjectComboBox.getValue().getId());

        // ===== KLJUČNA ISPRAVKA: Konvertujemo AcademicStaff ID u AppUser ID =====
        if (secretaryComboBox.getValue() != null) {
            try {
                int academicStaffId = secretaryComboBox.getValue().getId();
                int appUserId = secretaryDAO.getAppUserIdByAcademicStaffId(academicStaffId);
                t.setSecretaryId(appUserId); // Čuvamo AppUser ID u bazu
                System.out.println("Secretary - AcademicStaff ID: " + academicStaffId + " -> AppUser ID: " + appUserId);
            } catch (Exception e) {
                System.err.println("Error converting secretary ID: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrda brisanja");
        confirm.setHeaderText("Da li ste sigurni da želite obrisati ovaj završni rad?");
        confirm.setContentText("Ova akcija se ne može poništiti.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    thesisDAO.deleteThesis(thesis.getId());
                    show("Završni rad je uspješno obrisan!", Alert.AlertType.INFORMATION);
                    MentorsController.requestRefresh();
                    NavigationContext.setTargetView(DashboardView.THESIS);
                    SceneManager.show("/app/dashboard.fxml", "eDiploma");
                } catch (Exception e) {
                    show("Greška pri brisanju: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void back() {
        if (returnToThesisId != null) {
            SceneManager.showWithData("/app/thesisDetails.fxml", "Detalji završnog rada",
                    (ThesisDetailsController controller) -> controller.initWithThesisId(returnToThesisId));
        } else {
            NavigationContext.setTargetView(DashboardView.THESIS);
            SceneManager.show("/app/dashboard.fxml", "eDiploma");
        }
    }

    private void show(String msg, Alert.AlertType type) {
        new Alert(type, msg).showAndWait();
    }

    private void showErrorList(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Neispravan unos");
        alert.setHeaderText("Molimo ispravite sljedeće greške:");
        alert.setContentText("• " + String.join("\n• ", errors));
        alert.showAndWait();
    }
}