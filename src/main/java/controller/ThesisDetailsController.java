package controller;

import dao.CommissionDAO;
import dao.ThesisDAO;
import dto.ThesisDetailsDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Commission;
import model.Thesis;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;
import utils.GlobalErrorHandler;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ThesisDetailsController {

    @FXML private Text titleValue;
    @FXML private Text descriptionValue;
    @FXML private Text subjectValue;
    @FXML private Text statusValue;
    @FXML private Text applicationDateValue;
    @FXML private Text defenseDateValue;
    @FXML private Text gradeValue;
    @FXML private Text studentName;
    @FXML private Text studentIndex;
    @FXML private Text studentEmail;

    @FXML private Text mentorTitle;
    @FXML private Text mentorName;
    @FXML private Text mentorEmail;

    @FXML private VBox commissionNotFormedBox;
    @FXML private VBox commissionFormedBox;
    @FXML private Text chairmanName;
    @FXML private Text memberName;
    @FXML private Text mentorCommissionName;
    @FXML private Text secretaryCommissionName;
    @FXML private Text substituteName;

    @FXML private ProgressIndicator loader;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();
    private int thesisId;
    private ThesisDetailsDTO currentDetails;
    private Commission currentCommission;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public void initWithThesisId(int thesisId) {
        this.thesisId = thesisId;
        loadThesisDetails();
    }

    private void loadThesisDetails() {
        Task<ThesisDetailsDTO> task = new Task<>() {
            @Override
            protected ThesisDetailsDTO call() throws Exception {
                return thesisDAO.getThesisDetails(thesisId);
            }
        };

        if (loader != null) {
            loader.visibleProperty().bind(task.runningProperty());
        }

        task.setOnSucceeded(e -> {
            currentDetails = task.getValue();
            if (currentDetails != null) {
                populateFields();
                // Učitaj komisiju NAKON što su thesis details učitani
                loadCommission();
            } else {
                GlobalErrorHandler.error("Završni rad nije pronađen.");
            }
        });

        task.setOnFailed(e -> {
            GlobalErrorHandler.error("Greška pri učitavanju detalja završnog rada.", task.getException());
        });

        new Thread(task, "load-thesis-details").start();
    }

    private void loadCommission() {
        Task<Commission> task = new Task<>() {
            @Override
            protected Commission call() throws Exception {
                return commissionDAO.getCommissionByThesisId(thesisId);
            }
        };

        task.setOnSucceeded(e -> {
            currentCommission = task.getValue();
            updateCommissionDisplay();
        });

        task.setOnFailed(e -> {
            updateCommissionDisplay();
            GlobalErrorHandler.error("Greška pri učitavanju komisije.", task.getException());
        });

        new Thread(task, "load-commission").start();
    }

    private void populateFields() {
        titleValue.setText(currentDetails.getTitle() != null ? currentDetails.getTitle().toUpperCase() : "—");
        //descriptionValue.setText(currentDetails.getDescription());
        subjectValue.setText(currentDetails.getSubject() != null ? currentDetails.getSubject().getName() : "—");

        if (currentDetails.getStatus() != null) {
            statusValue.setText(currentDetails.getStatus());
        } else {
            statusValue.setText("—");
        }

        applicationDateValue.setText(currentDetails.getApplicationDate() != null ?
                currentDetails.getApplicationDate().format(DATE_FORMATTER) : "--/--/----");
        defenseDateValue.setText(currentDetails.getDefenseDate() != null ?
                currentDetails.getDefenseDate().format(DATE_FORMATTER) : "--/--/----");

        if (currentDetails.getGrade() != null) {
            gradeValue.setText(String.valueOf(currentDetails.getGrade()));
        } else {
            gradeValue.setText("—");
        }

        if (currentDetails.getStudent() != null) {
            studentName.setText(currentDetails.getStudent().getFirstName() + " " +
                    currentDetails.getStudent().getLastName());
            studentIndex.setText(String.format("%03d", currentDetails.getStudent().getIndexNumber()));
            studentEmail.setText(currentDetails.getStudent().getEmail() != null ?
                    currentDetails.getStudent().getEmail() : "—");
        }

        if (currentDetails.getMentor() != null) {
            mentorTitle.setText(currentDetails.getMentor().getTitle() != null ?
                    currentDetails.getMentor().getTitle() : "");
            mentorName.setText(currentDetails.getMentor().getFirstName() + " " +
                    currentDetails.getMentor().getLastName());
            mentorEmail.setText(currentDetails.getMentor().getEmail() != null ?
                    currentDetails.getMentor().getEmail() : "—");
        }
    }

    private void updateCommissionDisplay() {
        if (currentCommission != null && currentCommission.getMember1() != null) {
            commissionNotFormedBox.setVisible(false);
            commissionNotFormedBox.setManaged(false);
            commissionFormedBox.setVisible(true);
            commissionFormedBox.setManaged(true);

            // Predsjednik (Member1)
            chairmanName.setText(formatMemberName(currentCommission.getMember1()));

            // Član (Member2)
            memberName.setText(formatMemberName(currentCommission.getMember2()));

            // Zamjenski član (Member3)
            substituteName.setText(formatMemberName(currentCommission.getMember3()));

            // Mentor (iz Thesis, ne iz Commission!)
            if (currentDetails != null && currentDetails.getMentor() != null) {
                mentorCommissionName.setText(formatMemberName(currentDetails.getMentor()));
            } else {
                mentorCommissionName.setText("—");
            }

            // PROMJENA: Sekretar (sada je AcademicStaff, ne AppUser!)
            if (currentDetails != null && currentDetails.getSecretary() != null) {
                secretaryCommissionName.setText(formatMemberName(currentDetails.getSecretary()));
            } else {
                secretaryCommissionName.setText("—");
            }
        } else {
            commissionNotFormedBox.setVisible(true);
            commissionNotFormedBox.setManaged(true);
            commissionFormedBox.setVisible(false);
            commissionFormedBox.setManaged(false);
        }
    }

    private String formatMemberName(model.AcademicStaff member) {
        if (member == null) return "—";
        String title = member.getTitle() != null ? member.getTitle() + " " : "";
        return title + member.getFirstName() + " " + member.getLastName();
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrda brisanja");
        confirm.setHeaderText("Da li ste sigurni da želite obrisati ovaj završni rad?");
        confirm.setContentText("Ova akcija se ne može poništiti.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                thesisDAO.deleteThesis(thesisId);
                GlobalErrorHandler.info("Završni rad je uspješno obrisan!");
                back();
            } catch (Exception e) {
                GlobalErrorHandler.error("Greška pri brisanju završnog rada.", e);
            }
        }
    }

    @FXML
    private void handleEdit() {
        Thesis thesis = thesisDAO.getThesisById(thesisId);
        if (thesis != null) {
            SceneManager.showWithData(
                    "/app/thesisForm.fxml",
                    "Uredi završni rad",
                    (ThesisFormController controller) -> {
                        controller.initEdit(thesis, thesisId);
                    }
            );
        } else {
            GlobalErrorHandler.error("Greška pri dohvatanju podataka za uređivanje.");
        }
    }

    @FXML
    private void handleFormCommission() {
        if (currentDetails == null) {
            GlobalErrorHandler.error("Detalji rada nisu učitani.");
            return;
        }

        try {
            SceneManager.showWithData(
                    "/app/commissionForm.fxml",
                    "Dodavanje komisije",
                    (CommissionFormController controller) -> {
                        controller.initWithThesis(thesisId, currentDetails);
                    }
            );
        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri otvaranju forme komisije.", e);
        }
    }

    @FXML
    private void handleEditCommission() {
        if (currentDetails == null || currentCommission == null) {
            GlobalErrorHandler.error("Podaci nisu dostupni.");
            return;
        }

        try {
            SceneManager.showWithData(
                    "/app/commissionForm.fxml",
                    "Uredi komisiju",
                    (CommissionFormController controller) -> {
                        controller.initEditCommission(thesisId, currentDetails, currentCommission);
                    }
            );
        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri otvaranju forme za uređivanje komisije.", e);
        }
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.THESIS);
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }

    @FXML
    private void handleOpenWrittenExamReport() {
        // Check if commission is formed
        if (currentCommission == null || currentCommission.getMember1() == null) {
            GlobalErrorHandler.error("Komisija mora biti formirana prije kreiranja zapisnika.");
            return;
        }

        SceneManager.showWithData(
                "/app/writtenExamReport.fxml",
                "Zapisnik o pismenom dijelu",
                (WrittenExamReportController controller) -> controller.initWithThesisId(thesisId)
        );
    }

    @FXML
    private void handleOpenDefenseReport() {
        // Check if commission is formed
        if (currentCommission == null || currentCommission.getMember1() == null) {
            GlobalErrorHandler.error("Komisija mora biti formirana prije kreiranja zapisnika.");
            return;
        }

        // Check if defense date is set
        if (currentDetails.getDefenseDate() == null) {
            GlobalErrorHandler.error("Datum odbrane mora biti unesen prije kreiranja zapisnika.");
            return;
        }

        SceneManager.showWithData(
                "/app/defenseReport.fxml",
                "Zapisnik sa odbrane",
                (DefenseReportController controller) -> controller.initWithThesisId(thesisId)
        );
    }
}