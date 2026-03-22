package Factory;

import java.time.LocalDate;
import dto.ThesisDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class ThesisCardFactory {

    public VBox createCard(ThesisDTO rad) {
        VBox card = new VBox();
        card.getStyleClass().add("thesis-card");
        card.setPadding(new Insets(20, 25, 20, 25));

        VBox contentBox = new VBox(15);

        Text title = new Text(rad.getTitle());
        title.getStyleClass().add("card-title");
        title.setWrappingWidth(700);
        HBox.setHgrow(title, Priority.ALWAYS);

        card.setCursor(Cursor.HAND);

        if ("Odbranjen".equals(rad.getStatus())) {
            card.getStyleClass().add("card-defended");
        }

        if (isOlderThan90Days(rad.getApplicationDate()) && !"Odbranjen".equals(rad.getStatus())) {
            card.getStyleClass().add("card-overdue");
        }

        HBox infoRow = new HBox(30);
        infoRow.setAlignment(Pos.CENTER_LEFT);

        SVGPath studentIcon = createSvgIcon("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z", "#4f5dff", 0.6);
        HBox studentInfo = createInfoItem(studentIcon, rad.getStudentFullName());

        SVGPath mentorIcon = createSvgIcon("M5 13.18v4L12 21l7-3.82v-4L12 17l-7-3.82zM12 3L1 9l11 6 9-4.91V17h2V9L12 3z", "#6B7280", 0.6);
        HBox mentorInfo = createInfoItem(mentorIcon, rad.getMentorFullName());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox statusBadge = createStatusBadge(rad.getStatus());

        infoRow.getChildren().addAll(studentInfo, mentorInfo, spacer, statusBadge);
        contentBox.getChildren().addAll(title, infoRow);
        card.getChildren().add(contentBox);

        return card;
    }

    private boolean isOlderThan90Days(LocalDate applicationDate) {
        if (applicationDate == null) return false;
        return applicationDate.isBefore(LocalDate.now().minusDays(90));
    }

    private SVGPath createSvgIcon(String pathData, String colorHex, double scale) {
        SVGPath icon = new SVGPath();
        icon.setContent(pathData);
        icon.setStyle("-fx-fill: " + colorHex + ";");
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        return icon;
    }

    private HBox createInfoItem(SVGPath icon, String textContent) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        Text text = new Text(textContent != null ? textContent : "—");
        text.getStyleClass().add("card-info");
        box.getChildren().addAll(icon, text);
        return box;
    }

    private HBox createStatusBadge(String status) {
        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(6, 12, 6, 12));

        badge.getStyleClass().add("status-badge");

        String cssClass = getStatusCssClass(status);
        badge.getStyleClass().add(cssClass);

        Circle indicator = new Circle(4);
        indicator.getStyleClass().add("status-indicator");

        Text statusText = new Text(status);
        statusText.getStyleClass().add("status-text");

        badge.getChildren().addAll(indicator, statusText);
        return badge;
    }
    
    private String getStatusCssClass(String status) {
        if (status == null) return "status-default";

        switch (status) {
            case "Odbranjen": return "status-approved"; // Zelena
            case "Submitted":
            case "Approved": return "status-in-progress"; // Žuta
            case "In Review": return "status-pending"; // Plava
            default: return "status-default";
        }
    }
}