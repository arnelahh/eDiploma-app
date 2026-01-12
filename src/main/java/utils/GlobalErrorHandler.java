package utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

public final class GlobalErrorHandler {
    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Greška", message);
    }
    public static void error(String message, Throwable ex) {
        if(ex != null) ex.printStackTrace();

        String finalMessage = message;
        if(finalMessage == null ||finalMessage.isBlank()) {
            finalMessage = defaultMessage(ex);
        }
        show(Alert.AlertType.ERROR, "Greška", finalMessage);
    }

    public static void warning(String message) {
        show(Alert.AlertType.WARNING, "Upozorenje", message);
    }

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "Informacija", message);
    }

    public static void taskFailed(javafx.concurrent.Task<?> task, String userMessage) {
        Throwable ex = (task != null) ? task.getException() : null;
        error(userMessage, ex);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        final String msg = Objects.toString(message, "Unknown error");

        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }
    private static String defaultMessage(Throwable ex) {
        if (ex == null) return "Došlo je do neočekivane greške.";

        String m = ex.getMessage();
        if (m != null && !m.isBlank()) return m;

        return "Došlo je do greške: " + ex.getClass().getSimpleName();
    }

    @SuppressWarnings("unused")
    private static String stackTrace(Throwable ex) {
        if (ex == null) return "";
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
