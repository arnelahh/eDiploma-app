package Factory;

import dao.ThesisDAO;
import dto.ThesisDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import model.Thesis;

public class ThesisCardFactory {
    public VBox createCard(ThesisDTO rad) {
        // 1. Root Container (.thesis-card)
        // CSS: -fx-background-color: #ffffff; -fx-effect: dropshadow...
        VBox card = new VBox();
        card.getStyleClass().add("thesis-card");
        card.setPadding(new Insets(20, 25, 20, 25)); // Padding kao u FXML-u
        card.setCursor(Cursor.HAND);

        // Unutrašnji container za razmak
        VBox contentBox = new VBox(15);

        // 2. Naslov (.card-title)
        // CSS: -fx-font-size: 16px; -fx-fill: #2d3436;
        Text title = new Text(rad.getTitle()); // Pazi: getTitle() ili getNaslov()
        title.getStyleClass().add("card-title");
        title.setWrappingWidth(700);
        HBox.setHgrow(title, Priority.ALWAYS);



        // 3. Donji red (ikone i status)
        HBox infoRow = new HBox(30);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        // Info stavke
        HBox studentInfo = createInfoItem("user-icon", rad.getStudentFullName());
        HBox mentorInfo = createInfoItem("doc-icon", rad.getMentorFullName());

        String ciklusTekst = (rad.getCycle() == 1) ? "Prvi ciklus" : "Drugi ciklus";
        HBox cycleInfo = createInfoItem("cycle-icon", ciklusTekst);

        // Spacer (gura status skroz desno)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 4. Status Badge
        HBox statusBadge = createStatusBadge(rad.getStatus());


        // Spajanje
        infoRow.getChildren().addAll(studentInfo, mentorInfo, cycleInfo, spacer, statusBadge);
        contentBox.getChildren().addAll(title, infoRow);
        card.getChildren().add(contentBox);

        return card;
    }
    private HBox createInfoRow(String icon, String text) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("info-icon");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("info-text");

        row.getChildren().addAll(iconLabel, textLabel);
        return row;
    }

    private String getStatusClass(String status) {
        switch(status) {
            case "Na čekanju": return "status-pending";
            case "U procesu": return "status-progress";
            case "Odbranjen": return "status-completed";
            default: return "";
        }
    }
    private HBox createInfoItem(String iconClass, String textContent) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        // Ikona (.user-icon, .doc-icon...)
        Circle icon = new Circle(6);
        icon.getStyleClass().add(iconClass);

        // Tekst (.card-info)
        Text text = new Text(textContent);
        text.getStyleClass().add("card-info");

        box.getChildren().addAll(icon, text);
        return box;
    }

    // Kreira status badge prema statusu rada
    private HBox createStatusBadge(String status) {
        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(6, 12, 6, 12));

        // Dodajemo osnovnu klasu (.status-badge)
        badge.getStyleClass().add("status-badge");

        // Dodajemo specifičnu klasu boje na osnovu statusa
        String cssClass = getStatusCssClass(status);
        badge.getStyleClass().add(cssClass);

        // Indikator tačka (.status-indicator)
        Circle indicator = new Circle(4);
        indicator.getStyleClass().add("status-indicator");

        // Tekst statusa (.status-text)
        Text statusText = new Text(status);
        statusText.getStyleClass().add("status-text");

        // Važno: Moramo proslijediti CSS klasu roditelja (npr status-waiting)
        // kako bi child elementi znali koju boju da uzmu, jer u tvom CSS-u piše:
        // .status-waiting .status-text { ... }
        // Ali pošto su Text i Circle UNUTAR badge-a koji već ima klasu,
        // CSS selektor ".status-waiting .status-text" će raditi automatski.

        badge.getChildren().addAll(indicator, statusText);
        return badge;
    }

    // Mapiranje teksta iz baze u CSS klasu
    private String getStatusCssClass(String status) {
        switch (status) {
            case "Odbranjen": return "status-approved"; // Zelena
            case "U procesu": return "status-in-progress"; // Žuta
            case "Na čekanju": return "status-pending"; // Siva/Plava
            default: return "status-default";
        }
    }


}
