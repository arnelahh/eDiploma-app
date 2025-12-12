package controller;

import dao.AppUserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import model.AppUser;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {

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
        showSuccess("Login successful! Welcome " + user.getUsername());
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
