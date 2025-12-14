package controller;

import dao.StudentDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Student;

import java.util.List;

public class StudentsController {
    @FXML
    private VBox studentsContainer;

    private final StudentDAO studentDAO = new StudentDAO();

    @FXML
    public void initialize(){
        List<Student> students =studentDAO.getAllStudents();
        for(Student s : students){
            studentsContainer.getChildren().add(createStudentCard(s));
        }
    }

    private HBox createStudentCard(Student student){
        HBox card = new HBox(20);
        card.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 8;");
        card.setPrefHeight(80);

        VBox info = new VBox(5);
        Label name = new Label(student.getFirstName() + " " + student.getLastName());
        Label email = new Label(student.getEmail());
        info.getChildren().addAll(name, email);

        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> openEditStudentPage(student));

        card.getChildren().addAll(info, editButton);

        return card;
    }

    private void openEditStudentPage(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/app/editStudent.fxml")
            );

            Parent root = loader.load();

            EditStudentController controller = loader.getController();
            controller.setStudent(student);

            Scene scene = new Scene(root);

            Stage stage = (Stage) studentsContainer.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Edit Student");
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
