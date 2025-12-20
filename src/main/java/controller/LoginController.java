package controller;

import dao.AppUserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import model.AppUser;
import org.mindrot.jbcrypt.BCrypt;
import utils.SceneManager;
import utils.SessionManager;
import utils.UserSession;
import utils.GlobalErrorHandler;

public class LoginController {
    @FXML private Pane rootPane;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private ProgressIndicator loader;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordButton;

    public AppUserDAO userDao = new AppUserDAO();

    @FXML
    public void initialize() {
        // Povezivanje polja
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());
        passwordTextField.setVisible(false);
    }

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
                GlobalErrorHandler.error("User not found");
                return;
            }
            if (!user.isActive()) {
                GlobalErrorHandler.error("User is not active");
                return;
            }
            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                GlobalErrorHandler.error("Wrong password");
                return;
            }

            UserSession.setUser(user);
            SceneManager.show("/app/dashboard.fxml", "eDiploma");

            SessionManager.startSession(() -> {
                System.out.println("Session expired!");
                UserSession.clear();
                SceneManager.show("/app/login.fxml", "eDiploma");
            });
            if(rootPane != null) {
                rootPane.setOnMouseMoved(ev -> SessionManager.resetTimer());
                rootPane.setOnKeyPressed(ev -> SessionManager.resetTimer());
            }
        });

        task.setOnFailed(e -> {
            loader.setVisible(false);
            emailField.setDisable(false);
            passwordField.setDisable(false);
            GlobalErrorHandler.error("Login failed: " + task.getException());
        });

        new Thread(task, "login-thread").start();
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean visible = passwordTextField.isVisible();
        passwordTextField.setVisible(!visible);
        passwordField.setVisible(visible);
    }
}
