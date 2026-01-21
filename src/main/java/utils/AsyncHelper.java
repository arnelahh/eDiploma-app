package utils;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;


public class AsyncHelper {

    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(0);

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true); // Daemon thread ne sprječava gasenje aplikacije
        thread.setName("AsyncHelper-" + THREAD_COUNTER.incrementAndGet());
        return thread;
    });


    public static <T> void executeAsync(
            Callable<T> task,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError
    ) {
        Task<T> javaFXTask = new Task<>() {
            @Override
            protected T call() throws Exception {
                return task.call();
            }
        };

        javaFXTask.setOnSucceeded(event -> {
            if (onSuccess != null) {
                onSuccess.accept(javaFXTask.getValue());
            }
        });

        javaFXTask.setOnFailed(event -> {
            if (onError != null) {
                onError.accept(javaFXTask.getException());
            }
        });

        EXECUTOR.submit(javaFXTask);
    }

    public static <T> void executeAsync(
            Callable<T> task,
            Consumer<T> onSuccess
    ) {
        executeAsync(task, onSuccess, throwable -> {
            System.err.println("Error in async task: " + throwable.getMessage());
            throwable.printStackTrace();
        });
    }

    public static <T> void executeAsyncWithLoader(
            Callable<T> task,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError,
            ProgressIndicator loader
    ) {
        Task<T> javaFXTask = new Task<>() {
            @Override
            protected T call() throws Exception {
                return task.call();
            }
        };

        if (loader != null) {
            Platform.runLater(() -> loader.visibleProperty().bind(javaFXTask.runningProperty()));
        }

        javaFXTask.setOnSucceeded(event -> {
            if (onSuccess != null) {
                onSuccess.accept(javaFXTask.getValue());
            }
        });

        javaFXTask.setOnFailed(event -> {
            if (onError != null) {
                onError.accept(javaFXTask.getException());
            }
        });

        EXECUTOR.submit(javaFXTask);
    }


    public static void executeAsyncVoid(
            Runnable task,
            Runnable onSuccess,
            Consumer<Throwable> onError
    ) {
        Task<Void> javaFXTask = new Task<>() {
            @Override
            protected Void call() {
                task.run();
                return null;
            }
        };

        javaFXTask.setOnSucceeded(event -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        });

        javaFXTask.setOnFailed(event -> {
            if (onError != null) {
                onError.accept(javaFXTask.getException());
            }
        });

        EXECUTOR.submit(javaFXTask);
    }

    public static <T> void executeAsyncWithDisable(
            Callable<T> task,
            Consumer<T> onSuccess,
            Consumer<Throwable> onError,
            javafx.scene.Node... disableables
    ) {
        // Disable svih elemenata prije početka (mora biti u JavaFX thread-u)
        Platform.runLater(() -> {
            if (disableables != null) {
                for (javafx.scene.Node node : disableables) {
                    if (node != null) {
                        node.setDisable(true);
                    }
                }
            }
        });

        Task<T> javaFXTask = new Task<>() {
            @Override
            protected T call() throws Exception {
                return task.call();
            }
        };

        javaFXTask.setOnSucceeded(event -> {
            // Re-enable svih elemenata nakon uspjeha
            if (disableables != null) {
                for (javafx.scene.Node node : disableables) {
                    if (node != null) {
                        node.setDisable(false);
                    }
                }
            }
            if (onSuccess != null) {
                onSuccess.accept(javaFXTask.getValue());
            }
        });

        javaFXTask.setOnFailed(event -> {
            // Re-enable svih elemenata nakon greške
            if (disableables != null) {
                for (javafx.scene.Node node : disableables) {
                    if (node != null) {
                        node.setDisable(false);
                    }
                }
            }
            if (onError != null) {
                onError.accept(javaFXTask.getException());
            }
        });

        EXECUTOR.submit(javaFXTask);
    }
    public static void shutdown() {
        EXECUTOR.shutdown();
    }
}
