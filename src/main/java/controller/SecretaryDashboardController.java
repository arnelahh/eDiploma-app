package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import utils.SceneManager;
import utils.UserSession;

import java.util.Optional;

public class SecretaryDashboardController {
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize(){
        // Automatski učitaj završne radove pri pokretanju
        handleRadoviClick();
    }

    @FXML
    private void handleLogout() {
        // Create the confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Odjava");
        alert.setHeaderText("Da li ste sigurni da se želite odjaviti?");
        alert.setContentText(null);

        // Create custom buttons
        ButtonType buttonTypeNo = new ButtonType("Ne");
        ButtonType buttonTypeYes = new ButtonType("Da");

        // Set buttons
        alert.getButtonTypes().setAll(buttonTypeNo, buttonTypeYes);

        // Show and wait for response
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == buttonTypeYes) {
            try {
                UserSession.clear();
                SceneManager.show("/app/login.fxml", "eDiploma");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRadoviClick(){
        try{
            Parent worksView = FXMLLoader.load(
                    getClass().getResource("/app/thesis.fxml")
            );

            mainBorderPane.setCenter(worksView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
