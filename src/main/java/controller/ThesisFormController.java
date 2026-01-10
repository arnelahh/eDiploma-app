package controller;

import dao.*;
import dto.ThesisDetailsDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.*;
import utils.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.controlsfx.control.SearchableComboBox;

public class ThesisFormController {

    private enum Mode { CREATE, EDIT }

    private Mode mode;
    private Thesis thesis;

    private final AtomicInteger loadedCount = new AtomicInteger(0);
    private static final int TOTAL_LOADERS = 6;
    private Integer returnToThesisId = null;
    private boolean fieldsAlreadyFilled = false;

    private final ThesisValidator thesisValidator = new ThesisValidator();

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final AcademicStaffDAO mentorDAO = new AcademicStaffDAO();
    private final DepartmentDAO departmentDAO = new DepartmentDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final AppUserDAO secretaryDAO = new AppUserDAO();
    private final ThesisStatusDAO statusDAO = new ThesisStatusDAO();


    @FXML private Text formTitle;
    @FXML private Text formSubtitle;
    @FXML private TextField titleField;
    @FXML private DatePicker applicationDatePicker;
    @FXML private TextArea descriptionArea;
    @FXML private TextArea structureArea;
    @FXML private TextArea literatureArea;
    
    // Edit-only fields
    @FXML private VBox approvalDateContainer;
    @FXML private DatePicker approvalDatePicker;
    @FXML private VBox defenseDateContainer;
    @FXML private DatePicker defenseDatePicker;
    @FXML private VBox gradeContainer;
    @FXML private TextField gradeField;
    @FXML private VBox statusContainer;
    @FXML private ComboBox<ThesisStatus> statusComboBox;
    
    @FXML private SearchableComboBox<Student> studentComboBox;
    @FXML private SearchableComboBox<AcademicStaff> mentorComboBox;
    @FXML private ComboBox<Department> departmentComboBox;
    @FXML private SearchableComboBox<Subject> subjectComboBox;
    @FXML private SearchableComboBox<AcademicStaff> secretaryComboBox;
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
        loadStatuses();
    }

    private void loadStudents() {
        AsyncHelper.executeAsync(
            () -> studentDAO.getAllStudents(),
            students -> {
                studentComboBox.getItems().setAll(students);
                onDataLoaded();
            },
            error -> {
                GlobalErrorHandler.error("Greška pri učitavanju studenata", error);
                onDataLoaded();
            }
        );
    }

    private void loadMentors() {
        AsyncHelper.executeAsync(
            () -> mentorDAO.getAllActiveAcademicStaff(),
            mentors -> {
                mentorComboBox.getItems().setAll(mentors);
                onDataLoaded();
            },
            error -> {
                GlobalErrorHandler.error("Greška pri učitavanju mentora", error);
                onDataLoaded();
            }
        );
    }

    private void loadDepartments() {
        AsyncHelper.executeAsync(
            () -> departmentDAO.getAllDepartments(),
            departments -> {
                departmentComboBox.getItems().setAll(departments);
                onDataLoaded();
            },
            error -> {
                GlobalErrorHandler.error("Greška pri učitavanju odjela", error);
                onDataLoaded();
            }
        );
    }

    private void loadSubjects() {
        AsyncHelper.executeAsync(
            () -> subjectDAO.getAllSubjects(),
            subjects -> {
                subjectComboBox.getItems().setAll(subjects);
                onDataLoaded();
            },
            error -> {
                GlobalErrorHandler.error("Greška pri učitavanju predmeta", error);
                onDataLoaded();
            }
        );
    }

    private void loadSecretaries() {
        AsyncHelper.executeAsync(
            () -> secretaryDAO.getAllSecretariesAsStaff(),
            secretaries -> {
                secretaryComboBox.getItems().setAll(secretaries);
                onDataLoaded();
            },
            error -> {
                GlobalErrorHandler.error("Greška pri učitavanju sekretara", error);
                onDataLoaded();
            }
        );
    }

    private void loadStatuses() {
        AsyncHelper.executeAsync(
            () -> statusDAO.getAllThesisStatuses(),
            statuses -> {
                if (statusComboBox != null) {
                    statusComboBox.getItems().setAll(statuses);
                }
                onDataLoaded();
            },
            error -> {
                GlobalErrorHandler.error("Greška pri učitavanju statusa", error);
                onDataLoaded();
            }
        );
    }

    private void onDataLoaded() {
        int count = loadedCount.incrementAndGet();
        if (count >= TOTAL_LOADERS && mode == Mode.EDIT && thesis != null && !fieldsAlreadyFilled) {
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

        if (statusComboBox != null) {
            statusComboBox.setConverter(new javafx.util.StringConverter<>() {
                public String toString(ThesisStatus s) { return s != null ? s.getName() : ""; }
                public ThesisStatus fromString(String s) { return null; }
            });
        }
    }

    public void initCreate() {
        this.mode = Mode.CREATE;
        this.returnToThesisId = null;
        this.fieldsAlreadyFilled = false;
        if (formTitle != null) formTitle.setText("Dodaj novi završni rad");
        if (formSubtitle != null) formSubtitle.setText("Unesite podatke o novom završnom radu");
        toggleDeleteButton(false);
        toggleEditOnlyFields(false);
    }

    public void initEdit(Thesis thesis, Integer returnToThesisId) {
        this.mode = Mode.EDIT;
        this.thesis = thesis;
        this.returnToThesisId = returnToThesisId;
        this.fieldsAlreadyFilled = false;
        
        if (formTitle != null) formTitle.setText("Uredi završni rad");
        if (formSubtitle != null) formSubtitle.setText("Uredite podatke o završnom radu");
        toggleDeleteButton(true);
        toggleEditOnlyFields(true);

        javafx.application.Platform.runLater(this::attachUnlockOnWindowClose);
        
        // Try to fill fields immediately if data is already loaded
        if (loadedCount.get() >= TOTAL_LOADERS) {
            javafx.application.Platform.runLater(this::fillFields);
        }
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

    private void toggleEditOnlyFields(boolean visible) {
        if (approvalDateContainer != null) {
            approvalDateContainer.setVisible(visible);
            approvalDateContainer.setManaged(visible);
        }
        if (defenseDateContainer != null) {
            defenseDateContainer.setVisible(visible);
            defenseDateContainer.setManaged(visible);
        }
        if (gradeContainer != null) {
            gradeContainer.setVisible(visible);
            gradeContainer.setManaged(visible);
        }
        if (statusContainer != null) {
            statusContainer.setVisible(visible);
            statusContainer.setManaged(visible);
        }
    }

    private void fillFields() {
        if (fieldsAlreadyFilled || thesis == null) {
            return;
        }
        
        fieldsAlreadyFilled = true;
        
        // Basic fields
        if (titleField != null && thesis.getTitle() != null) {
            titleField.setText(thesis.getTitle());
        }
        if (applicationDatePicker != null) {
            applicationDatePicker.setValue(thesis.getApplicationDate());
        }

        // Edit-only fields
        if (approvalDatePicker != null) {
            approvalDatePicker.setValue(thesis.getApprovalDate());
        }
        if (defenseDatePicker != null) {
            defenseDatePicker.setValue(thesis.getDefenseDate());
        }
        if (gradeField != null && thesis.getGrade() != null && thesis.getGrade() > 0) {
            gradeField.setText(String.valueOf(thesis.getGrade()));
        }
        
        // Status selection
        if (statusComboBox != null && thesis.getStatusId() > 0) {
            int thesisStatusId = thesis.getStatusId();
            for (ThesisStatus status : statusComboBox.getItems()) {
                if (status.getId() == thesisStatusId) {
                    statusComboBox.setValue(status);
                    break;
                }
            }
        }
        
        // Description and Literature
        if (descriptionArea != null && thesis.getDescription() != null) {
            descriptionArea.setText(thesis.getDescription());
        }
        if (structureArea != null && thesis.getStructure() != null) {
            structureArea.setText(thesis.getStructure());
        }
        if (literatureArea != null && thesis.getLiterature() != null) {
            literatureArea.setText(thesis.getLiterature());
        }
        
        // ComboBox selections
        selectItemById(studentComboBox, thesis.getStudentId());
        selectItemById(mentorComboBox, thesis.getAcademicStaffId());
        selectItemById(departmentComboBox, thesis.getDepartmentId());
        selectItemById(subjectComboBox, thesis.getSubjectId());
        
        // Secretary selection - AppUser.Id is stored in thesis.secretaryId
        selectSecretaryByAppUserId(thesis.getSecretaryId());
    }

    private void selectSecretaryByAppUserId(Integer appUserId) {
        if (appUserId == null || secretaryComboBox == null || secretaryComboBox.getItems() == null) {
            return;
        }

        try {
            List<AcademicStaff> allSecretaries = secretaryComboBox.getItems();
            
            for (AcademicStaff secretary : allSecretaries) {
                try {
                    int secretaryAppUserId = secretaryDAO.getAppUserIdByAcademicStaffId(secretary.getId());
                    if (secretaryAppUserId == appUserId) {
                        secretaryComboBox.setValue(secretary);
                        return;
                    }
                } catch (Exception e) {
                    // Continue checking other secretaries
                }
            }
        } catch (Exception e) {
            System.err.println("Error selecting secretary: " + e.getMessage());
        }
    }

    private <T> void selectItemById(ComboBox<T> comboBox, Object id) {
        if (id == null || comboBox == null || comboBox.getItems() == null) {
            return;
        }

        for (T item : comboBox.getItems()) {
            try {
                java.lang.reflect.Method m = item.getClass().getMethod("getId");
                Object itemId = m.invoke(item);

                if (itemId != null && itemId.toString().equals(id.toString())) {
                    comboBox.setValue(item);
                    return;
                }
            } catch (Exception e) {
                // Ignorisanje greške pri refleksiji
            }
        }
    }

    @FXML
    private void handleSave() {
        ThesisDetailsDTO dto = extractDtoFromForm();
        ValidationResult result = thesisValidator.validate(dto);
        List<String> errors = result.getErrors();

        if (mode == Mode.EDIT) {
            validateGradeManually(errors);
        }

        if (!errors.isEmpty()) {
            showErrorList(errors);
            return;
        }

        try {
            if (mode == Mode.CREATE) {
                thesisDAO.insertThesis(buildThesisFromForm());
                GlobalErrorHandler.info("Završni rad je uspješno dodat!");
                returnToDashboard();
            } else {
                int userId = NavigationContext.getCurrentUser().getId();

                if (!thesisDAO.isLockedByUser(thesis.getId(), userId)) {
                    GlobalErrorHandler.warning("Vaša sesija za uređivanje je istekla ili je rad preuzeo drugi korisnik.");
                    back();
                    return;
                }

                updateThesisFromForm();
                thesisDAO.updateThesis(thesis);

                thesisDAO.unlockThesis(thesis.getId(), userId);

                GlobalErrorHandler.info("Završni rad je uspješno ažuriran!");
                MentorsController.requestRefresh();

                if (returnToThesisId != null) {
                    SceneManager.showWithData("/app/thesisDetails.fxml", "Detalji završnog rada",
                            (ThesisDetailsController controller) -> controller.initWithThesisId(returnToThesisId));
                } else {
                    back();
                }
            }
        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri snimanju završnog rada.", e);
        }
    }

    private void validateGradeManually(List<String> errors) {
        if (gradeField != null && gradeField.getText() != null && !gradeField.getText().trim().isEmpty()) {
            try {
                int grade = Integer.parseInt(gradeField.getText().trim());
                if (grade < 6 || grade > 10) {
                    errors.add("Ocjena mora biti između 6 i 10");
                }
            } catch (NumberFormatException e) {
                errors.add("Ocjena mora biti ispravan broj");
            }
        }
    }

    private ThesisDetailsDTO extractDtoFromForm() {
        return ThesisDetailsDTO.builder()
                .title(titleField.getText())
                .applicationDate(applicationDatePicker.getValue())
                .approvalDate(mode == Mode.EDIT && approvalDatePicker != null ? approvalDatePicker.getValue() : null)
                .defenseDate(mode == Mode.EDIT && defenseDatePicker != null ? defenseDatePicker.getValue() : null)
                .student(studentComboBox.getValue())
                .mentor(mentorComboBox.getValue())
                .department(departmentComboBox.getValue())
                .subject(subjectComboBox.getValue())
                .secretary(secretaryComboBox.getValue())
                .description(descriptionArea != null ? descriptionArea.getText() : null)
                .structure(structureArea != null ? structureArea.getText() : null)
                .literature(literatureArea != null ? literatureArea.getText() : null)
                .build();
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
        
        if (mode == Mode.EDIT) {
            if (approvalDatePicker != null) {
                t.setApprovalDate(approvalDatePicker.getValue());
            }
            if (defenseDatePicker != null) {
                t.setDefenseDate(defenseDatePicker.getValue());
            }
            if (gradeField != null && gradeField.getText() != null && !gradeField.getText().trim().isEmpty()) {
                try {
                    int grade = Integer.parseInt(gradeField.getText().trim());
                    t.setGrade(grade);
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            } else {
                t.setGrade(0);
            }
            if (statusComboBox != null && statusComboBox.getValue() != null) {
                t.setStatusId(statusComboBox.getValue().getId());
            }
        }

        if (studentComboBox.getValue() != null) t.setStudentId(studentComboBox.getValue().getId());
        if (mentorComboBox.getValue() != null) t.setAcademicStaffId(mentorComboBox.getValue().getId());
        if (departmentComboBox.getValue() != null) t.setDepartmentId(departmentComboBox.getValue().getId());
        if (subjectComboBox.getValue() != null) t.setSubjectId(subjectComboBox.getValue().getId());

        if (secretaryComboBox.getValue() != null) {
            try {
                int academicStaffId = secretaryComboBox.getValue().getId();
                int appUserId = secretaryDAO.getAppUserIdByAcademicStaffId(academicStaffId);
                t.setSecretaryId(appUserId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (descriptionArea != null && descriptionArea.getText() != null) {
            t.setDescription(descriptionArea.getText().trim());
        }
        if (structureArea != null && structureArea.getText() != null) {
            t.setStructure(structureArea.getText().trim());
        }
        if (literatureArea != null && literatureArea.getText() != null) {
            t.setLiterature(literatureArea.getText().trim());
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
                    GlobalErrorHandler.info("Završni rad je uspješno obrisan!");
                    MentorsController.requestRefresh();
                    returnToDashboard();
                } catch (Exception e) {
                    GlobalErrorHandler.error("Greška pri brisanju:");
                }
            }
        });
    }

    @FXML
    private void back() {
        if (mode == Mode.EDIT && thesis != null) {
            thesisDAO.unlockThesis(thesis.getId(), NavigationContext.getCurrentUser().getId());
        }
        if (returnToThesisId != null) {
            SceneManager.showWithData("/app/thesisDetails.fxml", "Detalji završnog rada",
                    (ThesisDetailsController controller) -> controller.initWithThesisId(returnToThesisId));
        } else {
            returnToDashboard();
        }
    }

    /**
     * Pomocna metoda koja vraća korisnika na odgovarajući dashboard na osnovu uloge
     */
    private void returnToDashboard() {
        AppUser currentUser = UserSession.getUser();
        
        NavigationContext.setTargetView(DashboardView.THESIS);
        
        if (currentUser != null && currentUser.getRole() != null) {
            String roleName = currentUser.getRole().getName();
            
            if ("SECRETARY".equalsIgnoreCase(roleName)) {
                SceneManager.show("/app/secretary-dashboard.fxml", "eDiploma - Sekretar");
                return;
            }
        }
        
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }


    private void showErrorList(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Neispravan unos");
        alert.setHeaderText("Molimo ispravite sledeće greške:");
        alert.setContentText("• " + String.join("\n• ", errors));
        alert.showAndWait();
    }

    private void attachUnlockOnWindowClose() {
        try {
            if (titleField == null || titleField.getScene() == null) return;

            var stage = (javafx.stage.Stage) titleField.getScene().getWindow();
            if (stage == null) return;

            stage.setOnCloseRequest(ev -> {
                try {
                    if (mode == Mode.EDIT && thesis != null && NavigationContext.getCurrentUser() != null) {
                        thesisDAO.unlockThesis(thesis.getId(), NavigationContext.getCurrentUser().getId());
                    }
                } catch (Exception ex) {}
            });
        } catch (Exception ignored) {}
    }

}
