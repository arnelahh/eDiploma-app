package controller;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.*;
import dto.NoticeDTO;
import dto.ThesisDetailsDTO;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import model.*;
import utils.GlobalErrorHandler;
import utils.SceneManager;
import utils.UserSession;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class NoticeController {

    @FXML private Text studentNameText;
    @FXML private Text thesisTitleText;
    @FXML private Text commissionDecisionNumberText;

    @FXML private DatePicker commissionDecisionDatePicker;
    @FXML private DatePicker commissionMeetingDatePicker;
    @FXML private DatePicker defenseDatePicker;
    @FXML private TextField defenseTimeField;
    @FXML private TextField documentNumberField;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();

    private DocumentType thisDocType;
    private DocumentType commissionReportDocType;

    private int thesisId;
    private ThesisDetailsDTO thesisDetails;
    private Commission commission;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private static final String DOC_NUMBER_PREFIX = "11-403-104-";

    public void initWithThesisId(int thesisId) {
        this.thesisId = thesisId;
        loadData();
    }

    private void loadData() {
        try {
            thesisDetails = thesisDAO.getThesisDetails(thesisId);
            commission = commissionDAO.getCommissionByThesisId(thesisId);

            if (thesisDetails == null) {
                GlobalErrorHandler.error("Završni rad nije pronađen.");
                back();
                return;
            }

            if (commission == null || commission.getMember1() == null) {
                GlobalErrorHandler.error("Komisija nije formirana za ovaj rad.");
                back();
                return;
            }

            thisDocType = documentTypeDAO.getByName("Obavijest");
            if (thisDocType == null) {
                GlobalErrorHandler.error("DocumentType 'Obavijest' nije pronađen.");
                back();
                return;
            }

            commissionReportDocType = documentTypeDAO.getByName("Rješenje o formiranju Komisije");
            if (commissionReportDocType == null) {
                GlobalErrorHandler.error("DocumentType 'Rješenje o formiranju Komisije' nije pronađen.");
                back();
                return;
            }

            // Check if required documents are READY
            Document commissionReportDoc = documentDAO.getByThesisAndType(thesisId, commissionReportDocType.getId());
            if (commissionReportDoc == null || commissionReportDoc.getStatus() != DocumentStatus.READY) {
                GlobalErrorHandler.error("Dokument 'Rješenje o formiranju Komisije' mora biti završen prije kreiranja obavijesti.");
                back();
                return;
            }

            DocumentType approvalDocType = documentTypeDAO.getByName("Rješenje o izradi završnog rada");
            if (approvalDocType != null) {
                Document approvalDoc = documentDAO.getByThesisAndType(thesisId, approvalDocType.getId());
                if (approvalDoc == null || approvalDoc.getStatus() != DocumentStatus.READY) {
                    GlobalErrorHandler.error("Dokument 'Rješenje o izradi završnog rada' mora biti završen prije kreiranja obavijesti.");
                    back();
                    return;
                }
            }

            populateFields();

            // Load commission decision number from previous document
            String commissionDecisionNumber = commissionReportDoc.getDocumentNumber();
            if (commissionDecisionNumber != null && !commissionDecisionNumber.isBlank()) {
                commissionDecisionNumberText.setText(commissionDecisionNumber);
            }

            // Load existing document if exists
            Document existing = documentDAO.getByThesisAndType(thesisId, thisDocType.getId());
            if (existing != null) {
                String extracted = extractUserDigits(existing.getDocumentNumber());
                if (extracted != null && documentNumberField != null) {
                    documentNumberField.setText(extracted);
                }
            }

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri učitavanju podataka.", e);
        }
    }

    private void populateFields() {
        // Student name
        if (thesisDetails.getStudent() != null) {
            String fullName = thesisDetails.getStudent().getLastName().toUpperCase() + " " +
                    thesisDetails.getStudent().getFirstName().toUpperCase();
            studentNameText.setText(fullName);
        }

        // Thesis title
        if (thesisDetails.getTitle() != null) {
            thesisTitleText.setText(thesisDetails.getTitle().toUpperCase());
        }

        // Set default dates if not set
        if (commissionDecisionDatePicker != null && commissionDecisionDatePicker.getValue() == null) {
            commissionDecisionDatePicker.setValue(LocalDate.now());
        }

        if (commissionMeetingDatePicker != null && commissionMeetingDatePicker.getValue() == null) {
            commissionMeetingDatePicker.setValue(LocalDate.now());
        }

        // If defense date exists in thesis, pre-fill it
        if (thesisDetails.getDefenseDate() != null && defenseDatePicker != null) {
            defenseDatePicker.setValue(thesisDetails.getDefenseDate());
        } else if (defenseDatePicker != null && defenseDatePicker.getValue() == null) {
            defenseDatePicker.setValue(LocalDate.now().plusDays(7)); // Default to one week from now
        }

        // Default defense time
        if (defenseTimeField != null && (defenseTimeField.getText() == null || defenseTimeField.getText().isBlank())) {
            defenseTimeField.setText("9:00");
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        try {
            // Update thesis defense date
            LocalDate defenseDate = defenseDatePicker.getValue();
            thesisDAO.updateDefenseDate(thesisId, defenseDate);

            byte[] pdfBytes = generatePdfBytes();
            String base64 = Base64.getEncoder().encodeToString(pdfBytes);

            String docNumber = buildFullDocumentNumber();
            DocumentStatus status = (docNumber != null && !docNumber.isBlank())
                    ? DocumentStatus.READY
                    : DocumentStatus.IN_PROGRESS;

            Integer userId = null;
            AppUser u = UserSession.getUser();
            if (u != null) userId = u.getId();

            documentDAO.upsert(
                    thesisId,
                    thisDocType.getId(),
                    base64,
                    userId,
                    docNumber,
                    status
            );

            GlobalErrorHandler.info("Dokument je uspješno sačuvan.");
            back();

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri snimanju dokumenta.", e);
        }
    }

    private boolean validateInput() {
        if (commissionDecisionDatePicker == null || commissionDecisionDatePicker.getValue() == null) {
            GlobalErrorHandler.error("Molimo odaberite datum rješenja komisije.");
            return false;
        }

        if (commissionMeetingDatePicker == null || commissionMeetingDatePicker.getValue() == null) {
            GlobalErrorHandler.error("Molimo odaberite datum sjednice komisije.");
            return false;
        }

        if (defenseDatePicker == null || defenseDatePicker.getValue() == null) {
            GlobalErrorHandler.error("Molimo odaberite datum odbrane.");
            return false;
        }

        String timeInput = defenseTimeField != null ? defenseTimeField.getText().trim() : "";
        if (timeInput.isBlank()) {
            GlobalErrorHandler.error("Molimo unesite vrijeme odbrane (npr. 9:00).");
            return false;
        }

        // Validate time format (HH:mm)
        if (!timeInput.matches("\\d{1,2}:\\d{2}")) {
            GlobalErrorHandler.error("Vrijeme mora biti u formatu HH:mm (npr. 9:00).");
            return false;
        }

        String input = documentNumberField != null ? documentNumberField.getText().trim() : "";
        if (!input.isBlank() && !input.matches("\\d{4}")) {
            GlobalErrorHandler.error("Broj obavijesti mora biti tačno 4 cifre (npr. 2252).");
            return false;
        }

        return true;
    }

    private String buildFullDocumentNumber() {
        String input = documentNumberField != null ? documentNumberField.getText().trim() : "";
        if (input.isBlank()) return null;

        String yy = String.format("%02d", LocalDate.now().getYear() % 100);
        return DOC_NUMBER_PREFIX + input + "/" + yy;
    }

    private String extractUserDigits(String fullNumber) {
        if (fullNumber == null || fullNumber.isBlank()) return null;

        String s = fullNumber.trim();
        if (s.startsWith(DOC_NUMBER_PREFIX)) {
            s = s.substring(DOC_NUMBER_PREFIX.length());
        }

        int slash = s.indexOf('/');
        if (slash > 0) s = s.substring(0, slash);

        return s.trim();
    }

    private byte[] generatePdfBytes() throws Exception {
        String deanName = "Prof.dr.sc. Samir Lemeš";
        String location = "u prostorijama Mašinskog fakulteta Univerziteta u Zenici";

        String userInput = documentNumberField.getText().trim();
        String yy = String.format("%02d", LocalDate.now().getYear() % 100);
        String fullDocNumber = DOC_NUMBER_PREFIX + userInput + "/" + yy;

        // Split into 18 characters for boxes
        String[] chars = new String[18];
        for (int i = 0; i < 18; i++) {
            if (i < fullDocNumber.length()) {
                chars[i] = String.valueOf(fullDocNumber.charAt(i));
            } else {
                chars[i] = "";
            }
        }

        NoticeDTO dto = NoticeDTO.builder()
                .documentDate(LocalDate.now())
                .studentFullName(studentNameText.getText())
                .thesisTitle(thesisTitleText.getText())
                .commissionDecisionNumber(commissionDecisionNumberText.getText())
                .commissionDecisionDate(commissionDecisionDatePicker.getValue())
                .commissionMeetingDate(commissionMeetingDatePicker.getValue())
                .defenseDate(defenseDatePicker.getValue())
                .defenseTime(defenseTimeField.getText().trim())
                .defenseLocation(location)
                .deanFullName(deanName)
                .build();

        String html = loadTemplate();

        // Format thesis title with lines
        String thesisTitle = dto.getThesisTitle();
        String thesisTitleHtml = formatThesisTitleWithLines(thesisTitle);

        html = html.replace("{{documentDate}}", dto.getDocumentDate().format(DATE_FORMAT))
                .replace("{{studentFullName}}", escapeXml(dto.getStudentFullName()))
                .replace("{{thesisTitle}}", thesisTitleHtml)
                .replace("{{commissionDecisionNumber}}", escapeXml(dto.getCommissionDecisionNumber()))
                .replace("{{commissionDecisionDate}}", dto.getCommissionDecisionDate().format(DATE_FORMAT))
                .replace("{{commissionMeetingDate}}", dto.getCommissionMeetingDate().format(DATE_FORMAT))
                .replace("{{defenseDate}}", dto.getDefenseDate().format(DATE_FORMAT))
                .replace("{{defenseTime}}", escapeXml(dto.getDefenseTime()))
                .replace("{{defenseLocation}}", escapeXml(dto.getDefenseLocation()))
                .replace("{{deanFullName}}", escapeXml(dto.getDeanFullName()));

        // Replace individual characters in boxes
        for (int i = 0; i < 18; i++) {
            html = html.replace("{{char" + i + "}}", chars[i]);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            builder.useFont(getFontFileFromResources("LiberationSerif-Regular.ttf"), "Times New Roman");
            File fontBold = getFontFileFromResources("LiberationSerif-Bold.ttf");
            builder.useFont(fontBold, "Times New Roman", 700, BaseRendererBuilder.FontStyle.NORMAL, true);
            File fontItalic = getFontFileFromResources("LiberationSerif-Italic.ttf");
            builder.useFont(fontItalic, "Times New Roman", 400, BaseRendererBuilder.FontStyle.ITALIC, true);
            File fontBoldItalic = getFontFileFromResources("LiberationSerif-BoldItalic.ttf");
            builder.useFont(fontBoldItalic, "Times New Roman", 700, BaseRendererBuilder.FontStyle.ITALIC, true);

            String baseUrl = getClass().getResource("/templates/").toExternalForm();
            builder.withHtmlContent(html, baseUrl);
            builder.toStream(baos);
            builder.run();

            return baos.toByteArray();
        }
    }

    private File getFontFileFromResources(String fileName) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream("/fonts/" + fileName);
        if (inputStream == null) {
            throw new FileNotFoundException("Font file not found in resources: " + fileName);
        }

        File tempFile = File.createTempFile("pdf_font_", ".ttf");
        tempFile.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            inputStream.transferTo(out);
        }
        return tempFile;
    }

    private String loadTemplate() throws IOException {
        InputStream is = getClass().getResourceAsStream("/templates/notice_template.html");
        if (is == null) {
            throw new FileNotFoundException("Template file not found!");
        }
        return new String(is.readAllBytes(), "UTF-8");
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String formatThesisTitleWithLines(String title) {
        if (title == null || title.isEmpty()) return "";

        String escaped = escapeXml(title);
        String[] words = escaped.split("\\s+");
        StringBuilder html = new StringBuilder();
        StringBuilder currentLine = new StringBuilder();
        int maxCharsPerLine = 45;

        boolean isFirstLine = true;

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxCharsPerLine && currentLine.length() > 0) {
                if (isFirstLine) {
                    html.append("<table class=\"thesis-line-table\"><tr>")
                            .append("<td class=\"label-cell\">sa temom</td>")
                            .append("<td class=\"content-cell\">\" ")
                            .append(currentLine.toString().trim())
                            .append("</td>")
                            .append("</tr></table>");
                    isFirstLine = false;
                } else {
                    html.append("<div class=\"thesis-line-full\">")
                            .append(currentLine.toString().trim())
                            .append("</div>");
                }
                currentLine = new StringBuilder();
            }
            if (currentLine.length() > 0) currentLine.append(" ");
            currentLine.append(word);
        }

        // Zadnja linija
        if (currentLine.length() > 0) {
            if (isFirstLine) {
                html.append("<table class=\"thesis-line-table\"><tr>")
                        .append("<td class=\"label-cell\">sa temom</td>")
                        .append("<td class=\"content-cell\">\" ")
                        .append(currentLine.toString().trim())
                        .append(" \"</td>")
                        .append("</tr></table>");
            } else {
                html.append("<div class=\"thesis-line-full\">")
                        .append(currentLine.toString().trim())
                        .append(" \"</div>");
            }
        }
        return html.toString();
    }

    @FXML
    private void back() {
        SceneManager.showWithData(
                "/app/thesisDetails.fxml",
                "Detalji završnog rada",
                (ThesisDetailsController controller) -> controller.initWithThesisId(thesisId)
        );
    }
}