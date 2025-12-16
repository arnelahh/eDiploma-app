package controller;

import Factory.MentorCardFactory;
import dao.MentorDAO;
import dto.MentorDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.AcademicStaff;
import utils.SceneManager;

public class MentorsController {

    @FXML
    private VBox mentorsCardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ProgressIndicator loader;

    @FXML
    private Button addMentorButton;

    private final MentorDAO mentorDAO = new MentorDAO();
    private final MentorCardFactory cardFactory = new MentorCardFactory();

    private final ObservableList<MentorDTO> masterList =
            FXCollections.observableArrayList();

    private FilteredList<MentorDTO> filteredList;

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
                masterList.addAll(mentorDAO.getAllMentors());
                return null;
            }
        };

        loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            filteredList = new FilteredList<>(masterList, s -> true);
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

            filteredList.setPredicate(dto -> {
                if (term.isEmpty()) return true;
                AcademicStaff mentor = dto.getMentor();

                return (mentor.getFirstName() != null && mentor.getFirstName().toLowerCase().contains(term))
                        || (mentor.getLastName() != null && mentor.getLastName().toLowerCase().contains(term))
                        || (mentor.getEmail() != null && mentor.getEmail().toLowerCase().contains(term))
                        || (mentor.getTitle() != null && mentor.getTitle().toLowerCase().contains(term));
            });

            renderMentors(filteredList);
        });
    }

    private void renderMentors(Iterable<MentorDTO> mentors) {
        mentorsCardsContainer.getChildren().clear();

        for (MentorDTO mentorDTO : mentors) {
            mentorsCardsContainer.getChildren().add(
                    cardFactory.create(mentorDTO, this::openEditMentorPage)
            );
        }
    }

    private void setupAddButton() {
        if (addMentorButton != null) {
            addMentorButton.setOnAction(e -> openAddMentorPage());
        }
    }

    private void openAddMentorPage() {
        SceneManager.showWithData(
                "/app/mentorForm.fxml",
                "Dodaj novog mentora",
                (MentorFormController controller) -> {
                    controller.initCreate();
                }
        );
    }

    private void openEditMentorPage(AcademicStaff mentor) {
        SceneManager.showWithData(
                "/app/mentorForm.fxml",
                "Uredi mentora",
                (MentorFormController controller) -> {
                    controller.initEdit(mentor);
                }
        );
    }

    private void showError(String msg) {
        Platform.runLater(() ->
                new Alert(Alert.AlertType.ERROR, msg).showAndWait()
        );
    }
}
