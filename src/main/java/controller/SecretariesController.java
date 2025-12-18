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
import model.AcademicStaff;
import utils.SceneManager;

public class SecretariesController {

    @FXML private VBox secretariesCardsContainer;
    @FXML private TextField searchField;
    @FXML private ProgressIndicator loader;
    @FXML private Button addSecretaryButton;

    private final SecretaryDAO secretaryDAO = new SecretaryDAO();
    private final SecretaryCardFactory cardFactory = new SecretaryCardFactory();

    private final ObservableList<SecretaryDTO> masterList = FXCollections.observableArrayList();
    private FilteredList<SecretaryDTO> filteredList;

    private static boolean needsRefresh = false;

    @FXML
    public void initialize() {
        setupAddButton();
        setupSearch();

        loadSecretariesAsync();
        needsRefresh = false;
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

        if (loader != null) loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            if (loader != null) loader.visibleProperty().unbind();
            if (loader != null) loader.setVisible(false);

            filteredList = new FilteredList<>(masterList, s -> true);
            renderSecretaries(filteredList);
        });

        task.setOnFailed(e -> {
            if (loader != null) loader.visibleProperty().unbind();
            if (loader != null) loader.setVisible(false);

            task.getException().printStackTrace();
            showError("Greška pri učitavanju sekretara");
        });

        new Thread(task, "load-secretaries-thread").start();
    }

    private void setupSearch() {
        if (searchField == null) return;

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredList == null) return;

            String term = newVal == null ? "" : newVal.toLowerCase().trim();

            filteredList.setPredicate(dto -> {
                if (term.isEmpty()) return true;

                AcademicStaff s = dto.getSecretary();
                String username = (dto.getUser() != null) ? dto.getUser().getUsername() : null;

                return (s != null && s.getFirstName() != null && s.getFirstName().toLowerCase().contains(term))
                        || (s != null && s.getLastName() != null && s.getLastName().toLowerCase().contains(term))
                        || (s != null && s.getEmail() != null && s.getEmail().toLowerCase().contains(term))
                        || (s != null && s.getTitle() != null && s.getTitle().toLowerCase().contains(term))
                        || (username != null && username.toLowerCase().contains(term));
            });

            renderSecretaries(filteredList);
        });
    }

    private void renderSecretaries(Iterable<SecretaryDTO> secretaries) {
        if (secretariesCardsContainer == null) return;

        secretariesCardsContainer.getChildren().clear();
        for (SecretaryDTO dto : secretaries) {
            secretariesCardsContainer.getChildren().add(
                    cardFactory.create(dto, this::openEditSecretaryPage)
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
                (SecretaryFormController controller) -> controller.initCreate()
        );
    }

    private void openEditSecretaryPage(SecretaryDTO dto) {
        SceneManager.showWithData(
                "/app/secretaryForm.fxml",
                "Uredi sekretara",
                (SecretaryFormController controller) -> controller.initEdit(dto)
        );
    }

    private void showError(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, msg).showAndWait());
    }
}
