package controller;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.CommissionDAO;
import dao.ThesisDAO;
import dto.ThesisDetailsDTO;
import dto.WrittenExamReportDTO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.Commission;
import utils.GlobalErrorHandler;
import utils.SceneManager;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class WrittenExamReportController {

    @FXML private Text studentNameText;
    @FXML private Text thesisTitleText;
    @FXML private Text mentorNameText;
    @FXML private Text submissionDateText;
    @FXML private Text chairmanText;
    @FXML private Text member1Text;
    @FXML private Text member2Text;
    @FXML private Text secretaryText;
    @FXML private Text proposedGradeText;
    @FXML private TextField facultyDecisionField;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();

    private int thesisId;
    private ThesisDetailsDTO thesisDetails;
    private Commission commission;

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
                return;
            }

            if (commission == null || commission.getMember1() == null) {
                GlobalErrorHandler.error("Komisija nije formirana za ovaj rad.");
                back();
                return;
            }

            populateFields();

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri učitavanju podataka.", e);
        }
    }

    private void populateFields() {
        // Student
        if (thesisDetails.getStudent() != null) {
            studentNameText.setText(thesisDetails.getStudent().getLastName() + " " + thesisDetails.getStudent().getFirstName());
        }

        // Thesis title
        thesisTitleText.setText(thesisDetails.getTitle() != null ?
                thesisDetails.getTitle().toUpperCase() : "");

        // Mentor
        if (thesisDetails.getMentor() != null) {
            String mentorName = (thesisDetails.getMentor().getTitle() != null ?
                    thesisDetails.getMentor().getTitle() + " " : "") +
                    thesisDetails.getMentor().getFirstName() + " " +
                    thesisDetails.getMentor().getLastName();
            mentorNameText.setText(mentorName);
        }

        // Submission date (use application date or approval date)
        LocalDate dateToShow = thesisDetails.getApprovalDate() != null ?
                thesisDetails.getApprovalDate() :
                thesisDetails.getApplicationDate();

        if (dateToShow != null) {
            submissionDateText.setText(dateToShow.format(DATE_FORMAT));
        } else {
            submissionDateText.setText("—");
        }

        // Commission
        if (commission.getMember1() != null) {
            chairmanText.setText(formatMemberName(commission.getMember1()));
        }

        if (commission.getMember2() != null) {
            member1Text.setText(formatMemberName(commission.getMember2()));
        }

        if (commission.getMember3() != null) {
            member2Text.setText(formatMemberName(commission.getMember3()));
        } else {
            member2Text.setText("—");
        }

        // Secretary
        if (thesisDetails.getSecretary() != null) {
            secretaryText.setText(formatMemberName(thesisDetails.getSecretary()));
        }

        // Proposed grade
        if (thesisDetails.getGrade() != null && thesisDetails.getGrade() > 0) {
            proposedGradeText.setText(String.valueOf(thesisDetails.getGrade()));
        } else {
            proposedGradeText.setText("—");
        }
    }

    private String formatMemberName(model.AcademicStaff member) {
        if (member == null) return "—";
        String title = member.getTitle() != null ? member.getTitle() + " " : "";
        return title + member.getFirstName() + " " + member.getLastName();
    }

    @FXML
    private void handleDownloadPDF() {
        String facultyDecision = facultyDecisionField.getText();
        if (facultyDecision == null || facultyDecision.trim().isEmpty()) {
            GlobalErrorHandler.error("Molimo unesite broj rješenja Fakulteta.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sačuvaj PDF");
        fileChooser.setInitialFileName("Zapisnik_Pismeni_" +
                thesisDetails.getStudent().getLastName() + "_" +
                thesisDetails.getStudent().getIndexNumber() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(facultyDecisionField.getScene().getWindow());

        if (file != null) {
            try {
                generatePDF(file);
                GlobalErrorHandler.info("PDF je uspješno sačuvan!");
            } catch (Exception e) {
                GlobalErrorHandler.error("Greška pri kreiranju PDF-a.", e);
            }
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

    /**
     * Splits the thesis title into two parts for PDF display.
     * First part: up to ~50 characters (breaks at last space)
     * Second part: remaining text
     * Both parts are uppercase and wrapped in quotes.
     * ISTA LOGIKA KAO U DefenseReportController - TARGET 50
     */
    private String[] splitThesisTitle(String fullTitle) {
        if (fullTitle == null || fullTitle.isEmpty()) {
            return new String[]{"\"\"", "\"\""};
        }

        String upperTitle = fullTitle.toUpperCase();

        // Target length for first line (around 50 chars)
        int targetLength = 50;

        // If title is short enough, put it all on first line
        if (upperTitle.length() <= targetLength) {
            return new String[]{"\"" + upperTitle + "\"", ""};
        }

        // Find the last space before target length
        int splitIndex = upperTitle.lastIndexOf(' ', targetLength);

        // If no space found, or space is too early, use target length
        if (splitIndex < 25) {
            splitIndex = targetLength;
        }

        String firstPart = "\"" + upperTitle.substring(0, splitIndex).trim();
        String secondPart = upperTitle.substring(splitIndex).trim() + "\"";

        return new String[]{firstPart, secondPart};
    }

    private void generatePDF(File outputFile) throws Exception {
        LocalDate dateToShow = thesisDetails.getApprovalDate() != null ?
                thesisDetails.getApprovalDate() :
                thesisDetails.getApplicationDate();

        // Split thesis title into two parts - TARGET 50
        String[] titleParts = splitThesisTitle(thesisDetails.getTitle());

        // Check if second line is too long (more than ~70 chars) - needs smaller font on BOTH lines
        boolean useSmallFont = titleParts[1].length() > 70;

        WrittenExamReportDTO dto = WrittenExamReportDTO.builder()
                .studentFullName(thesisDetails.getStudent().getLastName() + " " +
                        thesisDetails.getStudent().getFirstName())
                .thesisTitle(thesisDetails.getTitle())
                .thesisTitleLine1(titleParts[0])  // First part with opening quote
                .thesisTitleLine2(titleParts[1])  // Second part with closing quote
                .mentorFullName(formatMemberName(thesisDetails.getMentor()))
                .submissionDate(dateToShow)
                .chairmanFullName(formatMemberName(commission.getMember1()))
                .member1FullName(formatMemberName(commission.getMember2()))
                .member2FullName(commission.getMember3() != null ?
                        formatMemberName(commission.getMember3()) : "—")
                .secretaryFullName(formatMemberName(thesisDetails.getSecretary()))
                .facultyDecisionNumber(facultyDecisionField.getText().trim())
                .proposedGrade(thesisDetails.getGrade())
                .build();

        String html = loadTemplate();

        // Dodaj CSS klasu za smanjeni font na OBE linije ako je potrebno
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
                .replace("{{facultyDecisionNumber}}", dto.getFacultyDecisionNumber())
                .replace("{{proposedGrade}}", dto.getProposedGrade() != null ?
                        String.valueOf(dto.getProposedGrade()) : "—");

        try (OutputStream os = new FileOutputStream(outputFile)) {
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
            builder.toStream(os);
            builder.run();
        }
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