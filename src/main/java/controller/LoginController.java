package controller;

import dao.AppUserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
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
    @FXML private SVGPath eyeSvg;

    // SVG putanja za obično oko (kad su zvjezdice)
    private final String EYE_OPEN = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z";

    // SVG putanja za prekriženo oko (kad se tekst vidi)
    private final String EYE_CLOSED = "M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z";

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
        
        // Rutiranje na osnovu role korisnika
        String roleName = user.getRole().getName();
        if ("SECRETARY".equalsIgnoreCase(roleName)) {
            // Sekretar ide na secretary-dashboard
            SceneManager.show("/app/secretary-dashboard.fxml", "eDiploma - Sekretar");
        } else {
            // Ostali korisnici (ADMINISTRATOR itd.) idu na glavni dashboard
            SceneManager.show("/app/dashboard.fxml", "eDiploma");
        }

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
        if (passwordField.isVisible()) {
            // Prelazak u "vidljivi" tekst
            passwordField.setVisible(false);
            passwordTextField.setVisible(true);
            eyeSvg.setContent(EYE_CLOSED); // Postavi prekriženo oko
        } else {
            // Vraćanje na zvjezdice
            passwordTextField.setVisible(false);
            passwordField.setVisible(true);
            eyeSvg.setContent(EYE_OPEN); // Postavi obično oko
        }
    }
}
