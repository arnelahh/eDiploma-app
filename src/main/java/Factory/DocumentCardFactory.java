package Factory;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
import model.Document;
import model.DocumentStatus;
import model.DocumentType;

import java.util.Set;
import java.util.function.Consumer;

public class DocumentCardFactory {

    public static class Actions {
        public Consumer<Document> onDownload;
        public Consumer<Document> onView;
        public Consumer<DocumentType> onEdit;
        public Consumer<Document> onSendEmail; // NOVO: akcija za slanje emaila
    }

    private static final Set<String> EMAIL_DISABLED_DOC_NAMES = Set.of(
            "Zapisnik sa odbrane",
            "Zapisnik o pismenom dijelu diplomskog rada"
    );

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

        // NOVO: Dugme za slanje emaila (SVG ikonica umjesto emoji)
        Button btnSendEmail = new Button();
        btnSendEmail.getStyleClass().add("document-icon-btn");
        btnSendEmail.setText("");

        SVGPath mailIcon = new SVGPath();
        // Simple envelope icon (24x24 coordinate space)
        mailIcon.setContent("M2 4h20v16H2V4zm10 9L4 6h16l-8 7z");
        mailIcon.setScaleX(0.65);
        mailIcon.setScaleY(0.65);
        mailIcon.setStyle("-fx-fill: #2c3e50;");
        btnSendEmail.setGraphic(mailIcon);

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

        boolean emailDisabledForType = (type != null
                && type.getName() != null
                && EMAIL_DISABLED_DOC_NAMES.contains(type.getName()));

        // SEND EMAIL: prikazati samo ako je omogućeno za ovaj tip dokumenta
        if (!emailDisabledForType) {
            btnSendEmail.setDisable(notStarted || !ready || blockedByPrevious);
            btnSendEmail.setOnAction(e -> {
                if (!btnSendEmail.isDisable() && actions != null && actions.onSendEmail != null) {
                    actions.onSendEmail.accept(doc);
                }
            });
            // Dodaj dugmad - redoslijed: Send Email, Download, Edit
            card.getChildren().addAll(left, btnSendEmail, btnDownload, btnEdit);
        } else {
            // Dodaj dugmad - redoslijed: Download, Edit
            card.getChildren().addAll(left, btnDownload, btnEdit);
        }

        return card;
    }
}
