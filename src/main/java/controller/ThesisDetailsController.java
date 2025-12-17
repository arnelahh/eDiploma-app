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

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ThesisDetailsController {

    @FXML private Text titleValue;
    @FXML private Text descriptionValue;
    @FXML private Text subjectValue;
    @FXML private Text statusValue;
    @FXML private Text applicationDateValue;
    @FXML private Text defenseDateValue;

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
        System.out.println("\n=== THESIS DETAILS INIT ===");
        System.out.println("ThesisId: " + thesisId);
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
                System.out.println("✓ Thesis details loaded");
                populateFields();
                // Učitaj komisiju NAKON što su thesis details učitani
                loadCommission();
            } else {
                System.err.println("✗ Thesis details NOT FOUND");
                showError("Završni rad nije pronađen");
            }
        });

        task.setOnFailed(e -> {
            System.err.println("✗ Error loading thesis: " + task.getException().getMessage());
            task.getException().printStackTrace();
            showError("Greška pri učitavanju: " + task.getException().getMessage());
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
            if (currentCommission != null) {
                System.out.println("✓ Commission loaded");
            } else {
                System.out.println("ℹ No commission found (not yet created)");
            }
            updateCommissionDisplay();
        });

        task.setOnFailed(e -> {
            System.err.println("✗ Error loading commission: " + task.getException().getMessage());
            updateCommissionDisplay();
        });

        new Thread(task, "load-commission").start();
    }

    private void populateFields() {
        titleValue.setText(currentDetails.getTitle() != null ? currentDetails.getTitle() : "—");
        descriptionValue.setText("Razvoj moderne web aplikacije sa React frameworkom i Node.js backend-om");
        subjectValue.setText(currentDetails.getSubject() != null ? currentDetails.getSubject().getName() : "—");

        String statusText = currentDetails.getStatus() != null ?
                "3/6 (" + currentDetails.getStatus() + ")" : "—";
        statusValue.setText(statusText);

        applicationDateValue.setText(currentDetails.getApplicationDate() != null ?
                currentDetails.getApplicationDate().format(DATE_FORMATTER) : "--/--/----");
        defenseDateValue.setText(currentDetails.getDefenseDate() != null ?
                currentDetails.getDefenseDate().format(DATE_FORMATTER) : "--/--/----");

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
            System.out.println("\n=== DISPLAYING FORMED COMMISSION ===");

            commissionNotFormedBox.setVisible(false);
            commissionNotFormedBox.setManaged(false);
            commissionFormedBox.setVisible(true);
            commissionFormedBox.setManaged(true);

            // Predsjednik (Member1)
            chairmanName.setText(formatMemberName(currentCommission.getMember1()));
            System.out.println("Chairman: " + formatMemberName(currentCommission.getMember1()));

            // Član (Member2)
            memberName.setText(formatMemberName(currentCommission.getMember2()));
            System.out.println("Member: " + formatMemberName(currentCommission.getMember2()));

            // Zamjenski član (Member3)
            substituteName.setText(formatMemberName(currentCommission.getMember3()));
            System.out.println("Substitute: " + formatMemberName(currentCommission.getMember3()));

            // Mentor (iz Thesis, ne iz Commission!) - KLJUČNA ISPRAVKA
            if (currentDetails != null && currentDetails.getMentor() != null) {
                mentorCommissionName.setText(formatMemberName(currentDetails.getMentor()));
                System.out.println("Mentor: " + formatMemberName(currentDetails.getMentor()));
            } else {
                mentorCommissionName.setText("—");
                System.out.println("Mentor: NOT SET in thesis details");
            }

            // Sekretar (iz Thesis, ne iz Commission!) - KLJUČNA ISPRAVKA
            if (currentDetails != null && currentDetails.getSecretary() != null) {
                secretaryCommissionName.setText(currentDetails.getSecretary().getUsername());
                System.out.println("Secretary: " + currentDetails.getSecretary().getUsername());
            } else {
                secretaryCommissionName.setText("—");
                System.out.println("Secretary: NOT SET in thesis details");
            }

            System.out.println("====================================\n");
        } else {
            System.out.println("=== Commission NOT formed - showing form button ===\n");
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
        System.out.println("\n=== FORM COMMISSION CLICKED ===");

        if (currentDetails == null) {
            System.err.println("ERROR: currentDetails is NULL!");
            showError("Detalji rada nisu učitani");
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
            System.err.println("ERROR opening form: " + e.getMessage());
            e.printStackTrace();
            showError("Greška pri otvaranju forme: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditCommission() {
        System.out.println("\n=== EDIT COMMISSION CLICKED ===");

        if (currentDetails == null || currentCommission == null) {
            showError("Podaci nisu dostupni");
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
            System.err.println("ERROR opening edit form: " + e.getMessage());
            e.printStackTrace();
            showError("Greška: " + e.getMessage());
        }
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.THESIS);
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.showAndWait();
        });
    }

    private void showInfo(String message) {
        javafx.application.Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
            alert.showAndWait();
        });
    }
}