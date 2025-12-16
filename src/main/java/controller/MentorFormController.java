package controller;

import dao.MentorDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.AcademicStaff;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;

public class MentorFormController {

    private enum Mode { CREATE, EDIT }

    private Mode mode;
    private AcademicStaff mentor;

    private final MentorDAO mentorDAO = new MentorDAO();

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField titleField;
    @FXML private TextField emailField;
    @FXML private Button deleteButton;
    @FXML private HBox deleteButtonContainer;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;

    @FXML

    public void initCreate() {
        this.mode = Mode.CREATE;

        if (formTitle != null) {
            formTitle.setText("Dodaj novog mentora");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Unesite podatke o novom mentoru");
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

    public void initEdit(AcademicStaff mentor) {
        this.mode = Mode.EDIT;
        this.mentor = mentor;

        if (formTitle != null) {
            formTitle.setText("Uredi mentora");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Uredite podatke o mentoru");
        }

        // Show delete button in edit mode
        if (deleteButton != null) {
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        }
        if (deleteButtonContainer != null) {
            deleteButtonContainer.setVisible(true);
            deleteButtonContainer.setManaged(true);
        }

        fillFields();
    }

    private void fillFields() {
        firstNameField.setText(mentor.getFirstName());
        lastNameField.setText(mentor.getLastName());
        titleField.setText(mentor.getTitle());
        emailField.setText(mentor.getEmail());
    }

    @FXML
    private void handleSave() {
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            show("Ime je obavezno polje!", Alert.AlertType.WARNING);
            return;
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            show("Prezime je obavezno polje!", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (mode == Mode.CREATE) {
                mentorDAO.insertMentor(buildMentor());
            } else {
                updateMentor();
                mentorDAO.updateMentor(mentor);
            }
            back();
        } catch (Exception e) {
            show("Greška: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrda brisanja");
        confirm.setHeaderText("Da li ste sigurni da želite obrisati ovog mentora?");
        confirm.setContentText("Ova akcija se ne može poništiti.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    mentorDAO.deleteMentor(mentor.getId());
                    back();
                } catch (Exception e) {
                    show("Greška pri brisanju: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private AcademicStaff buildMentor() {
        AcademicStaff newMentor = new AcademicStaff();
        newMentor.setFirstName(firstNameField.getText().trim());
        newMentor.setLastName(lastNameField.getText().trim());
        newMentor.setTitle(titleField.getText() != null ? titleField.getText().trim() : "");
        newMentor.setEmail(emailField.getText() != null ? emailField.getText().trim() : "");
        newMentor.setIsDean(false);
        newMentor.setIsActive(true);
        return newMentor;
    }

    private void updateMentor() {
        mentor.setFirstName(firstNameField.getText().trim());
        mentor.setLastName(lastNameField.getText().trim());
        mentor.setTitle(titleField.getText() != null ? titleField.getText().trim() : "");
        mentor.setEmail(emailField.getText() != null ? emailField.getText().trim() : "");
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.MENTORS);
        SceneManager.show("/app/dashboard.fxml", "Dashboard");
    }

    private void show(String msg, Alert.AlertType type) {
        new Alert(type, msg).showAndWait();
    }
}
