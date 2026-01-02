package controller;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.DocumentDAO;
import dao.DocumentTypeDAO;
import dao.ThesisDAO;
import dto.FinalThesisApprovalDTO;
import dto.ThesisDetailsDTO;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import model.*;
import utils.GlobalErrorHandler;
import utils.SceneManager;
import utils.UserSession;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Base64;

public class FinalThesisApprovalController {

    @FXML private Text studentNameText;
    @FXML private Text titleText;
    @FXML private Text mentorText;
    @FXML private Text subjectText;
    @FXML private TextArea descriptionPreview;
    @FXML private TextArea structurePreview;
    @FXML private TextArea literaturePreview;

    @FXML private TextField decisionNumberField;
    @FXML private DatePicker decisionDatePicker;
    @FXML private TextField studentGenitiveField;
    @FXML private TextField studentStatusField;

    private final ThesisDAO thesisDAO = new ThesisDAO();

    private final DocumentDAO documentDAO = new DocumentDAO();
    private final DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();
    private DocumentType thisDocType;

    private int thesisId;
    private ThesisDetailsDTO thesisDetails;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public void initWithThesisId(int thesisId) {
        this.thesisId = thesisId;
        loadData();
    }

    private void loadData() {
        try {
            thesisDetails = thesisDAO.getThesisDetails(thesisId);

            if (thesisDetails == null) {
                GlobalErrorHandler.error("Podaci o završnom radu nisu pronađeni.");
                back();
                return;
            }

            thisDocType = documentTypeDAO.getByName("Rješenje o izradi završnog rada");
            if (thisDocType == null) {
                GlobalErrorHandler.error("DocumentType nije pronađen.");
                back();
                return;
            }

            populateReadOnlyFields();

            if (decisionDatePicker != null && decisionDatePicker.getValue() == null) {
                decisionDatePicker.setValue(LocalDate.now());
            }

            if (thesisDetails.getStudent() != null && (studentGenitiveField.getText() == null || studentGenitiveField.getText().isBlank())) {
                String suggestion = thesisDetails.getStudent().getLastName() + " " + thesisDetails.getStudent().getFirstName();
                studentGenitiveField.setText(suggestion);
            }

            if (studentStatusField != null && (studentStatusField.getText() == null || studentStatusField.getText().isBlank())) {
                studentStatusField.setText("apsolventa");
            }

            // učitaj postojeći doc (ako postoji) -> popuni broj
            Document existing = documentDAO.getByThesisAndType(thesisId, thisDocType.getId());
            if (existing != null) {
                String extracted = extractUserDigits(existing.getDocumentNumber(), thisDocType.getNumberPrefix());
                if (extracted != null && decisionNumberField != null) {
                    decisionNumberField.setText(extracted);
                }
            }

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri učitavanju podataka.", e);
        }
    }

    private void populateReadOnlyFields() {
        if (thesisDetails.getStudent() != null) {
            String firstName = thesisDetails.getStudent().getFirstName();
            String lastName = thesisDetails.getStudent().getLastName();
            String fatherName = thesisDetails.getStudent().getFatherName();

            StringBuilder sb = new StringBuilder();
            sb.append(firstName).append(" ");
            if (fatherName != null && !fatherName.isEmpty()) {
                sb.append("(").append(fatherName).append(") ");
            }
            sb.append(lastName);

            studentNameText.setText(sb.toString());
        }

        titleText.setText(thesisDetails.getTitle() != null ? thesisDetails.getTitle().toUpperCase() : "");
        subjectText.setText(thesisDetails.getSubject() != null ? thesisDetails.getSubject().getName() : "—");

        if (thesisDetails.getMentor() != null) {
            mentorText.setText(formatMemberName(thesisDetails.getMentor()));
        }

        descriptionPreview.setText(thesisDetails.getDescription());
        structurePreview.setText(thesisDetails.getStructure());
        literaturePreview.setText(thesisDetails.getLiterature());
    }

    @FXML
    private void handleSave() {
        if (!validateInputSmart()) return;

        try {
            byte[] pdfBytes = generatePdfBytes();
            String base64 = Base64.getEncoder().encodeToString(pdfBytes);

            String docNumber = null;
            if (thisDocType.isRequiresNumber()) {
                docNumber = buildFullDocumentNumberOrNull(); // null => IN_PROGRESS
            }

            DocumentStatus status;
            if (!thisDocType.isRequiresNumber()) {
                status = DocumentStatus.READY;
            } else {
                status = (docNumber != null && !docNumber.isBlank())
                        ? DocumentStatus.READY
                        : DocumentStatus.IN_PROGRESS;
            }

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

            GlobalErrorHandler.info("Dokument je uspješno sačuvan. ");
            back();

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri snimanju dokumenta.", e);
        }
    }

    private boolean validateInputSmart() {
        if (decisionDatePicker == null || decisionDatePicker.getValue() == null) {
            GlobalErrorHandler.error("Molimo odaberite datum rješenja.");
            return false;
        }
        if (studentGenitiveField == null || studentGenitiveField.getText() == null || studentGenitiveField.getText().trim().isEmpty()) {
            GlobalErrorHandler.error("Molimo unesite ime studenta u genitivu.");
            return false;
        }

        // broj nije obavezan => može IN_PROGRESS, ali ako je unesen mora biti cifre
        String input = decisionNumberField != null ? decisionNumberField.getText().trim() : "";
        if (!input.isBlank() && !input.matches("\\d{1,10}")) {
            GlobalErrorHandler.error("Broj rješenja unesite kao cifre (npr. 2210).");
            return false;
        }

        return true;
    }

    private String buildFullDocumentNumberOrNull() {
        String input = decisionNumberField != null ? decisionNumberField.getText().trim() : "";
        if (input.isBlank()) return null;

        String prefix = thisDocType != null ? thisDocType.getNumberPrefix() : null;
        String yy = String.format("%02d", LocalDate.now().getYear() % 100);

        if (prefix == null || prefix.isBlank()) {
            return input;
        }
        if (input.contains("/")) {
            return prefix + input;
        }
        return prefix + input + "/" + yy;
    }

    private String extractUserDigits(String full, String prefix) {
        if (full == null || full.isBlank()) return null;

        String s = full.trim();

        if (prefix != null && !prefix.isBlank() && s.startsWith(prefix)) {
            s = s.substring(prefix.length());
        }

        int slash = s.indexOf('/');
        if (slash > 0) s = s.substring(0, slash);

        return s.trim();
    }

    private byte[] generatePdfBytes() throws Exception {
        String statusGenitive = "studenta";
        if (thesisDetails.getStudent() != null && thesisDetails.getStudent().getStatus() != null) {
            if(Objects.equals(thesisDetails.getStudent().getStatus().getName(), "redovan")){
                statusGenitive = "redovnog studenta";
            }else {
                statusGenitive = statusGenitive+ " "+ thesisDetails.getStudent().getStatus().getName()+"a";
            }

        }

        String statusNominative = convertToNominative(statusGenitive);
        int cycleInt = (thesisDetails.getStudent() != null) ? thesisDetails.getStudent().getCycle() : 1;
        String cycleRoman = convertToRoman(cycleInt);

        String formattedDesc = formatTextToHtml(thesisDetails.getDescription());
        String formattedLit = formatTextToHtml(thesisDetails.getLiterature());
        String firstName = thesisDetails.getStudent().getFirstName();
        String lastName = thesisDetails.getStudent().getLastName();

        String decisionNumberForTemplate = "";
        if (thisDocType != null && thisDocType.isRequiresNumber()) {
            String full = buildFullDocumentNumberOrNull();
            decisionNumberForTemplate = (full != null) ? full : "";
        }

        FinalThesisApprovalDTO dto = FinalThesisApprovalDTO.builder()
                .decisionNumber(decisionNumberForTemplate)
                .decisionDate(decisionDatePicker.getValue())
                .studentFullName(studentNameText.getText())
                .studentNameGenitive(studentGenitiveField.getText().trim())
                .studentFirstName(firstName)
                .studentLastName(lastName)
                .studentStatusGenitive(statusGenitive)
                .studentStatusNominative(statusNominative)
                .studentCycle(cycleRoman)
                .departmentName(thesisDetails.getDepartment() != null ? thesisDetails.getDepartment().getName().toLowerCase() : "")
                .thesisTitle(thesisDetails.getTitle() != null ? thesisDetails.getTitle().toUpperCase() : "")
                .subjectName(thesisDetails.getSubject() != null ? thesisDetails.getSubject().getName() : "")
                .mentorFullNameAndTitle(thesisDetails.getMentor().getTitle() + " " + thesisDetails.getMentor().getFirstName() + " " + thesisDetails.getMentor().getLastName())
                .description(formattedDesc)
                .structure(thesisDetails.getStructure())
                .literature(formattedLit)
                .applicationDate(thesisDetails.getApplicationDate())
                .build();

        String html = loadTemplate();

        html = html.replace("{{decisionNumber}}", dto.getDecisionNumber())
                .replace("{{decisionDate}}", dto.getDecisionDate().format(DATE_FORMAT))
                .replace("{{studentNameGenitive}}", dto.getStudentNameGenitive())
                .replace("{{studentStatusGenitive}}", dto.getStudentStatusGenitive())
                .replace("{{studentFullName}}", dto.getStudentFullName())
                .replace("{{studentStatusNominative}}", dto.getStudentStatusNominative())
                .replace("{{studentCycle}}", escapeXml(dto.getStudentCycle()))
                .replace("{{departmentName}}", dto.getDepartmentName())
                .replace("{{thesisTitle}}", escapeXml(dto.getThesisTitle()))
                .replace("{{subjectName}}", dto.getSubjectName())
                .replace("{{mentorFullName}}", dto.getMentorFullNameAndTitle())
                .replace("{{description}}", dto.getDescription())
                .replace("{{literature}}", dto.getLiterature())
                .replace("{{structure}}", formatTextToHtml(dto.getStructure()))
                .replace("{{studentFirstName}}", escapeXml(dto.getStudentFirstName()))
                .replace("{{studentLastName}}", escapeXml(dto.getStudentLastName()))
                .replace("{{applicationDate}}", dto.getApplicationDate().format(DATE_FORMAT))
                .replace("{{deanName}}", "Prof. dr. sc. Samir Lemeš");

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
        if (inputStream == null) throw new FileNotFoundException("Font file not found in resources: " + fileName);

        File tempFile = File.createTempFile("pdf_font_", ".ttf");
        tempFile.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            inputStream.transferTo(out);
        }
        return tempFile;
    }

    private String convertToNominative(String genitiveStatus) {
        if (genitiveStatus == null) return "student";
        String s = genitiveStatus.toLowerCase().trim();

        if (s.contains("apsolvent")) {
            return "student apsolvent";
        }
        else if (s.contains("imatrikulant")) {
            return "student imatrikulant";
        }
        else if (s.contains("redovnog")) {
            return "redovan student";
        }
        else if (s.contains("vanredn") || s.contains("vandredn")) { // typo safe
            return "vanredan student";
        }
        else if (s.contains("daljinu") || s.contains("dl")) {
            return "student na daljinu";
        }
        if (s.contains("apsolvent")) return "apsolvent";
        if (s.contains("imatrikulant")) return "imatrikulant";
        if (s.contains("redovn")) return "redovan student";
        if (s.contains("vanredn") || s.contains("vandredn")) return "vanredan student";
        if (s.contains("daljinu") || s.contains("dl")) return "student na daljinu";

        return "student";
    }

    private String formatTextToHtml(String rawText) {
        if (rawText == null || rawText.isEmpty()) return "—";
        String safe = rawText
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
        return safe.replace("\n", "<br/>");
    }

    private String formatMemberName(AcademicStaff member) {
        if (member == null) return "—";
        return member.getTitle() + " " + member.getFirstName() + " " + member.getLastName();
    }

    private String loadTemplate() throws IOException {
        InputStream is = getClass().getResourceAsStream("/templates/final_thesis_approval_template.html");
        if (is == null) throw new FileNotFoundException("Template not found in /templates/");
        return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
    }

    @FXML
    private void back() {
        SceneManager.showWithData("/app/thesisDetails.fxml", "Detalji završnog rada",
                (Object controller) -> ((ThesisDetailsController) controller).initWithThesisId(thesisId)
        );
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String convertToRoman(int cycle) {
        return switch (cycle) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(cycle);
        };
    }
}
