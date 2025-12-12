package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.LoginController;
import dao.AppUserDAO;
import model.AppUser;
import org.mindrot.jbcrypt.BCrypt;

public class LoginTestApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML from resources
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.setTitle("eDiploma");
        //za full screen kad pokrene kod
        stage.setMaximized(true);
        stage.show();

        // Inject real DAO for dynamic login
        LoginController controller = loader.getController();
        AppUserDAO realDao = new AppUserDAO(); // connects to your DB
        controller.userDao = realDao;
    }

    public static void main(String[] args) {
        launch();
    }
}
