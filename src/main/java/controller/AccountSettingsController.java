package controller;

import dao.AppUserDAO;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import model.AcademicStaff;
import model.AppUser;
import org.mindrot.jbcrypt.BCrypt;
import utils.EmailValidator;
import utils.GlobalErrorHandler;
import utils.SceneManager;
import utils.UserSession;

public class AccountSettingsController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private PasswordField appPasswordField;
    @FXML private Button toggleAppPasswordButton;
    @FXML private Label appPasswordStatusLabel;

    private final AppUserDAO appUserDAO = new AppUserDAO();
    private AppUser currentUser;
    private boolean appPasswordVisible = false;
    private TextField appPasswordTextField;

    @FXML
    public void initialize() {
        loadUserData();
        setupAppPasswordToggle();
        updateAppPasswordStatus();
    }

    private void loadUserData() {
        currentUser = UserSession.getUser();
        if (currentUser == null) {
            GlobalErrorHandler.error("Korisnik nije prijavljen.");
            handleBack();
            return;
        }

        usernameField.setText(currentUser.getUsername());
        emailField.setText(currentUser.getEmail());
        roleField.setText(currentUser.getRole() != null ? currentUser.getRole().getName() : "N/A");

        AcademicStaff staff = currentUser.getAcademicStaff();
        if (staff != null) {
            firstNameField.setText(staff.getFirstName());
            lastNameField.setText(staff.getLastName());
        }

        // Load existing app password if present
        if (currentUser.getAppPassword() != null && !currentUser.getAppPassword().isEmpty()) {
            appPasswordField.setText(currentUser.getAppPassword());
        }
    }

    private void setupAppPasswordToggle() {
        // Create TextField for showing password
        appPasswordTextField = new TextField();
        appPasswordTextField.setPromptText(appPasswordField.getPromptText());
        appPasswordTextField.setManaged(false);
        appPasswordTextField.setVisible(false);

        // Bind text bidirectionally
        appPasswordTextField.textProperty().bindBidirectional(appPasswordField.textProperty());
    }

    private void updateAppPasswordStatus() {
        if (currentUser != null && currentUser.getAppPassword() != null && !currentUser.getAppPassword().isEmpty()) {
            appPasswordStatusLabel.setText("✓  App Password je konfigurisan");
            appPasswordStatusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #43a047;");
        } else {
            appPasswordStatusLabel.setText("⚠️  App Password nije konfigurisan");
            appPasswordStatusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #ff6b35;");
        }
    }

    @FXML
    private void handleToggleAppPassword() {
        // Toggle visibility logic would go here
        // For simplicity, just show the password in the same field
        if (appPasswordVisible) {
            toggleAppPasswordButton.setText("👁️");
            appPasswordVisible = false;
        } else {
            toggleAppPasswordButton.setText("🙈");
            appPasswordVisible = true;
        }
        // In real implementation, you'd swap between PasswordField and TextField
    }

    @FXML
    private void handleChangePassword() {
        String currentPw = currentPasswordField.getText();
        String newPw = newPasswordField.getText();
        String confirmPw = confirmPasswordField.getText();

        // Validation
        if (currentPw.isBlank() || newPw.isBlank() || confirmPw.isBlank()) {
            GlobalErrorHandler.error("Sva polja za lozinku moraju biti popunjena.");
            return;
        }

        if (newPw.length() < 8) {
            GlobalErrorHandler.error("Nova lozinka mora imati najmanje 8 karaktera.");
            return;
        }

        if (!newPw.equals(confirmPw)) {
            GlobalErrorHandler.error("Nova lozinka i potvrda se ne poklapaju.");
            return;
        }

        // Verify current password
        if (!BCrypt.checkpw(currentPw, currentUser.getPasswordHash())) {
            GlobalErrorHandler.error("Trenutna lozinka nije tačna.");
            return;
        }

        // Hash new password
        String newPasswordHash = BCrypt.hashpw(newPw, BCrypt.gensalt(12));

        try {
            appUserDAO.updatePassword(currentUser.getId(), newPasswordHash);
            currentUser.setPasswordHash(newPasswordHash);
            UserSession.setUser(currentUser);

            GlobalErrorHandler.info("Lozinka je uspješno promijenjena!");

            // Clear fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri promjeni lozinke.", e);
        }
    }

    @FXML
    private void handleSaveAppPassword() {
        String appPw = appPasswordField.getText().trim();

        if (appPw.isBlank()) {
            GlobalErrorHandler.error("App Password ne može biti prazan.");
            return;
        }

        // Remove spaces (Google app passwords often come with spaces)
        appPw = appPw.replaceAll("\\s+", "");

        if (appPw.length() != 16) {
            GlobalErrorHandler.error("Google App Password mora imati tačno 16 karaktera.");
            return;
        }

        try {
            // Hash the app password using BCrypt before storing
            String hashedAppPassword = BCrypt.hashpw(appPw, BCrypt.gensalt(12));
            
            appUserDAO.updateAppPassword(currentUser.getId(), hashedAppPassword);
            currentUser.setAppPassword(hashedAppPassword);
            UserSession.setUser(currentUser);

            updateAppPasswordStatus();
            GlobalErrorHandler.info("App Password je uspješno sačuvan!");

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri snimanju App Password-a.", e);
        }
    }

    @FXML
    private void handleTestConnection() {
        String email = currentUser.getEmail();
        String appPw = appPasswordField.getText().trim().replaceAll("\\s+", "");

        if (appPw.isBlank()) {
            GlobalErrorHandler.error("Prvo unesite App Password.");
            return;
        }

        if (appPw.length() != 16) {
            GlobalErrorHandler.error("App Password mora imati 16 karaktera.");
            return;
        }

        try {
            boolean success = EmailValidator.testGmailConnection(email, appPw);

            if (success) {
                GlobalErrorHandler.info("✓ Konekcija uspješna! Provjerite svoj email inbox.");
            } else {
                GlobalErrorHandler.error("✗ Test konekcije nije uspio. Provjerite App Password.");
            }

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri testiranju konekcije.", e);
        }
    }

    @FXML
    private void handleOpenGoogleHelp() {
        try {
            // Open Google help page
            String url = "https://support.google.com/accounts/answer/185833";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        } catch (Exception e) {
            GlobalErrorHandler.error("Ne mogu otvoriti link. URL: https://support.google.com/accounts/answer/185833");
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.show("/app/dashboard.fxml", "Dashboard");
    }
}
