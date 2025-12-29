package controller;

import dao.AppUserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import model.AppUser;
import org.mindrot.jbcrypt.BCrypt;
import utils.*;

public class LoginController {
    @FXML private Pane rootPane;
    @FXML private TextField emailField;
    @FXML private TextField passwordField;
    @FXML private ProgressIndicator loader;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordButton;

    public AppUserDAO userDao = new AppUserDAO();

    @FXML
    public void initialize() {
        // Povezivanje polja
        passwordField.textProperty().bindBidirectional(passwordTextField.textProperty());
        passwordTextField.setVisible(false);
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validacija praznih polja
        if (email.isEmpty() || password.isEmpty()) {
            GlobalErrorHandler.error("Molimo unesite email i lozinku");
            return;
        }

        loader.setVisible(true);

        // Korištenje AsyncHelper sa disable funkcionalnošću
        AsyncHelper.executeAsyncWithDisable(
            () -> userDao.findByEmail(email),
            user -> handleLoginSuccess(user, password),
            error -> {
                loader.setVisible(false);
                GlobalErrorHandler.error("Greška prilikom prijave: " + error.getMessage());
            },
            emailField, passwordField
        );
    }

    private void handleLoginSuccess(AppUser user, String password) {
        loader.setVisible(false);

        // Security improvement: Generic error message
        if (user == null || !BCrypt.checkpw(password, user.getPasswordHash())) {
            GlobalErrorHandler.error("Netačan email ili lozinka");
            return;
        }

        if (!user.isActive()) {
            GlobalErrorHandler.error("Korisnički nalog nije aktivan");
            return;
        }

        // Uspješna prijava
        UserSession.setUser(user);
        NavigationContext.setCurrentUser(user);
        SceneManager.show("/app/dashboard.fxml", "eDiploma");

        // Pokretanje session managera
        SessionManager.startSession(() -> {
            System.out.println("Session expired!");
            UserSession.clear();
            SceneManager.show("/app/login.fxml", "eDiploma");
        });

        // Resetovanje timera na aktivnost
        if (rootPane != null) {
            rootPane.setOnMouseMoved(ev -> SessionManager.resetTimer());
            rootPane.setOnKeyPressed(ev -> SessionManager.resetTimer());
        }
    }

    @FXML
    private void togglePasswordVisibility() {
        boolean visible = passwordTextField.isVisible();
        passwordTextField.setVisible(!visible);
        passwordField.setVisible(visible);
    }
}
