package controller;

import dao.AppUserDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

        Task<AppUser> loginTask = new Task<AppUser>() {
            @Override
            protected AppUser call() throws Exception {
                return userDao.findByEmail(email);
            }
        };

        loginTask.setOnSucceeded(e -> {
            AppUser user = loginTask.getValue();

            loader.setVisible(false);
            emailField.setDisable(false);
            passwordField.setDisable(false);

            if (user == null) {
                showError("User not found");
                return;
            }
            if(!user.isActive()) {
                showError("User is not active");
                return;
            }
            if(!BCrypt.checkpw(password, user.getPasswordHash())) {
                showError("Wrong password");
                return;
            }

            UserSession.setUser(user);

            SceneManager.show("/app/dashboard.fxml", "Dashboard");

            SessionManager.startSession(() -> {
                System.out.println("Session expired!");
                UserSession.clear();
                SceneManager.show("/app/login.fxml", "Login");
            });

            rootPane.setOnMouseMoved(ev -> SessionManager.resetTimer());
            rootPane.setOnKeyPressed(ev -> SessionManager.resetTimer());
        });

        loginTask.setOnFailed(e -> {
            loader.setVisible(false);
            emailField.setDisable(false);
            passwordField.setDisable(false);
            showError("Login failed: " + loginTask.getException().getMessage());
        });

        new Thread(loginTask).start();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.showAndWait();
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}
