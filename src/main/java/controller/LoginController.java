package controller;

import dao.AppUserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import model.AppUser;
import org.mindrot.jbcrypt.BCrypt;
import utils.SceneManager;
import utils.SessionManager;
import utils.UserSession;

import java.io.IOException;


public class LoginController {
    @FXML
    private Pane rootPane;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;

    public AppUserDAO userDao = new AppUserDAO();

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        AppUser user = userDao.findByEmail(email);

        if (user == null) {
            showError("User not found");
            return;
        }
        if(!user.isActive()) {
            showError("User is not active");
            return;
        }
        if(!BCrypt.checkpw(password,user.getPasswordHash())) {
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

        rootPane.setOnMouseMoved(e -> SessionManager.resetTimer());
        rootPane.setOnKeyPressed(e -> SessionManager.resetTimer());
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
