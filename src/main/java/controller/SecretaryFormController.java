package controller;

import dao.SecretaryDAO;
import dao.UserRoleDAO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import model.AppUser;
import model.UserRole;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;

import java.util.List;

public class SecretaryFormController {

    private enum Mode { CREATE, EDIT }

    private Mode mode;
    private AppUser secretary;

    private final SecretaryDAO secretaryDAO = new SecretaryDAO();
    private final UserRoleDAO userRoleDAO = new UserRoleDAO();

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField titleField;
    @FXML private TextField emailField;
    @FXML private Button deleteButton;
    @FXML private HBox deleteButtonContainer;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;
    @FXML private ProgressIndicator loader;

    @FXML
    public void initialize() {
        // Inicijalizacija bez učitavanja uloga
    }

    public void initCreate() {
        this.mode = Mode.CREATE;

        if (formTitle != null) {
            formTitle.setText("Dodaj novog sekretara");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Unesite podatke o novom sekretaru");
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

    public void initEdit(AppUser secretary) {
        this.mode = Mode.EDIT;
        this.secretary = secretary;

        if (formTitle != null) {
            formTitle.setText("Uredi sekretara");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Uredite podatke o sekretaru");
        }

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
        // Parse username to extract first and last name
        if (secretary.getUsername() != null) {
            String[] parts = secretary.getUsername().split(" ");
            if (parts.length >= 2) {
                firstNameField.setText(parts[0]);
                lastNameField.setText(parts[1]);
            } else if (parts.length == 1) {
                firstNameField.setText(parts[0]);
            }
        }

        titleField.setText(secretary.getRole() != null ? secretary.getRole().getName() : "");
        emailField.setText(secretary.getEmail());
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
                secretaryDAO.insertSecretary(buildSecretary());
            } else {
                updateSecretary();
                secretaryDAO.updateSecretary(secretary);
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
        confirm.setHeaderText("Da li ste sigurni da želite obrisati ovog sekretara?");
        confirm.setContentText("Ova akcija se ne može poništiti.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    secretaryDAO.deleteSecretary(secretary.getId());
                    back();
                } catch (Exception e) {
                    show("Greška pri brisanju: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private AppUser buildSecretary() {
        AppUser newSecretary = new AppUser();

        // Combine first and last name into username
        String username = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        newSecretary.setUsername(username);

        newSecretary.setEmail(emailField.getText() != null ? emailField.getText().trim() : "");

        // Create and set role with title as name
        UserRole role = new UserRole();
        role.setId(1); // Default role ID - može se prilagoditi
        role.setName(titleField.getText() != null ? titleField.getText().trim() : "");
        newSecretary.setRole(role);

        newSecretary.setActive(true);
        return newSecretary;
    }

    private void updateSecretary() {
        // Combine first and last name into username
        String username = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        secretary.setUsername(username);

        secretary.setEmail(emailField.getText() != null ? emailField.getText().trim() : "");

        // Update role with title
        if (secretary.getRole() == null) {
            secretary.setRole(new UserRole());
        }
        secretary.getRole().setName(titleField.getText() != null ? titleField.getText().trim() : "");
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.SECRETARIES);
        SceneManager.show("/app/dashboard.fxml", "Dashboard");
    }

    private void show(String msg, Alert.AlertType type) {
        new Alert(type, msg).showAndWait();
    }

    private void showError(String msg) {
        javafx.application.Platform.runLater(() ->
                new Alert(Alert.AlertType.ERROR, msg).showAndWait()
        );
    }
}