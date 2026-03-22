package Factory;

import dto.MentorDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import model.AcademicStaff;

import java.util.function.Consumer;

public class MentorCardFactory {

    public HBox create(MentorDTO mentorDTO, Consumer<AcademicStaff> onEdit) {
        AcademicStaff mentor = mentorDTO.getMentor();

        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.getStyleClass().add("thesis-card");
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(e -> onEdit.accept(mentor));

        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.getStyleClass().add("student-avatar");
        avatar.setPrefSize(50, 50);

        String initials = "";
        if (mentor.getFirstName() != null && !mentor.getFirstName().isEmpty()) {
            initials += mentor.getFirstName().substring(0, 1).toUpperCase();
        }
        if (mentor.getLastName() != null && !mentor.getLastName().isEmpty()) {
            initials += "." + mentor.getLastName().substring(0, 1).toUpperCase() + ".";
        }

        Text initialsText = new Text(initials);
        initialsText.getStyleClass().add("avatar-text");
        avatar.getChildren().add(initialsText);

        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        String fullName = (mentor.getTitle() != null ? mentor.getTitle() + " " : "") +
                mentor.getFirstName() + " " + mentor.getLastName();
        Text name = new Text(fullName);
        name.getStyleClass().add("card-title");

        HBox details = new HBox(20);
        details.setAlignment(Pos.CENTER_LEFT);

        SVGPath emailIcon = createSvgIcon("M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z", "#4f5dff", 0.6);
        details.getChildren().add(createInfoBlock(emailIcon, mentor.getEmail()));

        int count = mentorDTO.getStudentCount();
        String studentText = count + " " + getStudentLabel(count);
        SVGPath totalThesisIcon = createSvgIcon("M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z", "#6B7280", 0.6);
        details.getChildren().add(createInfoBlock(totalThesisIcon, studentText));

        int activeCount = mentorDTO.getOngoingThesisCount();
        String activeLabel = activeCount + " " + getThesisLabel(activeCount);
        SVGPath activeThesisIcon = createSvgIcon("M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z", "#00b894", 0.6);
        details.getChildren().add(createInfoBlock(activeThesisIcon, activeLabel));

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

        Text text = new Text(textValue != null ? textValue : "");
        text.getStyleClass().add("card-info");

        box.getChildren().addAll(icon, text);
        return box;
    }

    private String getStudentLabel(int count) {
        if (count == 1) {
            return "rad";
        } else if (count >= 2 && count <= 4) {
            return "rada";
        } else {
            return "radova";
        }
    }

    private String getThesisLabel(int count) {
        int mod10 = count % 10;
        int mod100 = count % 100;

        if (mod10 == 1 && mod100 != 11) {
            return "aktivan rad";
        } else if (mod10 >= 2 && mod10 <= 4 && (mod100 < 10 || mod100 >= 20)) {
            return "aktivna rada";
        } else {
            return "aktivnih radova";
        }
    }
}