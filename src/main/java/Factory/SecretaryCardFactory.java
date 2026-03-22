package Factory;

import dto.SecretaryDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import model.AcademicStaff;

import java.util.function.Consumer;

public class SecretaryCardFactory {

    public HBox create(SecretaryDTO dto, Consumer<SecretaryDTO> onEdit) {
        AcademicStaff s = dto.getSecretary();

        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.getStyleClass().add("thesis-card");
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> onEdit.accept(dto));

        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.getStyleClass().add("student-avatar");
        avatar.setPrefSize(50, 50);

        String initialsText = safeInitial(s.getFirstName()) + safeInitial(s.getLastName());
        Text initials = new Text(initialsText);
        initials.getStyleClass().add("avatar-text");
        avatar.getChildren().add(initials);

        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text name = new Text(
                (s.getTitle() != null ? s.getTitle() + " " : "") +
                        s.getFirstName() + " " + s.getLastName()
        );
        name.getStyleClass().add("card-title");

        HBox details = new HBox(30);
        details.setAlignment(Pos.CENTER_LEFT);

        SVGPath emailIcon = createSvgIcon("M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z", "#6B7280", 0.6);
        details.getChildren().add(createInfoBlock(emailIcon, s.getEmail()));

        info.getChildren().addAll(name, details);

        card.getChildren().addAll(avatar, info);
        return card;
    }

    private SVGPath createSvgIcon(String pathData, String colorHex, double scale) {
        SVGPath icon = new SVGPath();
        icon.setContent(pathData);
        icon.setStyle("-fx-fill: " + colorHex + ";");
        icon.setScaleX(scale);
        icon.setScaleY(scale);
        return icon;
    }

    private HBox createInfoBlock(SVGPath icon, String textValue) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);

        Text text = new Text(textValue != null ? textValue : "—");
        text.getStyleClass().add("card-info");

        box.getChildren().addAll(icon, text);
        return box;
    }

    private String safeInitial(String s) {
        if (s == null || s.isBlank()) return "—";
        return s.substring(0, 1).toUpperCase();
    }
}