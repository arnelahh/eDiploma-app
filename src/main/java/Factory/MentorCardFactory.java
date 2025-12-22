package Factory;

import dto.MentorDTO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
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

        details.getChildren().add(createInfo("user-icon", mentor.getEmail()));

        int count = mentorDTO.getStudentCount();
        String studentText = count + " " + getStudentLabel(count);
        HBox studentInfo = new HBox(6);
        studentInfo.setAlignment(Pos.CENTER_LEFT);
        Circle greenDot = new Circle(4);
        greenDot.setStyle("-fx-fill: #00b894;");
        Text studentCountText = new Text(studentText);
        studentCountText.getStyleClass().add("card-info");
        studentInfo.getChildren().addAll(greenDot, studentCountText);
        details.getChildren().add(studentInfo);

        // Dohvatimo broj (koji smo izračunali onim SQL upitom)
        int activeCount = mentorDTO.getOngoingThesisCount();

// Kreiramo tekst (npr. "2 aktivne teze")
        String activeLabel = activeCount + " " + getThesisLabel(activeCount);

        HBox ongoingInfo = new HBox(6);
        ongoingInfo.setAlignment(Pos.CENTER_LEFT);

// Narančasta točka za "U izradi / Aktivno"
        Circle orangeDot = new Circle(4);
        orangeDot.setStyle("-fx-fill: #ff9f43;"); // Pastelna narančasta

        Text ongoingCountText = new Text(activeLabel);
// Koristimo istu klasu za stil teksta radi konzistentnosti
        ongoingCountText.getStyleClass().add("card-info");

        ongoingInfo.getChildren().addAll(orangeDot, ongoingCountText);
        details.getChildren().add(ongoingInfo);

        info.getChildren().addAll(name, details);

        card.getChildren().addAll(avatar, info);
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

    private String getStudentLabel(int count) {
        if (count == 1) {
            return "student";
        } else if (count >= 2 && count <= 4) {
            return "studenta";
        } else {
            return "studenata";
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
