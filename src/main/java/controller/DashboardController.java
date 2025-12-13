package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import utils.SceneManager;
import utils.UserSession;

public class DashboardController {
    @FXML
    private Label lblOdjava; // povežemo label

    @FXML
    private void handleLogout() {
        try {
            UserSession.clear();

            // Prikaz login scene preko SceneManager-a
            SceneManager.show("/app/login.fxml", "Login");


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
