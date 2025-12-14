package controller;

import dao.StudentDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import model.Student;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;

import java.util.List;

public class StudentsController {
    @FXML
    private VBox studentsContainer;

    @FXML
    private TextField searchField;

    private final StudentDAO studentDAO = new StudentDAO();

    @FXML
    public void initialize(){
        loadStudents();
    }

    private void loadStudents() {
        studentsContainer.getChildren().clear();

        HBox header = createHeader();
        studentsContainer.getChildren().add(header);


        HBox searchBar = createSearchBar();
        studentsContainer.getChildren().add(searchBar);


        List<Student> students = studentDAO.getAllStudents();
        for(Student s : students){
            studentsContainer.getChildren().add(createStudentCard(s));
        }
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 30, 10, 30));
        header.setSpacing(20);

        VBox titleBox = new VBox(5);
        Label title = new Label("Studenti");
        title.setStyle("-fx-font-size: 24; -fx-font-weight: bold;");

        Label subtitle = new Label("Kompletan pregled i upravljanje studentima");
        subtitle.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");

        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+ Dodaj novog studenta");
        addButton.setStyle("-fx-background-color: #4f5dff; -fx-text-fill: white; " +
                "-fx-font-size: 14; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-padding: 10 20 10 20;");
        addButton.setOnAction(e -> openAddStudentPage());

        header.getChildren().addAll(titleBox, spacer, addButton);
        return header;
    }

    private HBox createSearchBar() {
        HBox searchBar = new HBox();
        searchBar.setPadding(new Insets(10, 30, 20, 30));

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Pretraži studente...");
        searchField.setPrefHeight(40);
        searchField.setMaxWidth(Double.MAX_VALUE);
        searchField.setStyle("-fx-background-radius: 8; -fx-background-color: white; " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 8; " +
                "-fx-font-size: 14;");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            performSearch(newValue);
        });

        searchBar.getChildren().add(searchField);
        return searchBar;
    }

    private void performSearch(String searchTerm) {
        if (studentsContainer.getChildren().size() > 2) {
            studentsContainer.getChildren().remove(2, studentsContainer.getChildren().size());
        }

        List<Student> students;
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            students = studentDAO.getAllStudents();
        } else {
            students = studentDAO.searchStudents(searchTerm);
        }

        for (Student s : students) {
            studentsContainer.getChildren().add(createStudentCard(s));
        }
    }

    private HBox createStudentCard(Student student){
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; " +
                "-fx-background-radius: 8; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 8; -fx-border-width: 1;");
        card.setPrefHeight(80);
        card.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(card, new Insets(5, 30, 5, 30));

        StackPane avatar = new StackPane();
        Circle circle = new Circle(25);
        circle.setStyle("-fx-fill: #e8eaff;");

        String initials = student.getFirstName().substring(0, 1).toUpperCase() +
                student.getLastName().substring(0, 1).toUpperCase() + ".";
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #4f5dff;");

        avatar.getChildren().addAll(circle, initialsLabel);

        VBox info = new VBox(5);

        Label name = new Label(student.getFirstName() + " " + student.getLastName() +
                " (" + String.format("%03d", student.getIndexNumber()) + ")");
        name.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        HBox detailsBox = new HBox(15);

        Label indexLabel = new Label("Indeks: " + student.getIndexNumber());
        indexLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");

        if (student.getStudyProgram() != null && !student.getStudyProgram().isEmpty()) {
            Label programLabel = new Label("● " + student.getStudyProgram());
            programLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
            detailsBox.getChildren().add(programLabel);
        }

        if (student.getStatus() != null) {
            Label statusLabel = new Label("● " + student.getStatus().getName());
            statusLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #666;");
            detailsBox.getChildren().add(statusLabel);
        }

        info.getChildren().addAll(name, detailsBox);
        HBox.setHgrow(info, Priority.ALWAYS);

        Button editButton = new Button("✏️");
        editButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; " +
                "-fx-font-size: 18; -fx-text-fill: #4f5dff;");
        editButton.setOnAction(e -> openEditStudentPage(student));

        card.getChildren().addAll(avatar, info, editButton);

        return card;
    }

    private void openAddStudentPage() {
        try {
            SceneManager.show("/app/addStudent.fxml", "Dodaj novog studenta");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openEditStudentPage(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/editStudent.fxml"));
            loader.load();

            EditStudentController controller = loader.getController();
            controller.setStudent(student);

            SceneManager.show("/app/editStudent.fxml", "Uredi studenta");

            controller.setStudent(student);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
