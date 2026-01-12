package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;
import utils.UserSession;

import java.util.Optional;

public class DashboardController {
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    public void initialize(){
        DashboardView view = NavigationContext.consumeTargetView();

        switch (view) {
            case STUDENTS -> handleStudentiClick();
            case THESIS -> handleRadoviClick();
            case MENTORS -> handleMentoriClick();
            case SECRETARIES -> handleSekretariClick();
            case STATISTICS -> handleStatisticsClick();
        }
    }

    @FXML
    private void handleLogout() {
        // Create the confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Odjava");
        alert.setHeaderText("Da li ste sigurni da se želite odjaviti?");
        alert.setContentText(null); // Removes the detailed content text to keep it clean

        // Create custom buttons
        ButtonType buttonTypeNo = new ButtonType("Ne");
        ButtonType buttonTypeYes = new ButtonType("Da");

        // Set buttons (Order matters: No first means it appears on the left)
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
    private void handleStudentiClick() {
        try {
            Parent studentsView = FXMLLoader.load(
                    getClass().getResource("/app/students.fxml")
            );
            mainBorderPane.setCenter(studentsView);

        } catch (Exception e) {
            e.printStackTrace();
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

    @FXML
    private void handleMentoriClick() {
        try {
            Parent mentorsView = FXMLLoader.load(
                    getClass().getResource("/app/mentors.fxml")
            );

            mainBorderPane.setCenter(mentorsView);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSekretariClick() {
        try {
            Parent secretariesView = FXMLLoader.load(
                    getClass().getResource("/app/secretaries.fxml")
            );
            mainBorderPane.setCenter(secretariesView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAccountSettings() {
        try {
            Parent accountSettingsView = FXMLLoader.load(
                    getClass().getResource("/app/accountSettings.fxml")
            );
            mainBorderPane.setCenter(accountSettingsView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleStatisticsClick() {
        try {
            Parent statisticsView = FXMLLoader.load(
                    getClass().getResource("/app/statistics.fxml")
            );
            mainBorderPane.setCenter(statisticsView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
