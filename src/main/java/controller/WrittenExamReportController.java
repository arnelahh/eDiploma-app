package controller;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.*;
import dto.ThesisDetailsDTO;
import dto.WrittenExamReportDTO;
import javafx.fxml.FXML;
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

public class WrittenExamReportController {

    @FXML private Text studentNameText;
    @FXML private Text thesisTitleText;
    @FXML private Text mentorNameText;
    @FXML private Text submissionDateText;
    @FXML private Text chairmanText;
    @FXML private Text member1Text;
    @FXML private Text member2Text;
    @FXML private Text secretaryText;
    @FXML private TextField facultyDecisionField;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();
    private DocumentType thisDocType;

    private int thesisId;
    private ThesisDetailsDTO thesisDetails;
    private Commission commission;
    private String approvalDecisionNumber; // From first document

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

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

            thisDocType = documentTypeDAO.getByName("Zapisnik o pismenom dijelu diplomskog rada");
            if (thisDocType == null) {
                GlobalErrorHandler.error("DocumentType nije pronađen.");
                back();
                return;
            }

            // Load approval decision number from first document
            DocumentType approvalDocType = documentTypeDAO.getByName("Rješenje o izradi završnog rada");
            if (approvalDocType != null) {
                Document approvalDoc = documentDAO.getByThesisAndType(thesisId, approvalDocType.getId());
                if (approvalDoc != null && approvalDoc.getDocumentNumber() != null) {
                    approvalDecisionNumber = approvalDoc.getDocumentNumber();
                } else {
                    approvalDecisionNumber = "";
                }
            } else {
                approvalDecisionNumber = "";
            }

            populateFields();

            // Učitaj postojeći dokument ako postoji
            Document existing = documentDAO.getByThesisAndType(thesisId, thisDocType.getId());


        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri učitavanju podataka.", e);
        }
    }

    private void populateFields() {
        if (thesisDetails.getStudent() != null) {
            studentNameText.setText(thesisDetails.getStudent().getLastName() + " " +
                    thesisDetails.getStudent().getFirstName());
        }

        thesisTitleText.setText(thesisDetails.getTitle() != null ?
                thesisDetails.getTitle().toUpperCase() : "");

        if (thesisDetails.getMentor() != null) {
            String mentorName = (thesisDetails.getMentor().getTitle() != null ?
                    thesisDetails.getMentor().getTitle() + " " : "") +
                    thesisDetails.getMentor().getFirstName() + " " +
                    thesisDetails.getMentor().getLastName();
            mentorNameText.setText(mentorName);
            member2Text.setText(mentorName);
        }

        LocalDate dateToShow = thesisDetails.getApprovalDate() != null ?
                thesisDetails.getApprovalDate() :
                thesisDetails.getApplicationDate();

        if (dateToShow != null) {
            submissionDateText.setText(dateToShow.format(DATE_FORMAT));
        } else {
            submissionDateText.setText("—");
        }

        if (commission.getMember1() != null) {
            chairmanText.setText(formatMemberName(commission.getMember1()));
        }

        if (commission.getMember2() != null) {
            member1Text.setText(formatMemberName(commission.getMember2()));
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

        try {
            byte[] pdfBytes = generatePdfBytes();
            String base64 = Base64.getEncoder().encodeToString(pdfBytes);

            DocumentStatus status = DocumentStatus.READY;

            Integer userId = null;
            AppUser u = UserSession.getUser();
            if (u != null) userId = u.getId();

            documentDAO.upsert(
                    thesisId,
                    thisDocType.getId(),
                    base64,
                    userId,
                    status
            );

            GlobalErrorHandler.info("Dokument je uspješno sačuvan.");
            back();

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri snimanju dokumenta.", e);
        }
    }

    private byte[] generatePdfBytes() throws Exception {
        LocalDate dateToShow = thesisDetails.getApprovalDate() != null ?
                thesisDetails.getApprovalDate() :
                thesisDetails.getApplicationDate();

        String[] titleParts = splitThesisTitle(thesisDetails.getTitle());
        boolean useSmallFont = titleParts[1].length() > 70;

        WrittenExamReportDTO dto = WrittenExamReportDTO.builder()
                .studentFullName(thesisDetails.getStudent().getLastName() + " " +
                        thesisDetails.getStudent().getFirstName())
                .thesisTitle(thesisDetails.getTitle())
                .thesisTitleLine1(titleParts[0])
                .thesisTitleLine2(titleParts[1])
                .mentorFullName(formatMemberName(thesisDetails.getMentor()))
                .submissionDate(dateToShow)
                .chairmanFullName(formatMemberName(commission.getMember1()))
                .member1FullName(formatMemberName(commission.getMember2()))
                .member2FullName(commission.getMember3() != null ?
                        formatMemberName(commission.getMember3()) : "—")
                .secretaryFullName(formatMemberName(thesisDetails.getSecretary()))
                .facultyDecisionNumber(approvalDecisionNumber != null ? approvalDecisionNumber : "")
                .build();

        String html = loadTemplate();

        String line1Class = useSmallFont ? "thesis-line-1 thesis-line-1-small" : "thesis-line-1";
        String line2Class = useSmallFont ? "thesis-line-2 thesis-line-2-small" : "thesis-line-2";

        html = html.replace("{{studentFullName}}", dto.getStudentFullName())
                .replace("{{thesisTitleLine1}}", dto.getThesisTitleLine1())
                .replace("{{thesisTitleLine2}}", dto.getThesisTitleLine2())
                .replace("class=\"thesis-line-1\"", "class=\"" + line1Class + "\"")
                .replace("class=\"thesis-line-2\"", "class=\"" + line2Class + "\"")
                .replace("{{mentorFullName}}", dto.getMentorFullName())
                .replace("{{submissionDate}}", dto.getSubmissionDate() != null ?
                        dto.getSubmissionDate().format(DATE_FORMAT) : "—")
                .replace("{{chairmanFullName}}", dto.getChairmanFullName())
                .replace("{{member1FullName}}", dto.getMember1FullName())
                .replace("{{member2FullName}}", dto.getMember2FullName())
                .replace("{{secretaryFullName}}", dto.getSecretaryFullName())
                .replace("{{facultyDecisionNumber}}", dto.getFacultyDecisionNumber());

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

    private String[] splitThesisTitle(String fullTitle) {
        if (fullTitle == null || fullTitle.isEmpty()) {
            return new String[]{"\"\"", "\"\""};
        }

        String upperTitle = fullTitle.toUpperCase();
        int targetLength = 50;

        if (upperTitle.length() <= targetLength) {
            return new String[]{"\"" + upperTitle + "\"", ""};
        }

        int splitIndex = upperTitle.lastIndexOf(' ', targetLength);

        if (splitIndex < 25) {
            splitIndex = targetLength;
        }

        String firstPart = "\"" + upperTitle.substring(0, splitIndex).trim();
        String secondPart = upperTitle.substring(splitIndex).trim() + "\"";

        return new String[]{firstPart, secondPart};
    }

    private String loadTemplate() throws IOException {
        InputStream is = getClass().getResourceAsStream("/templates/written_exam_report_template.html");
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
