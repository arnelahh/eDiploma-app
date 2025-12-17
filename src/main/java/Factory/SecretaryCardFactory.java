package Factory;

import dto.SecretaryDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import model.AppUser;

import java.util.function.Consumer;

public class SecretaryCardFactory {

    public HBox create(SecretaryDTO secretaryDTO, Consumer<AppUser> onEdit) {
        AppUser secretary = secretaryDTO.getSecretary();

        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.getStyleClass().add("thesis-card");

        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.getStyleClass().add("student-avatar");
        avatar.setPrefSize(50, 50);

        String initials = "";
        if (secretary.getUsername() != null && !secretary.getUsername().isEmpty()) {
            String[] parts = secretary.getUsername().split(" ");
            if (parts.length > 0 && !parts[0].isEmpty()) {
                initials += parts[0].substring(0, 1).toUpperCase();
            }
            if (parts.length > 1 && !parts[parts.length - 1].isEmpty()) {
                initials += parts[parts.length - 1].substring(0, 1).toUpperCase();
            }
        }

        Text initialsText = new Text(initials);
        initialsText.getStyleClass().add("avatar-text");
        avatar.getChildren().add(initialsText);

        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        String fullName = "";
        if (secretary.getRole() != null && secretary.getRole().getName() != null) {
            fullName = secretary.getRole().getName() + " ";
        }
        fullName += secretary.getUsername();
        Text name = new Text(fullName);
        name.getStyleClass().add("card-title");

        HBox details = new HBox(20);
        details.setAlignment(Pos.CENTER_LEFT);

        details.getChildren().add(createInfo("user-icon", secretary.getEmail()));

        int count = secretaryDTO.getThesisCount();
        String thesisText = count + " " + getThesisLabel(count);
        HBox thesisInfo = new HBox(6);
        thesisInfo.setAlignment(Pos.CENTER_LEFT);
        Circle greenDot = new Circle(4);
        greenDot.setStyle("-fx-fill: #00b894;");
        Text thesisCountText = new Text(thesisText);
        thesisCountText.getStyleClass().add("card-info");
        thesisInfo.getChildren().addAll(greenDot, thesisCountText);
        details.getChildren().add(thesisInfo);

        info.getChildren().addAll(name, details);

        Button edit = new Button("✎");
        edit.getStyleClass().add("edit-button-icon");
        edit.setOnAction(e -> onEdit.accept(secretary));

        card.getChildren().addAll(avatar, info, edit);
        return card;
    }

    private HBox createInfo(String iconClass, String textValue) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        Circle icon = new Circle(6);
        icon.getStyleClass().add(iconClass);

        Text text = new Text(textValue != null ? textValue : "");
        text.getStyleClass().add("card-info");

        box.getChildren().addAll(icon, text);
        return box;
    }

    private String getThesisLabel(int count) {
        if (count == 1) {
            return "rad";
        } else if (count >= 2 && count <= 4) {
            return "rada";
        } else {
            return "radova";
        }
    }
}