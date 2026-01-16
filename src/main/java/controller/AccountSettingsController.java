package controller;

import dao.AppUserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.AcademicStaff;
import model.AppUser;
import org.mindrot.jbcrypt.BCrypt;
import email.EmailService;
import utils.AESEncryption;
import utils.GlobalErrorHandler;
import utils.SceneManager;
import utils.UserSession;

public class AccountSettingsController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;

    @FXML private TextField newEmailField;
    @FXML private TextField confirmEmailField;
    @FXML private PasswordField emailPasswordField;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private PasswordField appPasswordField;
    @FXML private Label appPasswordStatusLabel;

    private final AppUserDAO appUserDAO = new AppUserDAO();
    private final EmailService emailService = new EmailService();
    private AppUser currentUser;

    @FXML
    public void initialize() {
        loadUserData();
        updateAppPasswordStatus();
    }

    private void loadUserData() {
        currentUser = UserSession.getUser();
        if (currentUser == null) {
            GlobalErrorHandler.error("Korisnik nije prijavljen.");
            handleRadoviClick();
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

        if (currentUser.getAppPassword() != null && !currentUser.getAppPassword().isEmpty()) {
            appPasswordField.setText("****************");
        }
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
    private void handleChangeEmail() {
        String newEmail = newEmailField.getText().trim();
        String confirmEmail = confirmEmailField.getText().trim();
        String password = emailPasswordField.getText();

        // Validacija
        if (newEmail.isBlank() || confirmEmail.isBlank() || password.isBlank()) {
            GlobalErrorHandler.error("Sva polja moraju biti popunjena.");
            return;
        }

        if (!newEmail.equals(confirmEmail)) {
            GlobalErrorHandler.error("Email adrese se ne poklapaju.");
            return;
        }

        // Validacija email formata
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            GlobalErrorHandler.error("Unesite validan email format.");
            return;
        }

        // Provjera lozinke
        if (!BCrypt.checkpw(password, currentUser.getPasswordHash())) {
            GlobalErrorHandler.error("Lozinka nije tačna.");
            return;
        }

        try {
            // Provjera da li je email već zauzet
            if (appUserDAO.isEmailTaken(newEmail, currentUser.getId())) {
                GlobalErrorHandler.error("Email adresa je već zauzeta.");
                return;
            }

            // Update email
            appUserDAO.updateEmail(currentUser.getId(), newEmail);
            currentUser.setEmail(newEmail);
            UserSession.setUser(currentUser);

            // Refresh UI
            emailField.setText(newEmail);
            newEmailField.clear();
            confirmEmailField.clear();
            emailPasswordField.clear();

            GlobalErrorHandler.info("Email adresa je uspješno promijenjena!");

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri promjeni email-a.", e);
        }
    }

    @FXML
    private void handleChangePassword() {
        String currentPw = currentPasswordField.getText();
        String newPw = newPasswordField.getText();
        String confirmPw = confirmPasswordField.getText();

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

        if (!BCrypt.checkpw(currentPw, currentUser.getPasswordHash())) {
            GlobalErrorHandler.error("Trenutna lozinka nije tačna.");
            return;
        }

        String newPasswordHash = BCrypt.hashpw(newPw, BCrypt.gensalt(12));

        try {
            appUserDAO.updatePassword(currentUser.getId(), newPasswordHash);
            currentUser.setPasswordHash(newPasswordHash);
            UserSession.setUser(currentUser);

            GlobalErrorHandler.info("Lozinka je uspješno promijenjena!");

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

        appPw = appPw.replaceAll("\\s+", "");

        if (appPw.length() != 16) {
            GlobalErrorHandler.error("Google App Password mora imati tačno 16 karaktera.");
            return;
        }

        try {
            // Koristi AES enkripciju umjesto BCrypt
            String encryptedAppPassword = AESEncryption.encrypt(appPw);
            
            appUserDAO.updateAppPassword(currentUser.getId(), encryptedAppPassword);
            currentUser.setAppPassword(encryptedAppPassword);
            UserSession.setUser(currentUser);

            updateAppPasswordStatus();
            GlobalErrorHandler.info("App Password je uspješno sačuvan!");

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri snimanju App Password-a.", e);
        }
    }

    @FXML
    private void handleTestConnection() {
        // Test email sa EmailService
        boolean success = emailService.sendTestEmail();
        
        if (success) {
            GlobalErrorHandler.info("✓ Konekcija uspješna! Test email je poslan. Provjerite inbox.");
        } else {
            GlobalErrorHandler.error("✗ Test konekcije nije uspio. Provjerite App Password.");
        }
    }

    @FXML
    private void handleOpenGoogleHelp() {
        String url = "https://support.google.com/accounts/answer/185833";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Google App Password uputstvo");
        alert.setHeaderText("Kopiraj ovaj link i otvori ga u browseru:");
        alert.setContentText(url);
        alert.showAndWait();
    }
    
    private void showUrlDialog(String url) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Google App Password Uputstvo");
        alert.setHeaderText("Kopirajte link i otvorite u browseru:");
        alert.setContentText(url);
        
        // Make the text selectable
        TextArea textArea = new TextArea(url);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        
        alert.getDialogPane().setExpandableContent(textArea);
        alert.getDialogPane().setExpanded(true);
        alert.showAndWait();
    }

    @FXML
    private void handleRadoviClick() {
        SceneManager.show("/app/secretary-dashboard.fxml", "eDiploma");
    }

}
