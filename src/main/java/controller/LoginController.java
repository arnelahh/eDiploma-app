package controller;

import dao.AppUserDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import model.AppUser;
import org.mindrot.jbcrypt.BCrypt;
import utils.SceneManager;
import utils.SessionManager;
import utils.UserSession;

public class LoginController {
    @FXML
    private Pane rootPane;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private ProgressIndicator loader;

    public AppUserDAO userDao = new AppUserDAO();

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        loader.setVisible(true);
        emailField.setDisable(true);
        passwordField.setDisable(true);

        javafx.concurrent.Task<AppUser> task = new javafx.concurrent.Task<>() {
            @Override
            protected AppUser call() {
                return userDao.findByEmail(email);
            }
        };

        task.setOnSucceeded(e -> {
            AppUser user = task.getValue();
            loader.setVisible(false);
            emailField.setDisable(false);
            passwordField.setDisable(false);

            if (user == null) {
                showError("User not found");
                return;
            }
            if (!user.isActive()) {
                showError("User is not active");
                return;
            }
            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                showError("Wrong password");
                return;
            }

            UserSession.setUser(user);
            SceneManager.show("/app/dashboard.fxml", "eDiploma");

            SessionManager.startSession(() -> {
                System.out.println("Session expired!");
                UserSession.clear();
                SceneManager.show("/app/login.fxml", "eDiploma");
            });

            rootPane.setOnMouseMoved(ev -> SessionManager.resetTimer());
            rootPane.setOnKeyPressed(ev -> SessionManager.resetTimer());
        });

        task.setOnFailed(e -> {
            loader.setVisible(false);
            emailField.setDisable(false);
            passwordField.setDisable(false);
            showError("Login failed: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button togglePasswordButton;

    @FXML
    private void togglePasswordVisibility() {
        if (passwordTextField.isVisible()) {
            // Sakrij textField, pokaži passwordField
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordField.setVisible(true);
        } else {
            // Pokaži textField, sakrij passwordField
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
        }
    }
}
