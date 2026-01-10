package controller;

import Factory.DocumentCardFactory;
import dao.CommissionDAO;
import dao.DocumentDAO;
import dao.DocumentTypeDAO;
import dao.ThesisDAO;
import dto.ThesisDetailsDTO;
import dto.ThesisLockInfoDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.*;
import service.DocumentEmailNotificationService;
import utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.Base64;

public class ThesisDetailsController {

    @FXML
    private Text titleValue;
    @FXML
    private Text subjectValue;
    @FXML
    private Text statusValue;
    @FXML
    private Text applicationDateValue;
    @FXML
    private Text defenseDateValue;
    @FXML
    private Text gradeValue;
    @FXML
    private Text studentName;
    @FXML
    private Text studentIndex;
    @FXML
    private Text studentEmail;

    @FXML
    private Text mentorTitle;
    @FXML
    private Text mentorName;
    @FXML
    private Text mentorEmail;

    @FXML
    private VBox commissionNotFormedBox;
    @FXML
    private VBox commissionFormedBox;
    @FXML
    private Text chairmanName;
    @FXML
    private Text memberName;
    @FXML
    private Text mentorCommissionName;
    @FXML
    private Text secretaryCommissionName;
    @FXML
    private Text substituteName;

    @FXML
    private ProgressIndicator loader;

    // DOCUMENTS UI CONTAINER
    @FXML
    private VBox documentsContainer;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();

    // DOCUMENTS
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();
    private final DocumentCardFactory cardFactory = new DocumentCardFactory();

    // EMAIL SERVICE
    private final DocumentEmailNotificationService emailNotificationService = new DocumentEmailNotificationService();

    private Map<Integer, DocumentType> typeById = new HashMap<>();

    private int thesisId;
    private ThesisDetailsDTO currentDetails;
    private Commission currentCommission;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public void initWithThesisId(int thesisId) {
        this.thesisId = thesisId;
        loadThesisDetails();
    }

    private void loadThesisDetails() {
        Task<ThesisDetailsDTO> task = new Task<>() {
            @Override
            protected ThesisDetailsDTO call() throws Exception {
                return thesisDAO.getThesisDetails(thesisId);
            }
        };

        if (loader != null) {
            loader.visibleProperty().bind(task.runningProperty());
        }

        task.setOnSucceeded(e -> {
            currentDetails = task.getValue();
            if (currentDetails != null) {
                populateFields();
                loadCommission();
                loadDocumentsUIAsync();
            } else {
                GlobalErrorHandler.error("Završni rad nije pronađen.");
            }
        });

        task.setOnFailed(e -> GlobalErrorHandler.error(
                "Greška pri učitavanju detalja završnog rada.",
                task.getException()
        ));

        new Thread(task, "load-thesis-details").start();
    }

    private void loadCommission() {
        Task<Commission> task = new Task<>() {
            @Override
            protected Commission call() throws Exception {
                return commissionDAO.getCommissionByThesisId(thesisId);
            }
        };

        task.setOnSucceeded(e -> {
            currentCommission = task.getValue();
            updateCommissionDisplay();
        });

        task.setOnFailed(e -> {
            updateCommissionDisplay();
            GlobalErrorHandler.error("Greška pri učitavanju komisije.", task.getException());
        });

        new Thread(task, "load-commission").start();
    }

    private void loadDocumentsUIAsync() {
        Task<Void> task = new Task<>() {
            List<DocumentType> types;
            List<Document> docs;

            @Override
            protected Void call() throws Exception {
                types = documentTypeDAO.getAllOrdered();
                docs = documentDAO.getByThesisId(thesisId);
                return null;
            }

            @Override
            protected void succeeded() {
                buildDocumentsUI(types, docs);
            }

            @Override
            protected void failed() {
                GlobalErrorHandler.error("Greška pri učitavanju dokumenata.", getException());
            }
        };

        new Thread(task, "load-documents").start();
    }

    private void buildDocumentsUI(List<DocumentType> types, List<Document> docs) {
        if (documentsContainer == null) return;

        // cache type map
        typeById = (types == null ? List.<DocumentType>of() : types)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(DocumentType::getId, Function.identity(), (a, b) -> a));

        Map<Integer, Document> docByTypeId = (docs == null ? List.<Document>of() : docs)
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Document::getTypeId, Function.identity(), (a, b) -> a));

        DocumentCardFactory.Actions actions = new DocumentCardFactory.Actions();
        actions.onEdit = this::openEditorForType;
        actions.onDownload = this::downloadDocument;
        actions.onSendEmail = this::handleSendEmailDocument; // NOVO: Send Email akcija

        documentsContainer.getChildren().clear();

        boolean previousAllReady = true;

        for (DocumentType type : (types == null ? List.<DocumentType>of() : types)) {
            Document doc = docByTypeId.get(type.getId());

            boolean blockedByPrevious = !previousAllReady;

            HBox card = cardFactory.create(type, doc, blockedByPrevious, actions);
            documentsContainer.getChildren().add(card);

            if (doc == null || doc.getStatus() != DocumentStatus.READY) {
                previousAllReady = false;
            }
        }
    }

    private void populateFields() {
        titleValue.setText(currentDetails.getTitle() != null ? currentDetails.getTitle().toUpperCase() : "—");
        subjectValue.setText(currentDetails.getSubject() != null ? currentDetails.getSubject().getName() : "—");

        statusValue.setText(currentDetails.getStatus() != null ? currentDetails.getStatus() : "—");

        applicationDateValue.setText(currentDetails.getApplicationDate() != null
                ? currentDetails.getApplicationDate().format(DATE_FORMATTER)
                : "--/--/----");

        defenseDateValue.setText(currentDetails.getDefenseDate() != null
                ? currentDetails.getDefenseDate().format(DATE_FORMATTER)
                : "--/--/----");

        gradeValue.setText(currentDetails.getGrade() != null ? String.valueOf(currentDetails.getGrade()) : "—");

        if (currentDetails.getStudent() != null) {
            studentName.setText(currentDetails.getStudent().getFirstName() + " " + currentDetails.getStudent().getLastName());
            studentIndex.setText(String.format("%03d", currentDetails.getStudent().getIndexNumber()));
            studentEmail.setText(currentDetails.getStudent().getEmail() != null ? currentDetails.getStudent().getEmail() : "—");
        }

        if (currentDetails.getMentor() != null) {
            mentorTitle.setText(currentDetails.getMentor().getTitle() != null ? currentDetails.getMentor().getTitle() : "");
            mentorName.setText(currentDetails.getMentor().getFirstName() + " " + currentDetails.getMentor().getLastName());
            mentorEmail.setText(currentDetails.getMentor().getEmail() != null ? currentDetails.getMentor().getEmail() : "—");
        }
    }

    private void updateCommissionDisplay() {
        if (currentCommission != null && currentCommission.getMember1() != null) {
            commissionNotFormedBox.setVisible(false);
            commissionNotFormedBox.setManaged(false);
            commissionFormedBox.setVisible(true);
            commissionFormedBox.setManaged(true);

            chairmanName.setText(formatMemberName(currentCommission.getMember1()));
            memberName.setText(formatMemberName(currentCommission.getMember2()));
            substituteName.setText(formatMemberName(currentCommission.getMember3()));

            if (currentDetails != null && currentDetails.getMentor() != null) {
                mentorCommissionName.setText(formatMemberName(currentDetails.getMentor()));
            } else {
                mentorCommissionName.setText("—");
            }

            if (currentDetails != null && currentDetails.getSecretary() != null) {
                secretaryCommissionName.setText(formatMemberName(currentDetails.getSecretary()));
            } else {
                secretaryCommissionName.setText("—");
            }
        } else {
            commissionNotFormedBox.setVisible(true);
            commissionNotFormedBox.setManaged(true);
            commissionFormedBox.setVisible(false);
            commissionFormedBox.setManaged(false);
        }
    }

    private String formatMemberName(AcademicStaff member) {
        if (member == null) return "—";
        String title = member.getTitle() != null ? member.getTitle() + " " : "";
        return title + member.getFirstName() + " " + member.getLastName();
    }

    // -------------------------
    // DOCUMENT ACTIONS
    // -------------------------

    private void openEditorForType(DocumentType type) {
        if (type == null) return;

        String name = type.getName() != null ? type.getName() : "";

        switch (name) {
            case "Rješenje o izradi završnog rada" -> handleOpenFinalThesisApproalReport();
            case "Zapisnik o pismenom dijelu diplomskog rada" -> handleOpenWrittenExamReport();
            case "Zapisnik sa odbrane" -> handleOpenDefenseReport();

            case "Rješenje o formiranju Komisije" -> handleOpenCommissionReport();
            case "Obavijest" -> handleOpenObavijest();
            case "Uvjerenje o završenom ciklusu" -> handleOpenCycleCompletion();

            default -> GlobalErrorHandler.info("Nepoznat tip dokumenta: " + name);
        }
    }

    private void downloadDocument(Document doc) {
        if (doc == null) return;

        if (doc.getStatus() != DocumentStatus.READY) {
            GlobalErrorHandler.error("Dokument nije READY. Prvo završite unos i sačuvajte.");
            return;
        }

        try {
            // pošto getByThesisId ne vraća content, uzmi ga ovdje
            String base64 = documentDAO.getContentBase64(doc.getId());
            if (base64 == null || base64.isBlank()) {
                GlobalErrorHandler.error("Dokument nema sačuvan sadržaj (PDF).");
                return;
            }

            byte[] pdfBytes = Base64.getDecoder().decode(base64);

            FileChooser fc = new FileChooser();
            fc.setTitle("Sačuvaj dokument");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

            DocumentType type = typeById.get(doc.getTypeId());
            String fileName = (type != null && type.getName() != null)
                    ? type.getName().replaceAll("[^a-zA-Z0-9čćđšžČĆĐŠŽ _-]", "").replace(" ", "_") + ".pdf"
                    : "Dokument.pdf";

            fc.setInitialFileName(fileName);

            File file = fc.showSaveDialog(documentsContainer.getScene().getWindow());
            if (file == null) return;

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfBytes);
            }

            GlobalErrorHandler.info("Dokument je sačuvan.");

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri preuzimanju dokumenta.", e);
        }
    }

    private String formatNameWithEmail(String fullName, String email) {
        if (fullName == null || fullName.isBlank()) fullName = "—";
        if (email == null || email.isBlank()) return fullName;
        return fullName + " (" + email + ")";
    }

    private String formatStudentLine(Student s) {
        if (s == null) return "Student: —";
        String fullName = s.getFirstName() + " " + s.getLastName();
        return "Student: " + formatNameWithEmail(fullName, s.getEmail());
    }

    private String formatStaffLine(String roleLabel, AcademicStaff staff) {
        if (staff == null) return roleLabel + ": —";
        String title = (staff.getTitle() != null && !staff.getTitle().isBlank()) ? staff.getTitle() + " " : "";
        String fullName = title + staff.getFirstName() + " " + staff.getLastName();
        return roleLabel + ": " + formatNameWithEmail(fullName, staff.getEmail());
    }

    private List<String> getRecipientPreviewLines(String documentTypeName) {
        List<String> lines = new ArrayList<>();

        Student student = (currentDetails != null) ? currentDetails.getStudent() : null;
        AcademicStaff mentor = (currentDetails != null) ? currentDetails.getMentor() : null;
        AcademicStaff secretary = (currentDetails != null) ? currentDetails.getSecretary() : null;

        switch (documentTypeName) {
            case "Rješenje o izradi rada", "Rješenje o izradi završnog rada" -> {
                lines.add(formatStudentLine(student));
                lines.add(formatStaffLine("Mentor", mentor));
                lines.add(formatStaffLine("Sekretar", secretary));
            }
            case "Rješenje o formiranju Komisije" -> {
                lines.add(formatStudentLine(student));
                lines.add(formatStaffLine("Mentor", mentor));
                lines.add(formatStaffLine("Sekretar", secretary));

                AcademicStaff chairman = (currentCommission != null) ? currentCommission.getMember1() : null;
                AcademicStaff member = (currentCommission != null) ? currentCommission.getMember2() : null;

                lines.add(formatStaffLine("Predsjednik komisije", chairman));
                lines.add(formatStaffLine("Član komisije", member));
            }
            case "Obavijest" -> {
                lines.add(formatStudentLine(student));
                lines.add(formatStaffLine("Mentor", mentor));
                lines.add(formatStaffLine("Sekretar", secretary));

                AcademicStaff chairman = (currentCommission != null) ? currentCommission.getMember1() : null;
                AcademicStaff member = (currentCommission != null) ? currentCommission.getMember2() : null;
                AcademicStaff substitute = (currentCommission != null) ? currentCommission.getMember3() : null;

                lines.add(formatStaffLine("Predsjednik komisije", chairman));
                lines.add(formatStaffLine("Član komisije", member));
                if (substitute != null) {
                    lines.add(formatStaffLine("Zamjenski član", substitute));
                }
            }
            default -> {
                // default minimal preview
                lines.add(formatStudentLine(student));
                lines.add(formatStaffLine("Mentor", mentor));
                lines.add(formatStaffLine("Sekretar", secretary));
            }
        }

        return lines;
    }

    /**
     * NOVO: Handler za slanje emaila sa dokumentom
     */
    private void handleSendEmailDocument(Document document) {
        if (document == null) {
            GlobalErrorHandler.error("Dokument nije pronađen.");
            return;
        }

        // Prikaži confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Pošalji Email");
        confirmation.setHeaderText("Da li ste sigurni da želite poslati email?");

        String documentTypeName = "N/A";
        if (document.getDocumentType() != null) {
            documentTypeName = document.getDocumentType().getName();
        } else if (typeById.containsKey(document.getTypeId())) {
            documentTypeName = typeById.get(document.getTypeId()).getName();
        }

        List<String> previewLines = getRecipientPreviewLines(documentTypeName);
        String recipientsBlock = String.join("\n", previewLines);

        confirmation.setContentText(
                "Dokument će se poslati na sljedeće primaoce:\n" +
                        recipientsBlock +
                        "\n\nDokument: " + documentTypeName +
                        "\nStatus: " + document.getStatus()
        );

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Postavi DocumentType ako nije već postavljen
            if (document.getDocumentType() == null && typeById.containsKey(document.getTypeId())) {
                document.setDocumentType(typeById.get(document.getTypeId()));
            }

            // Pošalji email
            emailNotificationService.sendDocumentEmail(document);
        }
    }


    @FXML
    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrda brisanja");
        confirm.setHeaderText("Da li ste sigurni da želite obrisati ovaj završni rad?");
        confirm.setContentText("Ova akcija se ne može poništiti.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                thesisDAO.deleteThesis(thesisId);
                GlobalErrorHandler.info("Završni rad je uspješno obrisan!");
                back();
            } catch (Exception e) {
                GlobalErrorHandler.error("Greška pri brisanju završnog rada.", e);
            }
        }
    }

    @FXML
    private void handleEdit() {
        AppUser currentUser = UserSession.getUser();
        if (currentUser == null) {
            GlobalErrorHandler.error("Korisnik nije prijavljen");
            return;
        }

        int userId = currentUser.getId();

        boolean lockAcquired = thesisDAO.lockThesis(thesisId, userId);
        if (!lockAcquired) {
            ThesisLockInfoDTO info = thesisDAO.getLockInfo(thesisId);

            String lockedByName = (info != null && info.getLockedByUsername() != null && !info.getLockedByUsername().isBlank())
                    ? info.getLockedByUsername()
                    : "Nepoznat korisnik";

            Timestamp lockedAt = (info != null) ? info.getLockedAt() : null;

            showLockedMessage(lockedByName, lockedAt);
            return;
        }

        Thesis thesis = thesisDAO.getThesisById(thesisId);
        if (thesis != null) {
            SceneManager.showWithData(
                    "/app/thesisForm.fxml",
                    "Uredi završni rad",
                    (ThesisFormController controller) -> controller.initEdit(thesis, thesisId)
            );
        } else {
            thesisDAO.unlockThesis(thesisId, userId);
            GlobalErrorHandler.error("Greška pri dohvatanju podataka za uređivanje.");
        }
    }

    @FXML
    private void handleFormCommission() {
        if (currentDetails == null) {
            GlobalErrorHandler.error("Detalji rada nisu učitani.");
            return;
        }

        try {
            SceneManager.showWithData(
                    "/app/commissionForm.fxml",
                    "Dodavanje komisije",
                    (CommissionFormController controller) -> controller.initWithThesis(thesisId, currentDetails)
            );
        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri otvaranju forme komisije.", e);
        }
    }

    @FXML
    private void handleEditCommission() {
        if (currentDetails == null || currentCommission == null) {
            GlobalErrorHandler.error("Podaci nisu dostupni.");
            return;
        }

        try {
            SceneManager.showWithData(
                    "/app/commissionForm.fxml",
                    "Uredi komisiju",
                    (CommissionFormController controller) -> controller.initEditCommission(thesisId, currentDetails, currentCommission)
            );
        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri otvaranju forme za uređivanje komisije.", e);
        }
    }

    @FXML
    private void back() {
        // Dohvati trenutnog korisnika
        AppUser currentUser = UserSession.getUser();

        NavigationContext.setTargetView(DashboardView.THESIS);

        // Provjeri tip korisnika i vrati ga na odgovarajući dashboard
        if (currentUser != null && currentUser.getRole() != null) {
            String roleName = currentUser.getRole().getName();

            if ("SECRETARY".equalsIgnoreCase(roleName)) {
                // Sekretar se vraća na secretary dashboard
                SceneManager.show("/app/secretary-dashboard.fxml", "eDiploma - Sekretar");
                return;
            }
        }

        // Svi ostali korisnici se vraćaju na glavni dashboard
        SceneManager.show("/app/dashboard.fxml", "eDiploma");
    }

    @FXML
    private void handleOpenWrittenExamReport() {
        if (currentCommission == null || currentCommission.getMember1() == null) {
            GlobalErrorHandler.error("Komisija mora biti formirana prije kreiranja zapisnika.");
            return;
        }

        SceneManager.showWithData(
                "/app/writtenExamReport.fxml",
                "Zapisnik o pismenom dijelu",
                (WrittenExamReportController controller) -> controller.initWithThesisId(thesisId)
        );
    }

    @FXML
    private void handleOpenDefenseReport() {
        if (currentCommission == null || currentCommission.getMember1() == null) {
            GlobalErrorHandler.error("Komisija mora biti formirana prije kreiranja zapisnika.");
            return;
        }

        if (currentDetails != null && currentDetails.getDefenseDate() == null) {
            GlobalErrorHandler.error("Datum odbrane mora biti unesen prije kreiranja zapisnika.");
            return;
        }

        SceneManager.showWithData(
                "/app/defenseReport.fxml",
                "Zapisnik sa odbrane",
                (DefenseReportController controller) -> controller.initWithThesisId(thesisId)
        );
    }

    @FXML
    private void handleOpenFinalThesisApproalReport() {
        SceneManager.showWithData(
                "/app/finalThesisApprovalReport.fxml",
                "eDiploma",
                (FinalThesisApprovalController controller) -> controller.initWithThesisId(thesisId)
        );
    }

    private void showLockedMessage(String userName, Timestamp lockedAt) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Rad je zaključan");
        alert.setHeaderText("Uređivanje nije moguće");

        String whenText = "";
        if (lockedAt != null) {
            var dt = lockedAt.toLocalDateTime();
            whenText = "\nZaključano: " + dt.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy. HH:mm"));
        }

        alert.setContentText(
                "Ovaj završni rad trenutno uređuje: " + userName +
                        whenText +
                        "\n\nPokušajte ponovo za 30 minuta."
        );
        alert.showAndWait();
    }

    @FXML
    private void handleOpenCommissionReport() {
        if (currentCommission == null || currentCommission.getMember1() == null) {
            GlobalErrorHandler.error("Komisija mora biti formirana prije kreiranja rješenja.");
            return;
        }

        SceneManager.showWithData(
                "/app/commissionReport.fxml",
                "Rješenje o formiranju komisije",
                (CommissionReportController controller) -> controller.initWithThesisId(thesisId)
        );
    }

    @FXML
    private void handleOpenObavijest() {
        if (currentCommission == null || currentCommission.getMember1() == null) {
            GlobalErrorHandler.error("Komisija mora biti formirana prije kreiranja obavijesti.");
            return;
        }

        // Check if required documents are READY
        DocumentType commissionReportDocType = documentTypeDAO.getByName("Rješenje o formiranju Komisije");
        if (commissionReportDocType != null) {
            Document commissionReportDoc = documentDAO.getByThesisAndType(thesisId, commissionReportDocType.getId());
            if (commissionReportDoc == null || commissionReportDoc.getStatus() != DocumentStatus.READY) {
                GlobalErrorHandler.error("Dokument 'Rješenje o formiranju Komisije' mora biti završen prije kreiranja obavijesti.");
                return;
            }
        }

        DocumentType approvalDocType = documentTypeDAO.getByName("Rješenje o izradi završnog rada");
        if (approvalDocType != null) {
            Document approvalDoc = documentDAO.getByThesisAndType(thesisId, approvalDocType.getId());
            if (approvalDoc == null || approvalDoc.getStatus() != DocumentStatus.READY) {
                GlobalErrorHandler.error("Dokument 'Rješenje o izradi završnog rada' mora biti završen prije kreiranja obavijesti.");
                return;
            }
        }

        SceneManager.showWithData(
                "/app/notice.fxml",
                "Obavijest o terminu završnog rada",
                (NoticeController controller) -> controller.initWithThesisId(thesisId)
        );
    }

    @FXML
    private void handleOpenCycleCompletion() {
        SceneManager.showWithData(
                "/app/cycleCompletion.fxml",
                "Uvjerenje o završenom ciklusu",
                (CycleCompletionController controller) -> controller.initWithThesisId(thesisId)
        );
    }

}
