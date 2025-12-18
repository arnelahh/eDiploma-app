package controller;

import dao.SecretaryDAO;
import dto.CreateSecretaryDTO;
import dto.CreateUserIdsDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import utils.*;

public class SecretaryFormController {

    @FXML private TextField titleField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private ProgressIndicator loader;

    private final SecretaryDAO secretaryDAO = new SecretaryDAO();
    private final SecretaryValidator validator = new SecretaryValidator();

    @FXML
    public void initialize() {
        if (loader != null) loader.setVisible(false);
    }

    @FXML
    private void handleSave() {
        CreateSecretaryDTO dto = CreateSecretaryDTO.builder()
                .title(textOrNull(titleField))
                .firstName(textOrNull(firstNameField))
                .lastName(textOrNull(lastNameField))
                .email(textOrNull(emailField))
                .username(textOrNull(usernameField))
                .rawPassword(passwordField != null ? passwordField.getText() : null)
                .build();

        ValidationResult vr = validator.validate(dto);
        if (!vr.isValid()) {
            show(vr.joined("\n"), Alert.AlertType.ERROR);
            return;
        }

        Task<CreateUserIdsDTO> task = new Task<>() {
            @Override
            protected CreateUserIdsDTO call() throws Exception {
                return secretaryDAO.createSecretary(dto); // DAO hashes password into PasswordHash
            }
        };

        if (loader != null) loader.visibleProperty().bind(task.runningProperty());
        setFormDisabled(true);

        task.setOnSucceeded(e -> {
            setFormDisabled(false);
            if (loader != null) loader.visibleProperty().unbind();

            show("Secretary account created successfully.", Alert.AlertType.INFORMATION);
            redirectToSecretaries();
        });

        task.setOnFailed(e -> {
            setFormDisabled(false);
            if (loader != null) loader.visibleProperty().unbind();
            if (loader != null) loader.setVisible(false);

            Throwable ex = task.getException();
            ex.printStackTrace();
            show("Error: " + (ex != null ? ex.getMessage() : "Unknown error"), Alert.AlertType.ERROR);
        });

        new Thread(task, "create-secretary").start();
    }

    private void redirectToSecretaries() {
        NavigationContext.setTargetView(DashboardView.SECRETARIES); // choose your enum target
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }

    private void setFormDisabled(boolean disabled) {
        if (titleField != null) titleField.setDisable(disabled);
        if (firstNameField != null) firstNameField.setDisable(disabled);
        if (lastNameField != null) lastNameField.setDisable(disabled);
        if (emailField != null) emailField.setDisable(disabled);
        if (usernameField != null) usernameField.setDisable(disabled);
        if (passwordField != null) passwordField.setDisable(disabled);
    }

    private String textOrNull(TextField tf) {
        return tf == null ? null : tf.getText();
    }

    private void show(String msg, Alert.AlertType type) {
        new Alert(type, msg).showAndWait();
    }

    @FXML
    private void back() {
        NavigationContext.setTargetView(DashboardView.SECRETARIES);
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }
}
