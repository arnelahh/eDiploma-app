package Factory;

import dao.AcademicStaffDAO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import model.AcademicStaff;

import java.util.function.Consumer;

public class MentorCardFactory {

    private final AcademicStaffDAO academicStaffDAO = new AcademicStaffDAO();

    /**
     * Creates a mentor card UI component
     * @param mentor The academic staff member to display
     * @param onEdit Callback function when edit button is clicked
     * @return HBox containing the complete mentor card
     */
    public HBox create(AcademicStaff mentor, Consumer<AcademicStaff> onEdit) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.getStyleClass().add("thesis-card");

        // Mentor info section
        VBox info = createInfoSection(mentor);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Edit button
        Button editBtn = createEditButton(mentor, onEdit);

        card.getChildren().addAll(info, editBtn);
        return card;
    }

    private VBox createInfoSection(AcademicStaff mentor) {
        VBox info = new VBox(8);

        // Full name with academic title
        String fullName = mentor.getFirstName() + " " + mentor.getLastName();
        Text name = new Text(fullName);
        name.getStyleClass().add("card-title");

        // Details row with email and student count
        HBox detailsRow = createDetailsRow(mentor);

        info.getChildren().addAll(name, detailsRow);
        return info;
    }

    private HBox createDetailsRow(AcademicStaff mentor) {
        HBox detailsRow = new HBox(30);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        // Email with icon
        if (mentor.getEmail() != null && !mentor.getEmail().isEmpty()) {
            HBox emailBox = createEmailBox(mentor.getEmail());
            detailsRow.getChildren().add(emailBox);
        }

        return detailsRow;
    }

    private HBox createEmailBox(String email) {
        HBox emailBox = new HBox(8);
        emailBox.setAlignment(Pos.CENTER_LEFT);

        Circle emailIcon = new Circle(6);
        emailIcon.getStyleClass().add("user-icon");

        Text emailText = new Text(email);
        emailText.getStyleClass().add("card-info");

        emailBox.getChildren().addAll(emailIcon, emailText);
        return emailBox;
    }

    private Button createEditButton(AcademicStaff mentor, Consumer<AcademicStaff> onEdit) {
        Button editBtn = new Button("✎");
        editBtn.getStyleClass().add("edit-button-icon");
        editBtn.setOnAction(e -> onEdit.accept(mentor));
        return editBtn;
    }
}
