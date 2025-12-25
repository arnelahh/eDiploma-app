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

        // Create a temp file
        File tempFile = File.createTempFile("pdf_font_", ".ttf");
        tempFile.deleteOnExit(); // Clean up on exit

        // Copy resource to temp file
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            inputStream.transferTo(out);
        }
        return tempFile;
    }

    private void generatePDF(File outputFile) throws Exception {
        LocalDate dateToShow = thesisDetails.getApprovalDate() != null ?
                thesisDetails.getApprovalDate() :
                thesisDetails.getApplicationDate();

        WrittenExamReportDTO dto = WrittenExamReportDTO.builder()
                .studentFullName(thesisDetails.getStudent().getLastName() + " " + thesisDetails.getStudent().getFirstName())
                .thesisTitle(thesisDetails.getTitle())
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
        html = html.replace("{{studentFullName}}", dto.getStudentFullName())
                .replace("{{thesisTitle}}", dto.getThesisTitle())
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

            // 1. Učitaj REGULAR (za običan tekst)
            // Težina: 400, Stil: NORMAL
            builder.useFont(getFontFileFromResources("LiberationSerif-Regular.ttf"), "Times New Roman");

            // 2. Učitaj BOLD (za naslove "POLITEHNIČKI FAKULTET", "ZAPISNIK", "Komisija...")
            // Težina: 700, Stil: NORMAL
            File fontBold = getFontFileFromResources("LiberationSerif-Bold.ttf");
            builder.useFont(fontBold, "Times New Roman", 700, BaseRendererBuilder.FontStyle.NORMAL, true);

            // 3. Učitaj ITALIC (ako zatreba)
            File fontItalic = getFontFileFromResources("LiberationSerif-Italic.ttf");
            builder.useFont(fontItalic, "Times New Roman", 400, BaseRendererBuilder.FontStyle.ITALIC, true);

            // 4. Učitaj BOLD ITALIC (ako zatreba)
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