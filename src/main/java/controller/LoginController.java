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
import utils.SessionManager;
import utils.UserSession;

import java.io.IOException;


public class LoginController {

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

        // TODO redirect to dashboard
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.setTitle("eDiploma");
            stage.setMaximized(true);
            stage.show();
            SessionManager.startSession(() -> {
                System.out.println("Session expired!");

                // TODO redirect to login screen
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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
