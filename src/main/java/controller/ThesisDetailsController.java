package controller;

import dao.ThesisDAO;
import dto.ThesisDetailsDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import model.Thesis;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;

import java.time.format.DateTimeFormatter;

public class ThesisDetailsController {

    @FXML private Text thesisTitle;
    @FXML private HBox statusBadge;
    @FXML private Text statusText;
    @FXML private Text applicationDate;
    @FXML private Text approvalDate;
    @FXML private Text defenseDate;
    @FXML private Text grade;
    @FXML private Text studentName;
    @FXML private Text indexNumber;
    @FXML private Text studentEmail;
    @FXML private Text studyProgram;
    @FXML private Text cycle;
    @FXML private Text ects;
    @FXML private Text mentorName;
    @FXML private Text mentorEmail;
    @FXML private Text departmentName;
    @FXML private Text subjectName;
    @FXML private Text secretaryName;
    @FXML private ProgressIndicator loader;
    @FXML private javafx.scene.control.Button editButton;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private int thesisId;
    private ThesisDetailsDTO currentDetails;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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

        task.setOnSucceeded(event -> {
            ThesisDetailsDTO details = task.getValue();
            if (details != null) {
                populateFields(details);
            } else {
                showError("Rad nije pronađen!");
            }
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            showError("Greška pri učitavanju detalja: " + ex.getMessage());
        });

        new Thread(task, "load-thesis-details").start();
    }

    private void populateFields(ThesisDetailsDTO details) {
        this.currentDetails = details;

        // Title
        thesisTitle.setText(details.getTitle());

        // Status badge
        statusText.setText(details.getStatus());
        applyStatusStyle(details.getStatus());

        // Hide edit button if thesis is defended
        if (editButton != null) {
            if ("Defended".equals(details.getStatus())) {
                editButton.setVisible(false);
                editButton.setManaged(false);
            } else {
                editButton.setVisible(true);
                editButton.setManaged(true);
            }
        }

        // Basic info
        applicationDate.setText(details.getApplicationDate() != null ?
                details.getApplicationDate().format(DATE_FORMATTER) : "—");
        approvalDate.setText(details.getApprovalDate() != null ?
                details.getApprovalDate().format(DATE_FORMATTER) : "—");
        defenseDate.setText(details.getDefenseDate() != null ?
                details.getDefenseDate().format(DATE_FORMATTER) : "—");
        grade.setText(details.getGrade() != null ?
                details.getGrade().toString() : "—");

        // Student info
        if (details.getStudent() != null) {
            studentName.setText(details.getStudent().getFirstName() + " " +
                    details.getStudent().getLastName());
            indexNumber.setText(String.valueOf(details.getStudent().getIndexNumber()));
            studentEmail.setText(details.getStudent().getEmail() != null ?
                    details.getStudent().getEmail() : "—");
            studyProgram.setText(details.getStudent().getStudyProgram() != null ?
                    details.getStudent().getStudyProgram() : "—");

            String cycleText = switch (details.getStudent().getCycle()) {
                case 1 -> "Prvi ciklus";
                case 2 -> "Drugi ciklus";
                case 3 -> "Treći ciklus";
                default -> "—";
            };
            cycle.setText(cycleText);

            ects.setText(String.valueOf(details.getStudent().getECTS()));
        }

        // Mentor info
        if (details.getMentor() != null) {
            String mentorFullName = (details.getMentor().getTitle() != null ?
                    details.getMentor().getTitle() + " " : "") +
                    details.getMentor().getFirstName() + " " +
                    details.getMentor().getLastName();
            mentorName.setText(mentorFullName);
            mentorEmail.setText(details.getMentor().getEmail() != null ?
                    details.getMentor().getEmail() : "—");
        }

        // Administrative info
        departmentName.setText(details.getDepartment() != null ?
                details.getDepartment().getName() : "—");
        subjectName.setText(details.getSubject() != null ?
                details.getSubject().getName() : "—");
        secretaryName.setText(details.getSecretary() != null ?
                details.getSecretary().getUsername() : "—");
    }

    private void applyStatusStyle(String status) {
        statusBadge.getStyleClass().removeAll("status-pending", "status-in-progress", "status-approved");

        String styleClass = switch (status) {
            case "Defended" -> "status-approved";
            case "U procesu" -> "status-in-progress";
            case "Na čekanju" -> "status-pending";
            default -> "status-pending";
        };

        statusBadge.getStyleClass().add(styleClass);
    }

    @FXML
    private void handleEdit() {
        // Load full Thesis object and open edit form
        Task<Thesis> task = new Task<>() {
            @Override
            protected Thesis call() throws Exception {
                return thesisDAO.getThesisById(thesisId);
            }
        };

        task.setOnSucceeded(event -> {
            Thesis thesis = task.getValue();
            if (thesis != null) {
                SceneManager.showWithData(
                        "/app/thesisForm.fxml",
                        "Uredi završni rad",
                        (ThesisFormController controller) -> {
                            controller.initEdit(thesis, thesisId);
                        }
                );
            }
        });

        task.setOnFailed(event -> {
            showError("Greška pri učitavanju rada: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    // Metoda za osvježavanje details page-a nakon edit-a
    public void refresh() {
        loadThesisDetails();
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.THESIS);
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}