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
import model.Thesis;
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
            System.out.println("Greška pri učitavanju statusa: " + task.getException().getMessage());
            statusFilter.getItems().addAll("Svi statusi", "Greška u učitavanju");
            statusFilter.getSelectionModel().selectFirst();
        });

        new Thread(task).start();
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
            Throwable greska = task.getException();
            System.out.println("Greška pri učitavanju: " + greska.getMessage());
        });
        new Thread(task).start();
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
        // Prvo moramo učitati kompletan Thesis objekat iz baze
        Task<Thesis> task = new Task<>() {
            @Override
            protected Thesis call() throws Exception {
                return dao.getThesisById(thesisDTO.getId());
            }
        };

        task.setOnSucceeded(e -> {
            Thesis thesis = task.getValue();
            if (thesis != null) {
                SceneManager.showWithData(
                        "/app/thesisForm.fxml",
                        "Uredi završni rad",
                        (ThesisFormController controller) -> {
                            controller.initEdit(thesis);
                        }
                );
            } else {
                System.out.println("Rad nije pronađen!");
            }
        });

        task.setOnFailed(e -> {
            System.out.println("Greška pri učitavanju rada: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    @FXML
    private void handleAddNew() {
        openAddThesisPage();
    }
}