package controller;

import dao.StudentDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import model.Student;
import utils.SceneManager;

import java.util.List;

public class StudentsController {

    @FXML
    private VBox studentsCardsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ProgressIndicator loader;

    @FXML
    private Button addStudentButton;

    private final StudentDAO studentDAO = new StudentDAO();

    @FXML
    public void initialize() {
        loadStudentsAsync();
        setupSearch();
        setupAddButton();
    }

    private void setupAddButton() {
        if (addStudentButton != null) {
            addStudentButton.setOnAction(e -> openAddStudentPage());
        }
    }

    /* =========================================================
       ============ ASYNC LOAD STUDENATA =======================
       ========================================================= */
    private void loadStudentsAsync() {
        studentsCardsContainer.getChildren().clear();

        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() {
                return studentDAO.getAllStudents();
            }
        };

        loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            for (Student s : task.getValue()) {
                studentsCardsContainer.getChildren().add(createStudentCard(s));
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showError("Greška pri učitavanju studenata");
        });

        new Thread(task).start();
    }

    /* =========================================================
       ================= SEARCH (ASYNC) ========================
       ========================================================= */
    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            performSearchAsync(newVal);
        });
    }

    private void performSearchAsync(String term) {
        studentsCardsContainer.getChildren().clear();

        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() {
                if (term == null || term.trim().isEmpty()) {
                    return studentDAO.getAllStudents();
                }
                return studentDAO.searchStudents(term);
            }
        };

        loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            for (Student s : task.getValue()) {
                studentsCardsContainer.getChildren().add(createStudentCard(s));
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showError("Greška pri pretrazi");
        });

        new Thread(task).start();
    }


    private HBox createStudentCard(Student student) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20, 25, 20, 25));
        card.getStyleClass().add("thesis-card");

        // Avatar with initials
        VBox avatar = new VBox();
        avatar.setAlignment(Pos.CENTER);
        avatar.getStyleClass().add("student-avatar");
        avatar.setPrefSize(50, 50);
        avatar.setMinSize(50, 50);
        avatar.setMaxSize(50, 50);

        Text initials = new Text(
                student.getFirstName().substring(0, 1).toUpperCase() +
                        student.getLastName().substring(0, 1).toUpperCase()
        );
        initials.getStyleClass().add("avatar-text");
        avatar.getChildren().add(initials);

        // Student info section
        VBox info = new VBox(8);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text name = new Text(
                student.getFirstName() + " " +
                        student.getLastName() + " (" +
                        String.format("%03d", student.getIndexNumber()) + ")"
        );
        name.getStyleClass().add("card-title");

        // Details row with icons
        HBox detailsRow = new HBox(30);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        // Email with icon
        HBox emailBox = new HBox(8);
        emailBox.setAlignment(Pos.CENTER_LEFT);
        Circle emailIcon = new Circle(6);
        emailIcon.getStyleClass().add("user-icon");
        Text emailText = new Text(student.getEmail());
        emailText.getStyleClass().add("card-info");
        emailBox.getChildren().addAll(emailIcon, emailText);

        // Cycle with icon (if you add this field later)
        HBox cycleBox = new HBox(8);
        cycleBox.setAlignment(Pos.CENTER_LEFT);
        Circle cycleIcon = new Circle(6);
        cycleIcon.getStyleClass().add("cycle-icon");
        Text cycleText = new Text("Prvi ciklus");
        cycleText.getStyleClass().add("card-info");
        cycleBox.getChildren().addAll(cycleIcon, cycleText);

        // Program with icon (if you add this field later)
        HBox programBox = new HBox(8);
        programBox.setAlignment(Pos.CENTER_LEFT);
        Circle programIcon = new Circle(6);
        programIcon.getStyleClass().add("doc-icon");
        Text programText = new Text("Softversko inženjerstvo");
        programText.getStyleClass().add("card-info");
        programBox.getChildren().addAll(programIcon, programText);

        detailsRow.getChildren().addAll(emailBox, cycleBox, programBox);
        info.getChildren().addAll(name, detailsRow);

        // Edit button
        Button edit = new Button("✎");
        edit.getStyleClass().add("edit-button-icon");
        edit.setOnAction(e -> openEditStudentPage(student));

        card.getChildren().addAll(avatar, info, edit);
        return card;
    }

    /* =========================================================
       ================= NAVIGACIJA =============================
       ========================================================= */

    private void openAddStudentPage() {
        SceneManager.show("/app/addStudent.fxml", "Dodaj studenta");
    }

    private void openEditStudentPage(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/editStudent.fxml"));
            SceneManager.show(loader, "Uredi studenta");

            EditStudentController controller = loader.getController();
            controller.setStudent(student);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        Platform.runLater(() ->
                new Alert(Alert.AlertType.ERROR, msg).showAndWait()
        );
    }
}
