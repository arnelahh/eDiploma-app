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
        loadThesises();
    }
    private void initStatusFilter() {
        statusFilter.setOnAction(e -> filterThesis());
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return statusDAO.getAllStatuses(); // Pozivamo novu DAO metodu
            }
        };
        task.setOnSucceeded(e -> {
            List<String> statusiIzBaze = task.getValue();

            // Očistimo ComboBox ako ima nešto
            statusFilter.getItems().clear();

            // PRVO dodamo "Svi statusi" (jer to ne dolazi iz baze)
            statusFilter.getItems().add("Svi statusi");

            // Zatim dodamo ono što smo dobili iz baze
            statusFilter.getItems().addAll(statusiIzBaze);

            // Selektujemo prvu opciju (default)
            statusFilter.getSelectionModel().selectFirst();
        });

        task.setOnFailed(e -> {
            System.out.println("Greška pri učitavanju statusa: " + task.getException().getMessage());
            // Fallback: Ako baza pukne, dodaj bar osnovne ručno da aplikacija radi
            statusFilter.getItems().addAll("Svi statusi", "Greška u učitavanju");
            statusFilter.getSelectionModel().selectFirst();
        });

        // 4. Pokretanje
        new Thread(task).start();
    }

    private void filterThesis() {
        // Ako podaci još nisu stigli iz baze, ne radi ništa
        if (masterList == null || masterList.isEmpty()) return;

        String searchText = searchField.getText().toLowerCase();
        String selectedStatus = statusFilter.getValue();

        List<ThesisDTO> filtriraniRadovi = masterList.stream()
                .filter(rad -> {
                    // 1. Provjera teksta (naslov, student ili mentor)
                    boolean matchesSearch = searchText.isEmpty() ||
                            rad.getTitle().toLowerCase().contains(searchText) ||
                            rad.getStudentFullName().toLowerCase().contains(searchText) || // Pretpostavka da imaš ovu metodu
                            rad.getMentorFullName().toLowerCase().contains(searchText);
                    boolean matchesStatus = selectedStatus == null ||
                            selectedStatus.equals("Svi statusi") ||
                            rad.getStatus().equalsIgnoreCase(selectedStatus);

                    // Vrati true samo ako OBA uslova važe
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
           masterList= task.getValue();
           displayTheses(masterList);
        });
        task.setOnFailed(e -> {
            Throwable greska=task.getException();
            System.out.println("Greška pri učitavanju: " + greska.getMessage());
        });
        new Thread(task).start();
    }

    private void displayTheses(List<ThesisDTO> radovi) {
        thesisCardsContainer.getChildren().clear();

        for (ThesisDTO rad : radovi) {
            VBox card = factory.createCard(rad);
            card.setOnMouseClicked(e -> openDetails(rad));
            thesisCardsContainer.getChildren().add(card);
        }
    }

    private void openDetails(ThesisDTO rad) {
    }

    @FXML
    private void handleAddNew() {
        // Otvori formu za dodavanje
    }

}
