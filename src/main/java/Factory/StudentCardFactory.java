package Factory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import model.Student;

import java.util.function.Consumer;


public class StudentCardFactory {
    public HBox create(Student student, Consumer<Student> onEdit) {

        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.getStyleClass().add("thesis-card");

        // Avatar
        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.getStyleClass().add("student-avatar");
        avatar.setPrefSize(50, 50);

        Text initials = new Text(
                student.getFirstName().substring(0, 1).toUpperCase() +
                        student.getLastName().substring(0, 1).toUpperCase()
        );
        initials.getStyleClass().add("avatar-text");
        avatar.getChildren().add(initials);

        // Info
        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text name = new Text(
                student.getFirstName() + " " +
                        student.getLastName() + " (" +
                        String.format("%03d", student.getIndexNumber()) + ")"
        );
        name.getStyleClass().add("card-title");

        HBox details = new HBox(30);
        details.setAlignment(Pos.CENTER_LEFT);

        details.getChildren().add(createInfo("user-icon", student.getEmail()));
        details.getChildren().add(createInfo("cycle-icon", cycleText(student)));
        details.getChildren().add(createInfo("doc-icon", student.getStudyProgram()));

        info.getChildren().addAll(name, details);

        // Edit button
        Button edit = new Button("✎");
        edit.getStyleClass().add("edit-button-icon");
        edit.setOnAction(e -> onEdit.accept(student));

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

    private String cycleText(Student s) {
        return s.getCycle() == 1 ? "Prvi ciklus" :
                s.getCycle() == 2 ? "Drugi ciklus" : "—";
    }
}
