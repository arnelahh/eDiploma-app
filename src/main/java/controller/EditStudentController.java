package controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import model.Student;
import utils.DashboardView;
import utils.NavigationContext;
import utils.SceneManager;

public class EditStudentController {
    private Student student;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    public void setStudent(Student student) {
        this.student = student;
        populateFields();
    }

    private void populateFields() {
        if (student == null) return;

        firstNameField.setText(student.getFirstName());
        lastNameField.setText(student.getLastName());
        // dodaj ostala polja po potrebi
    }

    @FXML
    private void handleBack() {
        NavigationContext.setTargetView(DashboardView.STUDENTS);
        SceneManager.show("/app/dashboard.fxml", "Dashboard");
    }
}
