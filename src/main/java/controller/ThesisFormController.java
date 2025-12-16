package controller;

import dao.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import model.*;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ThesisFormController {

    private enum Mode { CREATE, EDIT }

    private Mode mode;
    private Thesis thesis;

    private final AtomicInteger loadedCount = new AtomicInteger(0);
    private static final int TOTAL_LOADERS = 6; // students, mentors, departments, subjects, statuses, secretaries

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AcademicStaffDAO mentorDAO = new AcademicStaffDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final ThesisStatusDAO statusDAO = new ThesisStatusDAO();
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
    @FXML private ComboBox<ThesisStatus> statusComboBox;
    @FXML private ComboBox<AppUser> secretaryComboBox;
    @FXML private Button deleteButton;
    @FXML private HBox deleteButtonContainer;

    @FXML
    public void initialize() {
        setupComboBoxConverters();
        loadStudents();
        loadMentors();
        loadDepartments();
        loadSubjects();
        loadStatuses();
        loadSecretaries();
    }

    private void loadStudents() {
        Task<List<Student>> task = new Task<List<Student>>() {
            @Override
            protected List<Student> call() throws Exception {
                return studentDAO.getAllStudents();
            }
        };
        task.setOnSucceeded(event -> {
            studentComboBox.getItems().clear();
            studentComboBox.getItems().addAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(event -> {
            Throwable ex = event.getSource().getException();
            System.out.println(ex);
            onDataLoaded();
        });
        new Thread(task).start();
    }

    private void loadMentors() {
        Task<List<AcademicStaff>> task = new  Task<List<AcademicStaff>>() {
            @Override
            protected List<AcademicStaff> call() throws Exception {
                return mentorDAO.getAllActiveAcademicStaff();
            }
        };
        task.setOnSucceeded(event -> {
            mentorComboBox.getItems().clear();
            mentorComboBox.getItems().addAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(event -> {
            Throwable ex = event.getSource().getException();
            System.out.println(ex);
            onDataLoaded();
        });
        new Thread(task).start();
    }

    private void loadDepartments() {
        Task<List<Department>> task=new  Task<List<Department>>() {
            @Override
            protected List<Department> call() throws Exception {
                return departmentDAO.getAllDepartments();
            }
        };
        task.setOnSucceeded(event -> {
            departmentComboBox.getItems().clear();
            departmentComboBox.getItems().addAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(event -> {
            Throwable ex = event.getSource().getException();
            System.out.println(ex);
            onDataLoaded();
        });
        new Thread(task).start();
    }

    private void loadSubjects() {
        Task<List<Subject>> task = new   Task<List<Subject>>() {
            @Override
            protected List<Subject> call() throws Exception {
                return subjectDAO.getAllSubjects();
            }
        };
        task.setOnSucceeded(event -> {
            subjectComboBox.getItems().clear();
            subjectComboBox.getItems().addAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(event -> {
            Throwable ex = event.getSource().getException();
            System.out.println(ex);
            onDataLoaded();
        });
        new Thread(task).start();
    }

    private void loadStatuses() {
        Task<List<ThesisStatus>> task = new  Task<List<ThesisStatus>>() {
            @Override
            protected List<ThesisStatus> call() throws Exception {
                return statusDAO.getAllThesisStatuses();
            }
        };
        task.setOnSucceeded(event -> {
            statusComboBox.getItems().clear();
            statusComboBox.getItems().addAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(event -> {
            Throwable ex = event.getSource().getException();
            System.out.println(ex);
            onDataLoaded();
        });
        new Thread(task).start();
    }

    private void loadSecretaries() {
        Task<List<AppUser>> task = new   Task<List<AppUser>>() {
            @Override
            protected List<AppUser> call() throws Exception {
                return secretaryDAO.getAllAppUsers();
            }
        };
        task.setOnSucceeded(event -> {
            secretaryComboBox.getItems().clear();
            secretaryComboBox.getItems().addAll(task.getValue());
            onDataLoaded();
        });
        task.setOnFailed(event -> {
            Throwable ex = event.getSource().getException();
            System.out.println(ex);
            onDataLoaded();
        });
        new Thread(task).start();
    }

    private void onDataLoaded() {
        int count = loadedCount.incrementAndGet();
        if (count == TOTAL_LOADERS && mode == Mode.EDIT && thesis != null) {
            // All data loaded, now we can fill the fields
            javafx.application.Platform.runLater(this::fillFields);
        }
    }

    private void setupComboBoxConverters() {
        studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
            public String toString(Student s) {
                return s != null ? s.getFirstName() + " " + s.getLastName() + " (" + s.getIndexNumber() + ")" : "";
            }
            public Student fromString(String s) { return null; }
        });

        mentorComboBox.setConverter(new javafx.util.StringConverter<AcademicStaff>() {
            public String toString(AcademicStaff m) {
                return m != null ? (m.getTitle() != null ? m.getTitle() + " " : "") + m.getFirstName() + " " + m.getLastName() : "";
            }
            public AcademicStaff fromString(String s) { return null; }
        });

        departmentComboBox.setConverter(new javafx.util.StringConverter<Department>() {
            public String toString(Department d) {
                return d != null ? d.getName() : "";
            }
            public Department fromString(String s) { return null; }
        });

        subjectComboBox.setConverter(new javafx.util.StringConverter<Subject>() {
            public String toString(Subject s) {
                return s != null ? s.getName() : "";
            }
            public Subject fromString(String s) { return null; }
        });

        statusComboBox.setConverter(new javafx.util.StringConverter<ThesisStatus>() {
            public String toString(ThesisStatus s) {
                return s != null ? s.getName() : "";
            }
            public ThesisStatus fromString(String s) { return null; }
        });

        secretaryComboBox.setConverter(new javafx.util.StringConverter<AppUser>() {
            public String toString(AppUser u) {
                return u != null ? u.getUsername() + " (" + u.getEmail() + ")" : "";
            }
            public AppUser fromString(String s) { return null; }
        });
    }

    public void initCreate() {
        this.mode = Mode.CREATE;

        if (formTitle != null) {
            formTitle.setText("Dodaj novi završni rad");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Unesite podatke o novom završnom radu");
        }

        if (deleteButton != null) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
        if (deleteButtonContainer != null) {
            deleteButtonContainer.setVisible(false);
            deleteButtonContainer.setManaged(false);
        }
    }

    public void initEdit(Thesis thesis) {
        this.mode = Mode.EDIT;
        this.thesis = thesis;

        if (formTitle != null) {
            formTitle.setText("Uredi završni rad");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Uredite podatke o završnom radu");
        }

        if (deleteButton != null) {
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        }
        if (deleteButtonContainer != null) {
            deleteButtonContainer.setVisible(true);
            deleteButtonContainer.setManaged(true);
        }

        // fillFields() will be called automatically by onDataLoaded() when all ComboBoxes are populated
    }

    private void fillFields() {
        titleField.setText(thesis.getTitle());
        applicationDatePicker.setValue(thesis.getApplicationDate());
        approvalDatePicker.setValue(thesis.getApprovalDate());
        defenseDatePicker.setValue(thesis.getDefenseDate());

        if (thesis.getGrade() != null) {
            gradeField.setText(thesis.getGrade().toString());
        }

        // Set selected items in combo boxes
        studentComboBox.getItems().stream()
                .filter(s -> s.getId() == thesis.getStudentId())
                .findFirst()
                .ifPresent(studentComboBox::setValue);

        mentorComboBox.getItems().stream()
                .filter(m -> m.getId() == thesis.getAcademicStaffId())
                .findFirst()
                .ifPresent(mentorComboBox::setValue);

        departmentComboBox.getItems().stream()
                .filter(d -> d.getId() == thesis.getDepartmentId())
                .findFirst()
                .ifPresent(departmentComboBox::setValue);

        subjectComboBox.getItems().stream()
                .filter(s -> s.getId() == thesis.getSubjectId())
                .findFirst()
                .ifPresent(subjectComboBox::setValue);

        statusComboBox.getItems().stream()
                .filter(s -> s.getId() == thesis.getStatusId())
                .findFirst()
                .ifPresent(statusComboBox::setValue);

        secretaryComboBox.getItems().stream()
                .filter(u -> u.getId() == thesis.getSecretaryId())
                .findFirst()
                .ifPresent(secretaryComboBox::setValue);
    }

    @FXML
    private void handleSave() {
        // Validation
        if (!validateFields()) {
            return;
        }

        try {
            if (mode == Mode.CREATE) {
                thesisDAO.insertThesis(buildThesis());
                show("Završni rad je uspješno dodat!", Alert.AlertType.INFORMATION);
            } else {
                updateThesis();
                thesisDAO.updateThesis(thesis);
                show("Završni rad je uspješno ažuriran!", Alert.AlertType.INFORMATION);
            }
            MentorsController.requestRefresh();
            back();
        } catch (Exception e) {
            show("Greška: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            show("Naslov rada je obavezno polje!", Alert.AlertType.WARNING);
            return false;
        }
        if (applicationDatePicker.getValue() == null) {
            show("Datum prijave je obavezan!", Alert.AlertType.WARNING);
            return false;
        }
        if (studentComboBox.getValue() == null) {
            show("Student je obavezan!", Alert.AlertType.WARNING);
            return false;
        }
        if (mentorComboBox.getValue() == null) {
            show("Mentor je obavezan!", Alert.AlertType.WARNING);
            return false;
        }
        if (departmentComboBox.getValue() == null) {
            show("Odjel je obavezan!", Alert.AlertType.WARNING);
            return false;
        }
        if (subjectComboBox.getValue() == null) {
            show("Predmet je obavezan!", Alert.AlertType.WARNING);
            return false;
        }
        if (statusComboBox.getValue() == null) {
            show("Status je obavezan!", Alert.AlertType.WARNING);
            return false;
        }
        if (secretaryComboBox.getValue() == null) {
            show("Sekretar je obavezan!", Alert.AlertType.WARNING);
            return false;
        }

        // Validate grade if present
        if (gradeField.getText() != null && !gradeField.getText().trim().isEmpty()) {
            try {
                double grade = Double.parseDouble(gradeField.getText().trim());
                if (grade < 6.0 || grade > 10.0) {
                    show("Ocjena mora biti između 6 i 10!", Alert.AlertType.WARNING);
                    return false;
                }
            } catch (NumberFormatException e) {
                show("Ocjena mora biti broj!", Alert.AlertType.WARNING);
                return false;
            }
        }

        return true;
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
                    back();
                } catch (Exception e) {
                    show("Greška pri brisanju: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private Thesis buildThesis() {
        Thesis newThesis = new Thesis();
        newThesis.setTitle(titleField.getText().trim());
        newThesis.setApplicationDate(applicationDatePicker.getValue());
        newThesis.setApprovalDate(approvalDatePicker.getValue());
        newThesis.setDefenseDate(defenseDatePicker.getValue());

        if (gradeField.getText() != null && !gradeField.getText().trim().isEmpty()) {
            newThesis.setGrade(new java.math.BigDecimal(gradeField.getText().trim()));
        }

        newThesis.setStudentId(studentComboBox.getValue().getId());
        newThesis.setAcademicStaffId(mentorComboBox.getValue().getId());
        newThesis.setDepartmentId(departmentComboBox.getValue().getId());
        newThesis.setSubjectId(subjectComboBox.getValue().getId());
        newThesis.setStatusId(statusComboBox.getValue().getId());
        newThesis.setSecretaryId(secretaryComboBox.getValue().getId());
        newThesis.setActive(true);

        return newThesis;
    }

    private void updateThesis() {
        thesis.setTitle(titleField.getText().trim());
        thesis.setApplicationDate(applicationDatePicker.getValue());
        thesis.setApprovalDate(approvalDatePicker.getValue());
        thesis.setDefenseDate(defenseDatePicker.getValue());

        if (gradeField.getText() != null && !gradeField.getText().trim().isEmpty()) {
            thesis.setGrade(new java.math.BigDecimal(gradeField.getText().trim()));
        } else {
            thesis.setGrade(null);
        }

        thesis.setStudentId(studentComboBox.getValue().getId());
        thesis.setAcademicStaffId(mentorComboBox.getValue().getId());
        thesis.setDepartmentId(departmentComboBox.getValue().getId());
        thesis.setSubjectId(subjectComboBox.getValue().getId());
        thesis.setStatusId(statusComboBox.getValue().getId());
        thesis.setSecretaryId(secretaryComboBox.getValue().getId());
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.THESIS);
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }

    private void show(String msg, Alert.AlertType type) {
        new Alert(type, msg).showAndWait();
    }
}
