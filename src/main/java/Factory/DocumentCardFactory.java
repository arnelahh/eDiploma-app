package Factory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Document;
import model.DocumentStatus;
import model.DocumentType;

import java.util.function.Consumer;

public class DocumentCardFactory {

    public static class Actions {
        public Consumer<Document> onDownload;
        public Consumer<Document> onView;
        public Consumer<DocumentType> onEdit;
    }

    public HBox create(DocumentType type, Document doc, boolean blockedByPrevious, Actions actions) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(14, 18, 14, 18));
        card.getStyleClass().add("document-card");
        card.setCursor(Cursor.DEFAULT);

        VBox left = new VBox(0);
        left.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(left, Priority.ALWAYS);

        Text title = new Text(type.getName());
        title.getStyleClass().add("document-title");
        left.getChildren().add(title);

        Button btnDownload = new Button("⬇");
        btnDownload.getStyleClass().add("document-icon-btn");

        Button btnEdit = new Button("✏");
        btnEdit.getStyleClass().add("document-icon-btn");

        boolean notStarted = (doc == null);
        boolean ready = (!notStarted && doc.getStatus() == DocumentStatus.READY);
        boolean inProgress = (!notStarted && doc.getStatus() == DocumentStatus.IN_PROGRESS);

        if (ready) card.getStyleClass().add("document-green");
        else if (inProgress) card.getStyleClass().add("document-yellow");
        // else default (bijelo)

        // EDIT: blokiran po redoslijedu
        btnEdit.setDisable(blockedByPrevious);
        btnEdit.setOnAction(e -> {
            if (!blockedByPrevious && actions != null && actions.onEdit != null) {
                actions.onEdit.accept(type);
            }
        });


        btnDownload.setDisable(notStarted || !ready || blockedByPrevious);
        btnDownload.setOnAction(e -> {
            if (!btnDownload.isDisable() && actions != null && actions.onDownload != null) {
                actions.onDownload.accept(doc);
            }
        });

        card.getChildren().addAll(left, btnDownload, btnEdit);
        return card;
    }
}
