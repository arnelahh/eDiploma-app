package controller;

import dao.ThesisDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;

public class StatisticsController implements Initializable {

    @FXML private Label lblTotalThesis;
    @FXML private Label lblActiveThesis;
    @FXML private Label lblLateThesis;
    @FXML private VBox vboxTopMentors;
    @FXML private PieChart pieChartSekretari;
    @FXML private ComboBox<String> comboAcademicYear;

    private final ThesisDAO thesisDAO = new ThesisDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupAcademicYearFilter();
        loadDashboardData();
    }

    private void setupAcademicYearFilter() {
        // Generisanje akademskih godina (npr. trenutna i 4 prethodne)
        int currentYear = LocalDate.now().getYear();
        // Ako je trenutno 10. mjesec ili kasnije, to je početak nove akademske godine
        if (LocalDate.now().getMonthValue() < 10) {
            currentYear--;
        }

        ObservableList<String> years = FXCollections.observableArrayList();
        years.add("Sve godine"); // Opcija za prikaz svega

        for (int i = 0; i < 5; i++) {
            int start = currentYear - i;
            int end = start + 1;
            years.add(start + "/" + end);
        }

        comboAcademicYear.setItems(years);

        // Postavi default na "Sve godine" ili trenutnu akademsku godinu
        comboAcademicYear.getSelectionModel().selectFirst();

        // Listener: Kad korisnik promijeni godinu, osvježi listu mentora
        comboAcademicYear.valueProperty().addListener((obs, oldVal, newVal) -> {
            loadTopMentorsFiltered(newVal);
        });
    }

    private void loadDashboardData() {
        lblActiveThesis.setText(String.valueOf(thesisDAO.getActiveThesisCount()));
        lblTotalThesis.setText(String.valueOf(thesisDAO.getTotalThesisCount()));
        lblLateThesis.setText(String.valueOf(thesisDAO.getLateThesisCount()));

        loadTopMentorsFiltered(comboAcademicYear.getValue());
        populateSecretaryChart();
    }

    private void loadTopMentorsFiltered(String selectedYear) {
        LocalDate startDate = null;
        LocalDate endDate = null;

        // Logika za parsiranje datuma: "2024/2025" -> 01.10.2024 do 30.09.2025
        if (selectedYear != null && !selectedYear.equals("Sve godine")) {
            try {
                String[] parts = selectedYear.split("/");
                int startYearInt = Integer.parseInt(parts[0]);
                int endYearInt = Integer.parseInt(parts[1]);

                startDate = LocalDate.of(startYearInt, 10, 1);
                endDate = LocalDate.of(endYearInt, 9, 30);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Poziv nove DAO metode
        Map<String, Integer> topMentors = thesisDAO.getTopMentorsFiltered(startDate, endDate);

        // --- OSTATAK KODA ZA UI JE ISTI KAO PRIJE ---
        vboxTopMentors.getChildren().clear();

        int rank = 1;
        for (Map.Entry<String, Integer> entry : topMentors.entrySet()) {
            if (rank > 5) {
                break;
            }
            HBox row = new HBox();
            row.getStyleClass().add("mentor-row");
            row.setAlignment(Pos.CENTER_LEFT);
            row.setSpacing(15);

            Label lblRank = new Label("#" + rank);
            lblRank.setStyle("-fx-text-fill: #999; -fx-font-weight: bold; -fx-font-size: 14px;");

            Label lblName = new Label(entry.getKey());
            lblName.getStyleClass().add("mentor-name");

            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox badge = new HBox();
            badge.getStyleClass().add("mentor-count-badge");
            badge.setAlignment(Pos.CENTER);

            Label lblCount = new Label(String.valueOf(entry.getValue()));
            lblCount.getStyleClass().add("mentor-count-text");
            badge.getChildren().add(lblCount);

            row.getChildren().addAll(lblRank, lblName, spacer, badge);
            vboxTopMentors.getChildren().add(row);
            rank++;
        }

        if (topMentors.isEmpty()) {
            Label empty = new Label("Nema podataka za odabranu godinu.");
            empty.setStyle("-fx-text-fill: #999; -fx-padding: 10;");
            vboxTopMentors.getChildren().add(empty);
        }
    }

    private void populateSecretaryChart() {
        // Pozivamo novu metodu iz DAO
        Map<String, Integer> secretaryCounts = thesisDAO.getSecretaryThesisCounts();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : secretaryCounts.entrySet()) {
            // Kreiramo podatke: Ime (Broj)
            String label = entry.getKey() + " (" + entry.getValue() + ")";
            PieChart.Data data = new PieChart.Data(label, entry.getValue());
            pieData.add(data);
        }

        pieChartSekretari.setData(pieData);

        pieData.forEach(data -> {
            String tooltipText = String.format("%s: %d radova", data.getName(), (int) data.getPieValue());
            Tooltip tooltip = new Tooltip(tooltipText);
            Tooltip.install(data.getNode(), tooltip);
        });
    }
}