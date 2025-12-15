package utils;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class SceneManager {
    private static Stage primaryStage;

    private SceneManager() {}

    // poziva se JEDNOM na startu aplikacije
    public static void init(Stage stage) {
        primaryStage = stage;

        // Dobijamo rezoluciju korisnika
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        primaryStage.setX(screenBounds.getMinX());
        primaryStage.setY(screenBounds.getMinY());
        primaryStage.setWidth(screenBounds.getWidth());
        primaryStage.setHeight(screenBounds.getHeight());

        primaryStage.show();
    }
    public static void show(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            if (!primaryStage.isMaximized()) {
                primaryStage.setMaximized(true);
            }
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void show(FXMLLoader loader, String title) {
        try {
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
