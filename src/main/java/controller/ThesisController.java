package controller;

import Factory.ThesisCardFactory;
import dao.ThesisDAO;
import dao.ThesisStatusDAO;
import dto.ThesisDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import utils.GlobalErrorHandler;
import utils.SceneManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ThesisController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private VBox thesisCardsContainer;
    @FXML private Button btnAddNew;

    private final ThesisDAO dao;
    private final ThesisCardFactory factory;
    private final ThesisStatusDAO statusDAO;

    private List<ThesisDTO> masterList = new ArrayList<>();

    public ThesisController() {
        this.dao = new ThesisDAO();
        this.factory = new ThesisCardFactory();
        this.statusDAO = new ThesisStatusDAO();
    }

    @FXML
    public void initialize() {
        initStatusFilter();
        initSearchListener();
        setupAddButton();
        loadThesises();
    }

    private void setupAddButton() {
        if (btnAddNew != null) {
            btnAddNew.setOnAction(e -> openAddThesisPage());
        }
    }

    private void initStatusFilter() {
        statusFilter.setOnAction(e -> filterThesis());
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return statusDAO.getAllStatuses();
            }
        };
        task.setOnSucceeded(e -> {
            List<String> statusiIzBaze = task.getValue();
            statusFilter.getItems().clear();
            statusFilter.getItems().add("Svi statusi");
            statusFilter.getItems().addAll(statusiIzBaze);
            statusFilter.getSelectionModel().selectFirst();
        });

        task.setOnFailed(e -> {
            statusFilter.getItems().setAll("Svi statusi");
            statusFilter.getSelectionModel().selectFirst();
            GlobalErrorHandler.error("Greška pri učitavanju statusa.", task.getException());
        });

        new Thread(task, "load-thesis-statuses").start();
    }

    private void filterThesis() {
        if (masterList == null || masterList.isEmpty()) return;

        String searchText = searchField.getText().toLowerCase();
        String selectedStatus = statusFilter.getValue();

        List<ThesisDTO> filtriraniRadovi = masterList.stream()
                .filter(rad -> {
                    boolean matchesSearch = searchText.isEmpty() ||
                            rad.getTitle().toLowerCase().contains(searchText) ||
                            rad.getStudentFullName().toLowerCase().contains(searchText) ||
                            rad.getMentorFullName().toLowerCase().contains(searchText);

                    boolean matchesStatus = selectedStatus == null ||
                            selectedStatus.equals("Svi statusi") ||
                            rad.getStatus().equalsIgnoreCase(selectedStatus);

                    return matchesSearch && matchesStatus;
                })
                .collect(Collectors.toList());

        displayTheses(filtriraniRadovi);
    }

    private void initSearchListener() {
        searchField.textProperty().addListener((obs, old, newVal) -> filterThesis());
    }

    public void loadThesises() {
        Task<List<ThesisDTO>> task = new Task<List<ThesisDTO>>() {
            @Override
            protected List<ThesisDTO> call() throws Exception {
                return dao.getAllThesis();
            }
        };
        task.setOnSucceeded(e -> {
            masterList = task.getValue();
            displayTheses(masterList);
        });
        task.setOnFailed(e -> {
            GlobalErrorHandler.error("Greška pri učitavanju završnih radova.", task.getException());
        });
        new Thread(task, "load-theses").start();
    }

    private void displayTheses(List<ThesisDTO> radovi) {
        thesisCardsContainer.getChildren().clear();

        for (ThesisDTO rad : radovi) {
            VBox card = factory.createCard(rad);
            card.setOnMouseClicked(e -> openEditThesisPage(rad));
            thesisCardsContainer.getChildren().add(card);
        }
    }

    private void openAddThesisPage() {
        SceneManager.showWithData(
                "/app/thesisForm.fxml",
                "Dodaj novi završni rad",
                (ThesisFormController controller) -> {
                    controller.initCreate();
                }
        );
    }

    private void openEditThesisPage(ThesisDTO thesisDTO) {
        // Umjesto direktno edit forme, prvo otvaramo details page
        SceneManager.showWithData(
                "/app/thesisDetails.fxml",
                "eDiploma",
                (ThesisDetailsController controller) -> {
                    controller.initWithThesisId(thesisDTO.getId());
                }
        );
    }

    @FXML
    private void handleAddNew() {
        openAddThesisPage();
    }
}