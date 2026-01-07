package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import utils.SceneManager;
import utils.UserSession;

public class SecretaryDashboardController {

    @FXML private Label radoviLabel;
    @FXML private Label accountSettingsLabel;
    @FXML private Label lblOdjava;

    @FXML
    private void handleRadoviClick() {
        SceneManager.show("/app/thesis.fxml", "Završni radovi");
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
