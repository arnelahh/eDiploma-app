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
        public Consumer<Document> onSendEmail; // NOVO: akcija za slanje emaila
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

        // NOVO: Dugme za slanje emaila
        Button btnSendEmail = new Button("📧"); // Email emoji
        btnSendEmail.getStyleClass().add("document-icon-btn");

        boolean notStarted = (doc == null);
        boolean ready = (!notStarted && doc.getStatus() == DocumentStatus.READY);
        boolean inProgress = (!notStarted && doc.getStatus() == DocumentStatus.IN_PROGRESS);

        if (ready) card.getStyleClass().add("document-green");
        else if (inProgress) card.getStyleClass().add("document-yellow");
        // else default (bijelo)

        // EDIT: blokiran po redosljedu
        btnEdit.setDisable(blockedByPrevious);
        btnEdit.setOnAction(e -> {
            if (!blockedByPrevious && actions != null && actions.onEdit != null) {
                actions.onEdit.accept(type);
            }
        });

        // DOWNLOAD: omogućen samo za READY dokumente
        btnDownload.setDisable(notStarted || !ready || blockedByPrevious);
        btnDownload.setOnAction(e -> {
            if (!btnDownload.isDisable() && actions != null && actions.onDownload != null) {
                actions.onDownload.accept(doc);
            }
        });

        // SEND EMAIL: omogućen samo za READY dokumente
        btnSendEmail.setDisable(notStarted || !ready || blockedByPrevious);
        btnSendEmail.setOnAction(e -> {
            if (!btnSendEmail.isDisable() && actions != null && actions.onSendEmail != null) {
                actions.onSendEmail.accept(doc);
            }
        });

        // Dodaj dugmad - redoslijed: Send Email, Download, Edit
        card.getChildren().addAll(left, btnSendEmail, btnDownload, btnEdit);
        return card;
    }
}
