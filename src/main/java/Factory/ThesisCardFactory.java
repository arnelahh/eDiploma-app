package Factory;


import java.time.LocalDate;
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


public class ThesisCardFactory {

    public VBox createCard(ThesisDTO rad) {
        // 1. Root Container (.thesis-card)
        VBox card = new VBox();
        card.getStyleClass().add("thesis-card");
        card.setPadding(new Insets(20, 25, 20, 25));

        // Unutrašnji container za razmak
        VBox contentBox = new VBox(15);

        // 2. Naslov (.card-title)
        Text title = new Text(rad.getTitle());
        title.getStyleClass().add("card-title");
        title.setWrappingWidth(700);
        HBox.setHgrow(title, Priority.ALWAYS);

        card.setCursor(Cursor.HAND);

        // Ako je rad odbranjen, dodaj poseban stil ali ne disabluj
        if ("Defended".equals(rad.getStatus())) {
            card.getStyleClass().add("card-defended");
        }


        if (isOlderThan90Days(rad.getApplicationDate()) && !"Defended".equals(rad.getStatus())) {
            card.getStyleClass().add("card-overdue");
        }

        // 3. Donji red (ikone i status)
        HBox infoRow = new HBox(30);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        HBox studentInfo = createInfoItem("user-icon", rad.getStudentFullName());
        HBox mentorInfo = createInfoItem("doc-icon", rad.getMentorFullName());
        String ciklusTekst = (rad.getCycle() == 1) ? "Prvi ciklus" : "Drugi ciklus";
        HBox cycleInfo = createInfoItem("cycle-icon", ciklusTekst);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox statusBadge = createStatusBadge(rad.getStatus());

        infoRow.getChildren().addAll(studentInfo, mentorInfo, cycleInfo, spacer, statusBadge);
        contentBox.getChildren().addAll(title, infoRow);
        card.getChildren().add(contentBox);

        return card;
    }


    // Funkcija koja proverava da li je applicationDate stariji od 90 dana
    private boolean isOlderThan90Days(LocalDate applicationDate) {
        if (applicationDate == null) return false;
        return applicationDate.isBefore(LocalDate.now().minusDays(90));
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
            case "Defended": return "status-approved"; // Zelena
            case "Submitted": return "status-in-progress"; // Žuta
            case "Approved": return "status-in-progress";
            case "In Review": return "status-pending"; //plava
            default: return "status-default";
        }
    }


}
