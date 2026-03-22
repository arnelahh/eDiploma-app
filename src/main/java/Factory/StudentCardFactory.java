package Factory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import model.Student;

import java.util.function.Consumer;

public class StudentCardFactory {

    public HBox create(Student student, Consumer<Student> onEdit) {

        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.getStyleClass().add("thesis-card");

        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> onEdit.accept(student));

        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.getStyleClass().add("student-avatar");
        avatar.setPrefSize(50, 50);

        String initialsText = student.getFirstName().substring(0, 1).toUpperCase() + "." +
                                student.getLastName().substring(0, 1).toUpperCase() + ".";
        Text initials = new Text(initialsText);
        initials.getStyleClass().add("avatar-text");
        avatar.getChildren().add(initials);

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

        SVGPath emailIcon = createSvgIcon("M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z", "#6B7280", 0.6);
        details.getChildren().add(createInfoBlock(emailIcon, student.getEmail()));

        String programName = student.getStudyProgram() != null ? student.getStudyProgram() : "Nepoznat smjer";
        SVGPath programIcon = getProgramIcon(programName);
        details.getChildren().add(createInfoBlock(programIcon, programName));

        info.getChildren().addAll(name, details);

        card.getChildren().addAll(avatar, info);
        return card;
    }

    private SVGPath getProgramIcon(String program) {
        String path;
        String color;
        switch (program.toLowerCase()) {
            case "softversko inženjerstvo":
                path = "M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z";
                color = "#4f5dff";
                break;
            case "građevinarstvo":
                path = "M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z";
                color = "#f59e0b";
                break;
            case "proizvodni biznis":
                path = "M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z";
                color = "#10b981";
                break;
            default:
                path = "M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3z";
                color = "#6B7280"; // Siva
                break;
        }
        return createSvgIcon(path, color, 0.6);
    }

    // Pomoćna metoda za kreiranje SVG ikonica
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

        Text text = new Text(textValue != null && !textValue.trim().isEmpty() ? textValue : "—");
        text.getStyleClass().add("card-info");

        box.getChildren().addAll(icon, text);
        return box;
    }
}