package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;
import utils.UserSession;

public class DashboardController {
    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label lblOdjava;

    @FXML
    private Label pocetnaLabel, radoviLabel, studentiLabel, mentoriLabel;

    @FXML
    private AnchorPane rootPane;

    @FXML
    public void initialize(){
        DashboardView view = NavigationContext.consumeTargetView();

        switch (view) {
            case STUDENTS -> handleStudentiClick();
            case WORKS -> handleRadoviClick();
            case MENTORS -> handleMentoriClick();
            case HOME -> handlePocetnaClick();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            UserSession.clear();
            SceneManager.show("/app/login.fxml", "Login");

        } catch (Exception e) {
            e.printStackTrace();
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
    private void handlePocetnaClick(){
        try{
            Parent homeView = FXMLLoader.load(
                    getClass().getResource("/app/home.fxml")
            );

            mainBorderPane.setCenter(homeView);

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
}
