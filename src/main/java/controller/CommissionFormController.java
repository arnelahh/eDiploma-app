package controller;

import dao.AcademicStaffDAO;
import dto.ThesisDetailsDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import model.AcademicStaff;
import utils.SceneManager;

import java.util.List;

public class CommissionFormController {

    @FXML private Text formTitle;
    @FXML private Text formSubtitle;

    @FXML private ComboBox<AcademicStaff> chairmanComboBox;
    @FXML private TextField mentorField;
    @FXML private ComboBox<AcademicStaff> memberComboBox;
    @FXML private TextField secretaryField;
    @FXML private ComboBox<AcademicStaff> substituteComboBox;

    @FXML private ProgressIndicator loader;

    private final AcademicStaffDAO academicStaffDAO = new AcademicStaffDAO();

    private int thesisId;
    private ThesisDetailsDTO thesisDetails;
    private ThesisDetailsController.CommissionData existingCommission;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        setupComboBoxConverters();
        loadAcademicStaff();
    }

    private void setupComboBoxConverters() {
        javafx.util.StringConverter<AcademicStaff> converter = new javafx.util.StringConverter<>() {
            @Override
            public String toString(AcademicStaff staff) {
                if (staff == null) return "";
                String title = staff.getTitle() != null ? staff.getTitle() + " " : "";
                return title + staff.getFirstName() + " " + staff.getLastName();
            }

            @Override
            public AcademicStaff fromString(String s) {
                return null;
            }
        };

        chairmanComboBox.setConverter(converter);
        memberComboBox.setConverter(converter);
        substituteComboBox.setConverter(converter);
    }

    private void loadAcademicStaff() {
        Task<List<AcademicStaff>> task = new Task<>() {
            @Override
            protected List<AcademicStaff> call() throws Exception {
                return academicStaffDAO.getAllActiveAcademicStaff();
            }
        };

        if (loader != null) {
            loader.visibleProperty().bind(task.runningProperty());
        }

        task.setOnSucceeded(e -> {
            List<AcademicStaff> staffList = task.getValue();
            chairmanComboBox.getItems().clear();
            memberComboBox.getItems().clear();
            substituteComboBox.getItems().clear();

            chairmanComboBox.getItems().addAll(staffList);
            memberComboBox.getItems().addAll(staffList);
            substituteComboBox.getItems().addAll(staffList);

            // Ako je edit mode, popuni polja
            if (isEditMode && existingCommission != null) {
                fillExistingCommission();
            }
        });

        task.setOnFailed(e -> {
            showError("Greška pri učitavanju akademskog osoblja: " + task.getException().getMessage());
        });

        new Thread(task, "load-academic-staff").start();
    }

    public void initWithThesis(int thesisId, ThesisDetailsDTO details) {
        this.thesisId = thesisId;
        this.thesisDetails = details;
        this.isEditMode = false;

        if (formTitle != null) {
            formTitle.setText("Dodavanje komisije");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Unesite komisiju za završni rad");
        }

        // Postavi mentora (read-only)
        if (details != null && details.getMentor() != null) {
            String mentorText = formatStaffName(details.getMentor());
            mentorField.setText(mentorText);
        }

        // Postavi sekretara (read-only)
        if (details != null && details.getSecretary() != null) {
            secretaryField.setText(details.getSecretary().getUsername());
        }
    }

    public void initEditCommission(int thesisId, ThesisDetailsDTO details,
                                   ThesisDetailsController.CommissionData commission) {
        this.thesisId = thesisId;
        this.thesisDetails = details;
        this.existingCommission = commission;
        this.isEditMode = true;

        if (formTitle != null) {
            formTitle.setText("Uredi komisiju");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Uredite komisiju za završni rad");
        }

        // Postavi mentora (read-only)
        if (details != null && details.getMentor() != null) {
            String mentorText = formatStaffName(details.getMentor());
            mentorField.setText(mentorText);
        }

        // Postavi sekretara (read-only)
        if (details != null && details.getSecretary() != null) {
            secretaryField.setText(details.getSecretary().getUsername());
        }
    }

    private void fillExistingCommission() {
        if (existingCommission == null) return;

        // Predsjednik
        if (existingCommission.chairman != null) {
            chairmanComboBox.getItems().stream()
                    .filter(s -> s.getId() == existingCommission.chairman.getId())
                    .findFirst()
                    .ifPresent(chairmanComboBox::setValue);
        }

        // Član
        if (existingCommission.member != null) {
            memberComboBox.getItems().stream()
                    .filter(s -> s.getId() == existingCommission.member.getId())
                    .findFirst()
                    .ifPresent(memberComboBox::setValue);
        }

        // Zamjenski član
        if (existingCommission.substitute != null) {
            substituteComboBox.getItems().stream()
                    .filter(s -> s.getId() == existingCommission.substitute.getId())
                    .findFirst()
                    .ifPresent(substituteComboBox::setValue);
        }
    }

    private String formatStaffName(AcademicStaff staff) {
        if (staff == null) return "";
        String title = staff.getTitle() != null ? staff.getTitle() + " " : "";
        return title + staff.getFirstName() + " " + staff.getLastName();
    }

    @FXML
    private void handleSave() {
        // Validacija
        if (chairmanComboBox.getValue() == null) {
            showWarning("Morate izabrati predsjednika komisije!");
            return;
        }
        if (memberComboBox.getValue() == null) {
            showWarning("Morate izabrati člana komisije!");
            return;
        }

        // Kreiranje CommissionData objekta
        ThesisDetailsController.CommissionData commissionData = new ThesisDetailsController.CommissionData();
        commissionData.chairman = chairmanComboBox.getValue();
        commissionData.member = memberComboBox.getValue();
        commissionData.substitute = substituteComboBox.getValue();

        // Mentor iz thesis details
        if (thesisDetails != null) {
            commissionData.mentor = thesisDetails.getMentor();
        }

        // Vrati se na ThesisDetails sa podacima o komisiji
        SceneManager.showWithData(
                "/app/thesisDetails.fxml",
                "Detalji završnog rada",
                (ThesisDetailsController controller) -> {
                    controller.initWithThesisId(thesisId);
                    controller.setCommissionData(commissionData);
                }
        );
    }

    @FXML
    private void handleCancel() {
        // Vrati se na ThesisDetails bez izmjena
        SceneManager.showWithData(
                "/app/thesisDetails.fxml",
                "Detalji završnog rada",
                (ThesisDetailsController controller) -> {
                    controller.initWithThesisId(thesisId);
                }
        );
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }
}
