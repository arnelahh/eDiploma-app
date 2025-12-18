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

    private final ObservableList<SecretaryDTO> masterList = FXCollections.observableArrayList();
    private FilteredList<SecretaryDTO> filteredList;

    private static boolean needsRefresh = false;

    @FXML
    public void initialize() {
        setupAddButton();
        setupSearch();
        loadSecretariesAsync();

        if (needsRefresh) {
            needsRefresh = false;
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

                AcademicStaff s = dto.getSecretary();
                String username = dto.getUser() != null ? dto.getUser().getUsername() : null;

                return (s.getFirstName() != null && s.getFirstName().toLowerCase().contains(term))
                        || (s.getLastName() != null && s.getLastName().toLowerCase().contains(term))
                        || (s.getEmail() != null && s.getEmail().toLowerCase().contains(term))
                        || (s.getTitle() != null && s.getTitle().toLowerCase().contains(term))
                        || (username != null && username.toLowerCase().contains(term));
            });

            renderSecretaries(filteredList);
        });
    }

    private void renderSecretaries(Iterable<SecretaryDTO> secretaries) {
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
                (SecretaryFormController controller) -> {
                    // You don’t have initCreate(), so just open the empty form
                    // If you add initCreate() later, call it here
                }
        );
    }

    private void openEditSecretaryPage(SecretaryDTO dto) {
        // You currently don’t have edit support in SecretaryFormController.
        // When you add it, pass dto or staff/user ids here.
        SceneManager.showWithData(
                "/app/secretaryForm.fxml",
                "Uredi sekretara",
                (SecretaryFormController controller) -> {
                    // controller.initEdit(dto);  <-- implement later if needed
                }
        );
    }

    private void showError(String msg) {
        Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, msg).showAndWait());
    }
}
