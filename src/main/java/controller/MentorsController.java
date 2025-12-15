package controller;

import Factory.MentorCardFactory;
import dao.AcademicStaffDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import model.AcademicStaff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MentorsController {
    private final AcademicStaffDAO academicStaffDAO = new AcademicStaffDAO();
    private final ObservableList<AcademicStaff> masterList = FXCollections.observableArrayList();
    private FilteredList<AcademicStaff> filteredList;
    private final MentorCardFactory cardFactory = new MentorCardFactory();

    @FXML
    private VBox mentorsCardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ProgressIndicator loader;

    @FXML
    private Button addMentorButton;

    @FXML
    public void initialize() {
        setupAddButton();
        setupSearch();
        loadMentorsAsync();
    }

    private void loadMentorsAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                masterList.clear();
                masterList.addAll(academicStaffDAO.getAllAcademicStaff());
                return null;
            }
        };

        loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            filteredList = new FilteredList<>(masterList, m -> true);
            renderMentors(filteredList);
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showError("Greška pri učitavanju mentora");
        });

        new Thread(task, "load-mentors-thread").start();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredList == null) return;

            String term = newVal == null ? "" : newVal.toLowerCase().trim();

            filteredList.setPredicate(mentor -> {
                if (term.isEmpty()) return true;

                String fullName = (mentor.getFirstName() + " " + mentor.getLastName()).toLowerCase();
                String email = mentor.getEmail() != null ? mentor.getEmail().toLowerCase() : "";

                return fullName.contains(term) || email.contains(term);
            });

            renderMentors(filteredList);
        });
    }

    private void renderMentors(Iterable<AcademicStaff> mentors) {
        mentorsCardsContainer.getChildren().clear();

        for (AcademicStaff mentor : mentors) {
            mentorsCardsContainer.getChildren().add(
                    cardFactory.create(mentor, this::openEditMentorPage)
            );
        }
    }

    private void setupAddButton() {
        if (addMentorButton != null) {
            addMentorButton.setOnAction(e -> openAddMentorPage());
        }
    }

    private void openAddMentorPage() {
        System.out.println("Opening add mentor form...");
    }

    private void openEditMentorPage(AcademicStaff mentor) {
        System.out.println("Editing mentor: " + mentor.getFirstName() + " " + mentor.getLastName());
    }

    private void showError(String msg) {
        Platform.runLater(() ->
                new Alert(Alert.AlertType.ERROR, msg).showAndWait()
        );
    }
}
