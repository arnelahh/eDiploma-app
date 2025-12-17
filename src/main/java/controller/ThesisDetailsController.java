package controller;

import dao.ThesisDAO;
import dto.ThesisDetailsDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.AcademicStaff;
import model.Thesis;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ThesisDetailsController {

    // Osnovne informacije o radu
    @FXML private Text titleValue;
    @FXML private Text descriptionValue;
    @FXML private Text subjectValue;
    @FXML private Text statusValue;
    @FXML private Text applicationDateValue;
    @FXML private Text defenseDateValue;

    // Student info
    @FXML private Text studentName;
    @FXML private Text studentIndex;
    @FXML private Text studentEmail;

    // Mentor info
    @FXML private Text mentorTitle;
    @FXML private Text mentorName;
    @FXML private Text mentorEmail;

    // Komisija
    @FXML private VBox commissionNotFormedBox;
    @FXML private VBox commissionFormedBox;
    @FXML private Text chairmanName;
    @FXML private Text memberName;
    @FXML private Text secretaryCommissionName;
    @FXML private Text substituteName;

    @FXML private ProgressIndicator loader;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private int thesisId;
    private ThesisDetailsDTO currentDetails;
    private CommissionData commissionData;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    // Jednostavna klasa za čuvanje podataka o komisiji (privremeno)
    public static class CommissionData {
        public AcademicStaff chairman;
        public AcademicStaff member;
        public AcademicStaff secretary;
        public AcademicStaff substitute;
        public AcademicStaff mentor;
    }

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

        loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            currentDetails = task.getValue();
            if (currentDetails != null) {
                populateFields();
            } else {
                showError("Završni rad nije pronađen");
            }
        });

        task.setOnFailed(e -> {
            showError("Greška pri učitavanju: " + task.getException().getMessage());
        });

        new Thread(task, "load-thesis-details").start();
    }

    private void populateFields() {
        // Osnovne informacije
        titleValue.setText(currentDetails.getTitle() != null ? currentDetails.getTitle() : "—");

        // Opis rada - za sada hardkodirano jer nije u DTO-u
        descriptionValue.setText("Razvoj moderne web aplikacije sa React frameworkom i Node.js backend-om");

        subjectValue.setText(currentDetails.getSubject() != null ? currentDetails.getSubject().getName() : "—");

        // Status sa brojem koraka
        String statusText = currentDetails.getStatus() != null ?
                "3/6 (" + currentDetails.getStatus() + ")" : "—";
        statusValue.setText(statusText);

        applicationDateValue.setText(currentDetails.getApplicationDate() != null ?
                currentDetails.getApplicationDate().format(DATE_FORMATTER) : "--/--/----");
        defenseDateValue.setText(currentDetails.getDefenseDate() != null ?
                currentDetails.getDefenseDate().format(DATE_FORMATTER) : "--/--/----");

        // Student
        if (currentDetails.getStudent() != null) {
            studentName.setText(currentDetails.getStudent().getFirstName() + " " +
                    currentDetails.getStudent().getLastName());
            studentIndex.setText(String.format("%03d", currentDetails.getStudent().getIndexNumber()));
            studentEmail.setText(currentDetails.getStudent().getEmail() != null ?
                    currentDetails.getStudent().getEmail() : "—");
        }

        // Mentor
        if (currentDetails.getMentor() != null) {
            mentorTitle.setText(currentDetails.getMentor().getTitle() != null ?
                    currentDetails.getMentor().getTitle() : "");
            mentorName.setText(currentDetails.getMentor().getFirstName() + " " +
                    currentDetails.getMentor().getLastName());
            mentorEmail.setText(currentDetails.getMentor().getEmail() != null ?
                    currentDetails.getMentor().getEmail() : "—");
        }

        // Komisija - provjeri da li je formirana
        updateCommissionDisplay();
    }

    private void updateCommissionDisplay() {
        if (commissionData != null && commissionData.chairman != null) {
            // Komisija je formirana
            commissionNotFormedBox.setVisible(false);
            commissionNotFormedBox.setManaged(false);
            commissionFormedBox.setVisible(true);
            commissionFormedBox.setManaged(true);

            chairmanName.setText(formatMemberName(commissionData.chairman));
            memberName.setText(formatMemberName(commissionData.member));
            secretaryCommissionName.setText(formatMemberName(commissionData.secretary));
            substituteName.setText(formatMemberName(commissionData.substitute));
        } else {
            // Komisija nije formirana
            commissionNotFormedBox.setVisible(true);
            commissionNotFormedBox.setManaged(true);
            commissionFormedBox.setVisible(false);
            commissionFormedBox.setManaged(false);
        }
    }

    private String formatMemberName(AcademicStaff member) {
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
                showInfo("Završni rad je uspješno obrisan!");
                back();
            } catch (Exception e) {
                showError("Greška pri brisanju: " + e.getMessage());
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
            showError("Greška pri dohvatanju podataka za uređivanje");
        }
    }

    @FXML
    private void handleFormCommission() {
        SceneManager.showWithData(
                "/app/commissionForm.fxml",
                "Dodavanje komisije",
                (CommissionFormController controller) -> {
                    controller.initWithThesis(thesisId, currentDetails);
                }
        );
    }

    @FXML
    private void handleEditCommission() {
        SceneManager.showWithData(
                "/app/commissionForm.fxml",
                "Uredi komisiju",
                (CommissionFormController controller) -> {
                    controller.initEditCommission(thesisId, currentDetails, commissionData);
                }
        );
    }

    // Metoda koju poziva CommissionFormController nakon što se komisija sačuva
    public void setCommissionData(CommissionData data) {
        this.commissionData = data;
        updateCommissionDisplay();
    }

    public void refresh() {
        loadThesisDetails();
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.THESIS);
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }
}
