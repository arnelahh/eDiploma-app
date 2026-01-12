package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import controller.LoginController;
import dao.AppUserDAO;
import utils.SceneManager;


public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.init(stage);

        SceneManager.show("/app/login.fxml", "eDiploma");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/app/login.fxml"));
        loader.load();
        LoginController controller = loader.getController();
        AppUserDAO realDao = new AppUserDAO();
        controller.userDao = realDao;
    }

    public static void main(String[] args) {
        launch();
    }
}
