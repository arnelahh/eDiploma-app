package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.LoginController;
import dao.AppUserDAO;
import model.AppUser;
import org.mindrot.jbcrypt.BCrypt;
import utils.SceneManager;


public class LoginTestApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Inicijaliziraj SceneManager sa stage-om
        SceneManager.init(stage);

        // Pokaži login scenu preko SceneManager-a
        SceneManager.show("/app/login.fxml", "eDiploma");

        // Inject real DAO za dinamički login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/login.fxml"));
        loader.load(); // potrebno za dohvat kontrolera
        LoginController controller = loader.getController();
        AppUserDAO realDao = new AppUserDAO(); // konekcija na bazu
        controller.userDao = realDao;
    }

    public static void main(String[] args) {
        launch();
    }
}
