package controller;

import dao.SecretaryDAO;
import dto.CreateSecretaryDTO;
import dto.CreateUserIdsDTO;
import dto.SecretaryDTO;
import dto.UpdateSecretaryDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.AcademicStaff;
import model.AppUser;
import utils.*;

import java.util.Optional;

public class SecretaryFormController {

    private enum Mode { CREATE, EDIT }

    private Mode mode = Mode.CREATE;

    private int appUserId;
    private int academicStaffId;

    @FXML private TextField titleField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private Label formTitle;
    @FXML private Label formSubtitle;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private ProgressIndicator loader;
    @FXML private Button deleteButton;

    private final SecretaryDAO secretaryDAO = new SecretaryDAO();
    private final SecretaryValidator createValidator = new SecretaryValidator();
    private final UpdateSecretaryValidator updateValidator = new UpdateSecretaryValidator();

    @FXML
    public void initialize() {
        if (loader != null) loader.setVisible(false);

        if (deleteButton != null) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
            deleteButton.setDisable(false);
        }
    }

    public void initCreate() {
        mode = Mode.CREATE;
        updateFormTexts();
        appUserId = 0;
        academicStaffId = 0;

        if (titleField != null) titleField.clear();
        if (firstNameField != null) firstNameField.clear();
        if (lastNameField != null) lastNameField.clear();
        if (emailField != null) emailField.clear();
        if (usernameField != null) usernameField.clear();
        if (passwordField != null) passwordField.clear();

        if (deleteButton != null) {
            deleteButton.setVisible(false);
            deleteButton.setManaged(false);
        }
    }

    public void initEdit(SecretaryDTO dto) {
        mode = Mode.EDIT;
        updateFormTexts();
        AcademicStaff staff = dto.getSecretary();
        AppUser user = dto.getUser();

        this.academicStaffId = staff.getId();
        this.appUserId = user.getId();

        if (titleField != null) titleField.setText(staff.getTitle());
        if (firstNameField != null) firstNameField.setText(staff.getFirstName());
        if (lastNameField != null) lastNameField.setText(staff.getLastName());
        if (emailField != null) emailField.setText(staff.getEmail());

        if (usernameField != null) usernameField.setText(user.getUsername());

        if (passwordField != null) passwordField.clear();

        if (deleteButton != null) {
            deleteButton.setVisible(true);
            deleteButton.setManaged(true);
        }
    }

    @FXML
    private void handleSave() {
        if (mode == Mode.CREATE) {
            handleCreate();
        } else {
            handleUpdate();
        }
    }

    private void handleCreate() {
        CreateSecretaryDTO dto = CreateSecretaryDTO.builder()
                .title(textOrNull(titleField))
                .firstName(textOrNull(firstNameField))
                .lastName(textOrNull(lastNameField))
                .email(textOrNull(emailField))
                .username(textOrNull(usernameField))
                .rawPassword(passwordField != null ? passwordField.getText() : null)
                .build();

        ValidationResult vr = createValidator.validate(dto);
        if (!vr.isValid()) {
            GlobalErrorHandler.error(vr.joined("\n"));
            return;
        }

        Task<CreateUserIdsDTO> task = new Task<>() {
            @Override
            protected CreateUserIdsDTO call() throws Exception {
                return secretaryDAO.createSecretary(dto);
            }
        };

        runTask(task, "create-secretary", true);
    }

    private void handleUpdate() {
        UpdateSecretaryDTO dto = UpdateSecretaryDTO.builder()
                .appUserId(appUserId)
                .academicStaffId(academicStaffId)
                .title(textOrNull(titleField))
                .firstName(textOrNull(firstNameField))
                .lastName(textOrNull(lastNameField))
                .email(textOrNull(emailField))
                .username(textOrNull(usernameField))
                .rawPassword(passwordField != null ? passwordField.getText() : null) // optional
                .build();

        ValidationResult vr = updateValidator.validate(dto);
        if (!vr.isValid()) {
            GlobalErrorHandler.error(vr.joined("\n"));
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                secretaryDAO.updateSecretary(dto);
                return null;
            }
        };

        runTask(task, "update-secretary", true);
    }

    @FXML
    private void handleDelete() {
        if (mode != Mode.EDIT) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrda brisanja");
        confirm.setHeaderText("Obrisati sekretara?");
        confirm.setContentText("Ova akcija se ne može poništiti.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                secretaryDAO.deleteSecretary(appUserId, academicStaffId);
                return null;
            }
        };

        runTask(task, "delete-secretary", true);
    }

    private void runTask(Task<?> task, String threadName, boolean redirectOnSuccess) {
        setBusy(true);
        if (loader != null) loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            setBusy(false);
            if (loader != null) loader.visibleProperty().unbind();
            if (redirectOnSuccess) redirectToSecretaries();
        });

        task.setOnFailed(e -> {
            setBusy(false);
            if (loader != null) loader.visibleProperty().unbind();
            if (loader != null) loader.setVisible(false);

            GlobalErrorHandler.error("Operacija nije uspjela.", task.getException());
        });

        new Thread(task, threadName).start();
    }

    private void redirectToSecretaries() {
        NavigationContext.setTargetView(DashboardView.SECRETARIES);
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }

    private void setBusy(boolean busy) {
        if (titleField != null) titleField.setDisable(busy);
        if (firstNameField != null) firstNameField.setDisable(busy);
        if (lastNameField != null) lastNameField.setDisable(busy);
        if (emailField != null) emailField.setDisable(busy);
        if (usernameField != null) usernameField.setDisable(busy);
        if (passwordField != null) passwordField.setDisable(busy);
        if (deleteButton != null) deleteButton.setDisable(busy);
    }

    private String textOrNull(TextField tf) {
        return tf == null ? null : tf.getText();
    }

    @FXML
    private void back() {
        redirectToSecretaries();
    }
    private void updateFormTexts() {
        if (formTitle == null || formSubtitle == null) return;

        if (mode == Mode.CREATE) {
            formTitle.setText("Dodaj novog sekretara");
            formSubtitle.setText("Unesite podatke o novom sekretaru");
        } else {
            formTitle.setText("Uredite sekretara");
            formSubtitle.setText("Uredite podatke o sekretaru");
        }
    }
}
