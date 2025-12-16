package controller;

import Factory.StudentCardFactory;
import dao.StudentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import model.Student;
import utils.SceneManager;

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
    private final StudentCardFactory cardFactory = new StudentCardFactory();

    private final ObservableList<Student> masterList =
            FXCollections.observableArrayList();

    private FilteredList<Student> filteredList;

    @FXML
    public void initialize() {
        setupAddButton();
        setupSearch();
        loadStudentsAsync();
    }

    private void loadStudentsAsync() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                masterList.clear();
                masterList.addAll(studentDAO.getAllStudents());
                return null;
            }
        };

        loader.visibleProperty().bind(task.runningProperty());

        task.setOnSucceeded(e -> {
            filteredList = new FilteredList<>(masterList, s -> true);
            renderStudents(filteredList);
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showError("Greška pri učitavanju studenata");
        });

        new Thread(task, "load-students-thread").start();
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (filteredList == null) return;

            String term = newVal == null ? "" : newVal.toLowerCase().trim();

            filteredList.setPredicate(student -> {
                if (term.isEmpty()) return true;

                return student.getFirstName().toLowerCase().contains(term)
                        || student.getLastName().toLowerCase().contains(term)
                        || String.valueOf(student.getIndexNumber()).contains(term)
                        || (student.getEmail() != null &&
                        student.getEmail().toLowerCase().contains(term));
            });

            renderStudents(filteredList);
        });
    }

    private void renderStudents(Iterable<Student> students) {
        studentsCardsContainer.getChildren().clear();

        for (Student student : students) {
            studentsCardsContainer.getChildren().add(
                    cardFactory.create(student, this::openEditStudentPage)
            );
        }
    }

    private void setupAddButton() {
        if (addStudentButton != null) {
            addStudentButton.setOnAction(e -> openAddStudentPage());
        }
    }

    private void openAddStudentPage() {
        SceneManager.showWithData(
                "/app/studentForm.fxml",
                "eDiploma",
                (StudentFormController controller) -> {
                    controller.initCreate();
                }
        );
    }

    private void openEditStudentPage(Student student) {
        SceneManager.showWithData(
                "/app/studentForm.fxml",
                "eDiploma",
                (StudentFormController controller) -> {
                    controller.initEdit(student);
                }
        );
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}
