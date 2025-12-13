package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage primaryStage;

    private SceneManager() {}

    // poziva se JEDNOM na startu aplikacije
    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setMaximized(true);
    }

    public static void show(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    SceneManager.class.getResource(fxmlPath)
            );
            Parent root = loader.load();

            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle(title);
            primaryStage.setMaximized(true); // centralno
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
