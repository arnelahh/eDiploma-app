package controller;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.*;
import dto.CommissionReportDTO;
import dto.ThesisDetailsDTO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.Commission;
import utils.GlobalErrorHandler;
import utils.SceneManager;
import utils.UserSession;
import model.*;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class CommissionReportController {

    @FXML private Text studentNameText;
    @FXML private Text decisionDateText;
    @FXML private Text chairmanText;
    @FXML private Text member1Text;
    @FXML private Text mentorText;
    @FXML private Text secretaryText;
    @FXML private TextField documentNumberField;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();
    private DocumentType thisDocType;

    private int thesisId;
    private ThesisDetailsDTO thesisDetails;
    private Commission commission;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private static final String DOC_NUMBER_PREFIX = "11-403-103-";

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

            thisDocType = documentTypeDAO.getByName("Rješenje o formiranju Komisije");
            if (thisDocType == null) {
                GlobalErrorHandler.error("DocumentType nije pronađen.");
                back();
                return;
            }

            populateFields();

            // Učitaj postojeći dokument ako postoji
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
        // Student
        if (thesisDetails.getStudent() != null) {
            String fullName = thesisDetails.getStudent().getLastName() + " " +
                    thesisDetails.getStudent().getFirstName();
            studentNameText.setText(fullName.toUpperCase());
        }

        // Date
        decisionDateText.setText(LocalDate.now().format(DATE_FORMAT));

        // Commission
        if (commission.getMember1() != null) {
            chairmanText.setText(formatMemberName(commission.getMember1()));
        }

        if (commission.getMember2() != null) {
            member1Text.setText(formatMemberName(commission.getMember2()));
        }

        if (thesisDetails.getMentor() != null) {
            mentorText.setText(formatMemberName(thesisDetails.getMentor()));
        }

        if (thesisDetails.getSecretary() != null) {
            secretaryText.setText(formatMemberName(thesisDetails.getSecretary()));
        }
    }

    private String formatMemberName(model.AcademicStaff member) {
        if (member == null) return "—";
        String title = member.getTitle() != null ? member.getTitle() + " " : "";
        return title + member.getFirstName() + " " + member.getLastName();
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        try {
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
        String input = documentNumberField != null ? documentNumberField.getText().trim() : "";
        if (!input.isBlank() && !input.matches("\\d{4}")) {
            GlobalErrorHandler.error("Broj rješenja mora biti tačno 4 cifre (npr. 1295).");
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

        // Format: "11-403-103-1295/25"
        // Izvlačimo samo "1295"
        String s = fullNumber.trim();
        if (s.startsWith(DOC_NUMBER_PREFIX)) {
            s = s.substring(DOC_NUMBER_PREFIX.length());
        }

        int slash = s.indexOf('/');
        if (slash > 0) s = s.substring(0, slash);

        return s.trim();
    }

    private byte[] generatePdfBytes() throws Exception {
        String chairmanName = commission.getMember1() != null
                ? formatMemberName(commission.getMember1()) : "—";
        String member1Name = commission.getMember2() != null
                ? formatMemberName(commission.getMember2()) : "—";
        String mentorName = thesisDetails.getMentor() != null
                ? formatMemberName(thesisDetails.getMentor()) : "—";
        String secretaryName = thesisDetails.getSecretary() != null
                ? formatMemberName(thesisDetails.getSecretary()) : "—";

        // Dean info
        String deanName = "Prof.dr.sc. Samir Lemeš"; // Default


        String documentNumberForTemplate = buildFullDocumentNumber();
        if (documentNumberForTemplate == null || documentNumberForTemplate.isBlank()) {
            documentNumberForTemplate = DOC_NUMBER_PREFIX + "____/" +
                    String.format("%02d", LocalDate.now().getYear() % 100);
        }

        CommissionReportDTO dto = CommissionReportDTO.builder()
                .documentNumberPrefix(DOC_NUMBER_PREFIX)
                .userInputNumbers(documentNumberField.getText().trim())
                .documentNumberSuffix("/" + String.format("%02d", LocalDate.now().getYear() % 100))
                .decisionDate(LocalDate.now())
                .studentFullName(studentNameText.getText())
                .chairmanFullName(chairmanName)
                .member1FullName(member1Name)
                .mentorFullName(mentorName)
                .secretaryFullName(secretaryName)
                .deanFullName(deanName)
                .build();

        String html = loadTemplate();

        html = html.replace("{{documentNumber}}", documentNumberForTemplate)
                .replace("{{decisionDate}}", dto.getDecisionDate().format(DATE_FORMAT))
                .replace("{{studentFullName}}", dto.getStudentFullName())
                .replace("{{chairmanFullName}}", dto.getChairmanFullName())
                .replace("{{member1FullName}}", dto.getMember1FullName())
                .replace("{{mentorFullName}}", dto.getMentorFullName())
                .replace("{{secretaryFullName}}", dto.getSecretaryFullName())
                .replace("{{deanFullName}}", dto.getDeanFullName());

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
        InputStream is = getClass().getResourceAsStream("/templates/commission_report_template.html");
        if (is == null) {
            throw new FileNotFoundException("Template file not found!");
        }
        return new String(is.readAllBytes(), "UTF-8");
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