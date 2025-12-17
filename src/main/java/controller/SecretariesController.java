package controller;

import Factory.SecretaryCardFactory;
import dao.SecretaryDAO;
import dto.SecretaryDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.AppUser;
import utils.SceneManager;

public class SecretariesController {

    @FXML
    private VBox secretariesCardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ProgressIndicator loader;

    @FXML
    private Button addSecretaryButton;

    private final SecretaryDAO secretaryDAO = new SecretaryDAO();
    private final SecretaryCardFactory cardFactory = new SecretaryCardFactory();

    private final ObservableList<SecretaryDTO> masterList =
            FXCollections.observableArrayList();

    private FilteredList<SecretaryDTO> filteredList;

    private static boolean needsRefresh = false;

    @FXML
    public void initialize() {
        setupAddButton();
        setupSearch();
        loadSecretariesAsync();

        if(needsRefresh){
            needsRefresh=false;
        }
    }

    public static void requestRefresh() {
        needsRefresh = true;
    }

    private void loadSecretariesAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                masterList.clear();
                masterList.addAll(secretaryDAO.getAllSecretaries());
                return null;
            }
        };

        loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            filteredList = new FilteredList<>(masterList, s -> true);
            renderSecretaries(filteredList);
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showError("Greška pri učitavanju sekretara");
        });

        new Thread(task, "load-secretaries-thread").start();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredList == null) return;

            String term = newVal == null ? "" : newVal.toLowerCase().trim();

            filteredList.setPredicate(dto -> {
                if (term.isEmpty()) return true;
                AppUser secretary = dto.getSecretary();

                return (secretary.getUsername() != null && secretary.getUsername().toLowerCase().contains(term))
                        || (secretary.getEmail() != null && secretary.getEmail().toLowerCase().contains(term));
            });

            renderSecretaries(filteredList);
        });
    }

    private void renderSecretaries(Iterable<SecretaryDTO> secretaries) {
        secretariesCardsContainer.getChildren().clear();

        for (SecretaryDTO secretaryDTO : secretaries) {
            secretariesCardsContainer.getChildren().add(
                    cardFactory.create(secretaryDTO, this::openEditSecretaryPage)
            );
        }
    }

    private void setupAddButton() {
        if (addSecretaryButton != null) {
            addSecretaryButton.setOnAction(e -> openAddSecretaryPage());
        }
    }

    private void openAddSecretaryPage() {
        SceneManager.showWithData(
                "/app/secretaryForm.fxml",
                "Dodaj novog sekretara",
                (SecretaryFormController controller) -> {
                    controller.initCreate();
                }
        );
    }

    private void openEditSecretaryPage(AppUser secretary) {
        SceneManager.showWithData(
                "/app/secretaryForm.fxml",
                "Uredi sekretara",
                (SecretaryFormController controller) -> {
                    controller.initEdit(secretary);
                }
        );
    }

    private void showError(String msg) {
        Platform.runLater(() ->
                new Alert(Alert.AlertType.ERROR, msg).showAndWait()
        );
    }
}