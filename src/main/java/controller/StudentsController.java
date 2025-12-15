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
import model.Student;
import utils.SceneManager;

import java.util.List;

public class StudentsController {

    @FXML
    private VBox studentsContainer;

    @FXML
    private TextField searchField;

    @FXML
    private ProgressIndicator loader;

    private final StudentDAO studentDAO = new StudentDAO();

    @FXML
    public void initialize() {
        loadStudentsAsync();
        setupSearch();
    }

    /* =========================================================
       ============ ASYNC LOAD STUDENATA =======================
       ========================================================= */
    private void loadStudentsAsync() {

        studentsContainer.getChildren().clear();
        studentsContainer.getChildren().addAll(
                createHeader(),
                createSearchBar()
        );

        Task<List<Student>> task = new Task<>() {
            @Override
            protected List<Student> call() {
                return studentDAO.getAllStudents();
            }
        };

        loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            for (Student s : task.getValue()) {
                studentsContainer.getChildren().add(createStudentCard(s));
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

        if (studentsContainer.getChildren().size() > 2) {
            studentsContainer.getChildren().remove(2, studentsContainer.getChildren().size());
        }

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
                studentsContainer.getChildren().add(createStudentCard(s));
            }
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showError("Greška pri pretrazi");
        });

        new Thread(task).start();
    }

    /* =========================================================
       ================= UI KOMPONENTE =========================
       ========================================================= */

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 10, 30));

        VBox titleBox = new VBox(5);

        Label title = new Label("Studenti");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: black;");

        Label subtitle = new Label("Kompletan pregled i upravljanje studentima");
        subtitle.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+ Dodaj novog studenta");
        addButton.setStyle("""
                -fx-background-color: #4f5dff;
                -fx-text-fill: white;
                -fx-font-size: 14;
                -fx-font-weight: bold;
                -fx-background-radius: 8;
                -fx-cursor: hand;
                -fx-padding: 10 20;
                """);
        addButton.setOnAction(e -> openAddStudentPage());

        header.getChildren().addAll(titleBox, spacer, addButton);
        return header;
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setPadding(new Insets(10, 30, 20, 30));

        searchField = new TextField();
        searchField.setPromptText("🔍 Pretraži studente...");
        searchField.setPrefHeight(40);
        searchField.setStyle("""
                -fx-background-radius: 8;
                -fx-border-radius: 8;
                -fx-border-color: #e0e0e0;
                -fx-font-size: 14;
                """);

        HBox.setHgrow(searchField, Priority.ALWAYS);
        searchBar.getChildren().add(searchField);
        return searchBar;
    }

    private HBox createStudentCard(Student student) {

        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 8;
                -fx-border-color: #e0e0e0;
                -fx-border-radius: 8;
                """);
        VBox.setMargin(card, new Insets(5, 30, 5, 30));

        StackPane avatar = new StackPane();
        Circle circle = new Circle(25);
        circle.setStyle("-fx-fill: #e8eaff;");

        Label initials = new Label(
                student.getFirstName().substring(0, 1).toUpperCase() +
                        student.getLastName().substring(0, 1).toUpperCase()
        );
        initials.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #4f5dff;");

        avatar.getChildren().addAll(circle, initials);

        VBox info = new VBox(5);

        Label name = new Label(
                student.getFirstName() + " " +
                        student.getLastName() + " (" +
                        String.format("%03d", student.getIndexNumber()) + ")"
        );
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        info.getChildren().add(name);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button edit = new Button("✏️");
        edit.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
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
