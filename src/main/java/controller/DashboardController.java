package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import utils.SceneManager;
import utils.UserSession;

public class DashboardController {

    @FXML private Label radoviLabel;
    @FXML private Label studentiLabel;
    @FXML private Label mentoriLabel;
    @FXML private Label sekretariLabel;
    @FXML private Label accountSettingsLabel;
    @FXML private Label lblOdjava;

    @FXML
    private void handleRadoviClick() {
        SceneManager.show("/app/thesis.fxml", "Završni radovi");
    }

    @FXML
    private void handleStudentiClick() {
        SceneManager.show("/app/students.fxml", "Studenti");
    }

    @FXML
    private void handleMentoriClick() {
        SceneManager.show("/app/mentors.fxml", "Mentori");
    }

    @FXML
    private void handleSekretariClick() {
        SceneManager.show("/app/secretaries.fxml", "Sekretari");
    }

    @FXML
    private void handleAccountSettings() {
        SceneManager.show("/app/accountSettings.fxml", "Postavke naloga");
    }

    @FXML
    private void handleLogout() {
        UserSession.clearSession();
        SceneManager.show("/app/login.fxml", "Prijava");
    }
}
