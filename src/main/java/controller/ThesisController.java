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
import model.AppUser;
import utils.GlobalErrorHandler;
import utils.SceneManager;
import utils.UserSession;

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

    // Active theses (excluding graduated)
    private List<ThesisDTO> masterList = new ArrayList<>();
    
    // Graduated theses cache
    private List<ThesisDTO> graduatedThesesCache = null;
    private boolean graduatedLoaded = false;

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
            // Provjeri tip korisnika
            AppUser currentUser = UserSession.getUser();
            
            // Ako je sekretar, sakrij dugme "Dodaj novi rad"
            if (currentUser != null && currentUser.getRole() != null) {
                String roleName = currentUser.getRole().getName();
                
                if ("SECRETARY".equalsIgnoreCase(roleName)) {
                    btnAddNew.setVisible(false);
                    btnAddNew.setManaged(false);
                    return;
                }
            }
            
            // Za ostale korisnike, dugme je vidljivo
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

    /**
     * Glavni filter metod sa lazy loading logikom
     */
    private void filterThesis() {
        String selectedStatus = statusFilter.getValue();
        
        // Ako je selektovan "Odbranjen" status
        if (selectedStatus != null && selectedStatus.equalsIgnoreCase("Odbranjen")) {
            if (!graduatedLoaded) {
                // Učitaj odbranene radove prvi put
                loadGraduatedTheses();
                return;
            } else {
                // Koristi keširane odbranene radove
                filterWithGraduated();
                return;
            }
        }
        
        // Ako je "Svi statusi" i odbraneni nisu učitani, samo prikaži aktivne
        if (selectedStatus != null && selectedStatus.equals("Svi statusi") && !graduatedLoaded) {
            filterStandard();
            return;
        }
        
        // Ako je "Svi statusi" i odbraneni JESU učitani, prikaži sve
        if (selectedStatus != null && selectedStatus.equals("Svi statusi") && graduatedLoaded) {
            filterWithGraduated();
            return;
        }
        
        // Standardni filter bez odbranenih
        filterStandard();
    }

    /**
     * Standardni filter - samo aktivni radovi (bez odbranenih)
     */
    private void filterStandard() {
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

    /**
     * Filter koji uključuje i odbranene radove (kombinuje masterList + graduatedCache)
     */
    private void filterWithGraduated() {
        // Kombinuj masterList sa graduatedThesesCache
        List<ThesisDTO> combined = new ArrayList<>(masterList);
        if (graduatedThesesCache != null) {
            combined.addAll(graduatedThesesCache);
        }
        
        String searchText = searchField.getText().toLowerCase();
        String selectedStatus = statusFilter.getValue();
        
        List<ThesisDTO> filtered = combined.stream()
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
        
        displayTheses(filtered);
    }

    /**
     * Lazy load odbranenih radova
     */
    private void loadGraduatedTheses() {
        Task<List<ThesisDTO>> task = new Task<>() {
            @Override
            protected List<ThesisDTO> call() throws Exception {
                AppUser currentUser = UserSession.getUser();
                
                if (currentUser != null && currentUser.getRole() != null) {
                    String roleName = currentUser.getRole().getName();
                    
                    if ("SECRETARY".equalsIgnoreCase(roleName)) {
                        return dao.getGraduatedThesisBySecretaryId(currentUser.getId());
                    }
                }
                
                return dao.getGraduatedTheses();
            }
        };
        
        task.setOnSucceeded(e -> {
            graduatedThesesCache = task.getValue();
            graduatedLoaded = true;
            System.out.println("[ThesisController] Loaded " + graduatedThesesCache.size() + " graduated theses.");
            filterWithGraduated();
        });
        
        task.setOnFailed(e -> {
            GlobalErrorHandler.error("Greška pri učitavanju odbranenih radova.", task.getException());
        });
        
        new Thread(task, "load-graduated-theses").start();
    }

    private void initSearchListener() {
        searchField.textProperty().addListener((obs, old, newVal) -> filterThesis());
    }

    /**
     * Inicijalno učitavanje - SVE OSIM ODBRANENIH
     */
    public void loadThesises() {
        Task<List<ThesisDTO>> task = new Task<List<ThesisDTO>>() {
            @Override
            protected List<ThesisDTO> call() throws Exception {
                // Dohvatanje trenutno ulogovanog korisnika
                AppUser currentUser = UserSession.getUser();
                
                // Provjera role korisnika
                if (currentUser != null && currentUser.getRole() != null) {
                    String roleName = currentUser.getRole().getName();
                    
                    // Ako je sekretar, dohvati samo njegove radove (osim odbranenih)
                    if ("SECRETARY".equalsIgnoreCase(roleName)) {
                        return dao.getThesisBySecretaryIdExcludingGraduated(currentUser.getId());
                    }
                }
                
                // Za sve ostale korisnike (ADMINISTRATOR itd.), dohvati sve radove (osim odbranenih)
                return dao.getAllThesisExcludingGraduated();
            }
        };
        task.setOnSucceeded(e -> {
            masterList = task.getValue();
            System.out.println("[ThesisController] Loaded " + masterList.size() + " active theses (excluding graduated).");
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
