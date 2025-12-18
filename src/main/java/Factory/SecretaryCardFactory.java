package Factory;

import dto.SecretaryDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
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

        // Avatar (initials)
        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.getStyleClass().add("student-avatar");
        avatar.setPrefSize(50, 50);

        String initialsText = safeInitial(s.getFirstName()) + safeInitial(s.getLastName());
        Text initials = new Text(initialsText);
        initials.getStyleClass().add("avatar-text");
        avatar.getChildren().add(initials);

        // Info
        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text name = new Text(
                (s.getTitle() != null ? s.getTitle() + " " : "") +
                        s.getFirstName() + " " + s.getLastName()
        );
        name.getStyleClass().add("card-title");

        HBox details = new HBox(30);
        details.setAlignment(Pos.CENTER_LEFT);

        details.getChildren().add(createInfo("user-icon", s.getEmail()));
        details.getChildren().add(createInfo("doc-icon", dto.getUser() != null ? dto.getUser().getUsername() : "—"));
        details.getChildren().add(createInfo("cycle-icon", dto.getUser() != null && dto.getUser().isActive() ? "Aktivan" : "Neaktivan"));

        info.getChildren().addAll(name, details);

        // Edit button
        Button edit = new Button("✎");
        edit.getStyleClass().add("edit-button-icon");
        edit.setOnAction(e -> onEdit.accept(dto));

        card.getChildren().addAll(avatar, info, edit);
        return card;
    }

    private HBox createInfo(String iconClass, String textValue) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        Circle icon = new Circle(6);
        icon.getStyleClass().add(iconClass);

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
