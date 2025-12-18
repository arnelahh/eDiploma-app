package controller;

import dao.MentorDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.AcademicStaff;
import utils.*;

import java.util.List;

public class MentorFormController {

    private enum Mode { CREATE, EDIT }

    private Mode mode;
    private AcademicStaff mentor;

    // Instanciramo validator i DAO
    private final MentorValidator validator = new MentorValidator();
    private final MentorDAO mentorDAO = new MentorDAO();

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField titleField;
    @FXML private TextField emailField;
    @FXML private Button deleteButton;
    @FXML private HBox deleteButtonContainer;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private Button saveButton; // Dodaj fx:id="saveButton" u FXML ako želiš disable tokom snimanja

    @FXML
    public void initCreate() {
        this.mode = Mode.CREATE;

        if (formTitle != null) {
            formTitle.setText("Dodaj novog mentora");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Unesite podatke o novom mentoru");
        }

        toggleDeleteButton(false);
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

        toggleDeleteButton(true);
        fillFields();
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
        if (mentor != null) {
            firstNameField.setText(mentor.getFirstName());
            lastNameField.setText(mentor.getLastName());
            titleField.setText(mentor.getTitle());
            emailField.setText(mentor.getEmail());
        }
    }

    /**
     * Kreira privremeni objekat iz forme radi validacije.
     * Uključuje ID ako je EDIT mode, da bi validator znao ignorisati trenutni red u bazi.
     */
    private AcademicStaff extractFormData() {
        AcademicStaff staff = new AcademicStaff();

        if (mode == Mode.EDIT && mentor != null) {
            staff.setId(mentor.getId());
        }

        staff.setFirstName(firstNameField.getText() != null ? firstNameField.getText().trim() : "");
        staff.setLastName(lastNameField.getText() != null ? lastNameField.getText().trim() : "");
        staff.setTitle(titleField.getText() != null ? titleField.getText().trim() : "");
        staff.setEmail(emailField.getText() != null ? emailField.getText().trim() : "");

        // Default vrijednosti
        staff.setIsDean(false);
        staff.setIsActive(true);

        return staff;
    }

    @FXML
    private void handleSave() {
        // 1. Preuzimanje podataka sa forme
        AcademicStaff tempStaff = extractFormData();

        // 2. Osnovna validacija (Format podataka)
        ValidationResult basicResult = validator.validate(tempStaff);

        if (!basicResult.isValid()) {
            showErrorList(basicResult.getErrors());
            return;
        }

        // Onemogući dugme da spriječiš višestruke klikove dok čekamo bazu
        if (saveButton != null) saveButton.setDisable(true);

        // 3. Validacija jedinstvenosti (Email u bazi - Asinhrono)
        validator.validateUniqueness(tempStaff).thenAccept(uniqueResult -> {

            // Vraćamo se na JavaFX Application Thread za ažuriranje UI-a
            Platform.runLater(() -> {
                // Ponovo omogući dugme
                if (saveButton != null) saveButton.setDisable(false);

                if (!uniqueResult.isValid()) {
                    // Prikaz grešaka iz baze (npr. Email zauzet)
                    showErrorList(uniqueResult.getErrors());
                } else {
                    // Sve validacije prošle, izvrši snimanje
                    performSave(tempStaff);
                }
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                if (saveButton != null) saveButton.setDisable(false);
                show("Došlo je do neočekivane greške pri validaciji: " + ex.getMessage(), Alert.AlertType.ERROR);
            });
            return null;
        });
    }

    private void performSave(AcademicStaff validatedData) {
        try {
            if (mode == Mode.CREATE) {
                mentorDAO.insertMentor(validatedData);
                show("Mentor je uspješno dodan!", Alert.AlertType.INFORMATION);
            } else {
                // Ažuriramo originalni objekat (da zadržimo referencu ako treba)
                updateOriginalMentor(validatedData);
                mentorDAO.updateMentor(mentor);
                show("Podaci o mentoru su ažurirani!", Alert.AlertType.INFORMATION);
            }
            back();
        } catch (Exception e) {
            show("Greška prilikom snimanja u bazu: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateOriginalMentor(AcademicStaff newData) {
        mentor.setFirstName(newData.getFirstName());
        mentor.setLastName(newData.getLastName());
        mentor.setTitle(newData.getTitle());
        mentor.setEmail(newData.getEmail());
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
                    show("Mentor je obrisan.", Alert.AlertType.INFORMATION);
                    back();
                } catch (Exception e) {
                    show("Greška pri brisanju: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.MENTORS);
        SceneManager.show("/app/dashboard.fxml", "Dashboard");
    }

    private void show(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Greška" : "Informacija");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showErrorList(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Neispravan unos");
        alert.setHeaderText("Molimo ispravite sljedeće greške:");
        alert.setContentText("• " + String.join("\n• ", errors));
        alert.showAndWait();
    }
}