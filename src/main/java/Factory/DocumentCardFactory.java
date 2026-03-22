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

        Button btnDownload = new Button();
        btnDownload.setGraphic(createIcon("M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"));
        btnDownload.getStyleClass().add("document-icon-btn");

        Button btnEdit = new Button();
        btnEdit.setGraphic(createIcon("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"));
        btnEdit.getStyleClass().add("document-icon-btn");

        Button btnSendEmail = new Button();
        btnSendEmail.setGraphic(createIcon("M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"));
        btnSendEmail.getStyleClass().add("document-icon-btn");


        SVGPath mailIcon = new SVGPath();
        mailIcon.setContent("M2 4h20v16H2V4zm10 9L4 6h16l-8 7z");
        mailIcon.setScaleX(0.65);
        mailIcon.setScaleY(0.65);
        mailIcon.setStyle("-fx-fill: #2c3e50;");


        boolean notStarted = (doc == null);
        boolean ready = (!notStarted && doc.getStatus() == DocumentStatus.READY);
        boolean inProgress = (!notStarted && doc.getStatus() == DocumentStatus.IN_PROGRESS);

        if (ready) card.getStyleClass().add("document-green");
        else if (inProgress) card.getStyleClass().add("document-yellow");

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

        if (!emailDisabledForType) {
            btnSendEmail.setDisable(notStarted || !ready || blockedByPrevious);
            btnSendEmail.setOnAction(e -> {
                if (!btnSendEmail.isDisable() && actions != null && actions.onSendEmail != null) {
                    actions.onSendEmail.accept(doc);
                }
            });
            card.getChildren().addAll(left, btnSendEmail, btnDownload, btnEdit);
        } else {
            card.getChildren().addAll(left, btnDownload, btnEdit);
        }

        return card;
    }

    private SVGPath createIcon(String pathData) {
        SVGPath icon = new SVGPath();
        icon.setContent(pathData);
        icon.setScaleX(0.75);
        icon.setScaleY(0.75);
        return icon;
    }
}
