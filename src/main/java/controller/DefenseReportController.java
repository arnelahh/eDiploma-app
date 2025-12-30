package controller;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.CommissionDAO;
import dao.ThesisDAO;
import dto.DefenseReportDTO;
import dto.ThesisDetailsDTO;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.Commission;
import utils.GlobalErrorHandler;
import utils.SceneManager;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DefenseReportController {

    @FXML private Text studentNameText;
    @FXML private Text thesisTitleText;
    @FXML private Text mentorNameText;
    @FXML private Text defenseDateText;
    @FXML private Text chairmanText;
    @FXML private Text member1Text;
    @FXML private Text member2Text;
    @FXML private Text secretaryText;
    @FXML private Text finalGradeText;

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
            studentNameText.setText(thesisDetails.getStudent().getLastName() + " " +
                    thesisDetails.getStudent().getFirstName());
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

        // Defense date
        LocalDate defenseDate = thesisDetails.getDefenseDate();
        if (defenseDate != null) {
            defenseDateText.setText(defenseDate.format(DATE_FORMAT));
        } else {
            defenseDateText.setText("—");
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

        // Final grade
        if (thesisDetails.getGrade() != null && thesisDetails.getGrade() > 0) {
            finalGradeText.setText(String.valueOf(thesisDetails.getGrade()));
        } else {
            finalGradeText.setText("—");
        }
    }

    private String formatMemberName(model.AcademicStaff member) {
        if (member == null) return "—";
        String title = member.getTitle() != null ? member.getTitle() + " " : "";
        return title + member.getFirstName() + " " + member.getLastName();
    }

    @FXML
    private void handleDownloadPDF() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sačuvaj PDF");
        fileChooser.setInitialFileName("Zapisnik_Odbrana_" +
                thesisDetails.getStudent().getLastName() + "_" +
                thesisDetails.getStudent().getIndexNumber() + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(finalGradeText.getScene().getWindow());

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
     * First part: up to ~40 characters (breaks at last space)
     * Second part: remaining text
     * Both parts are uppercase and wrapped in quotes.
     */
    private String[] splitThesisTitle(String fullTitle) {
        if (fullTitle == null || fullTitle.isEmpty()) {
            return new String[]{"\"\"", "\"\""};
        }

        String upperTitle = fullTitle.toUpperCase();

        // Target length for first line (around 40 chars)
        int targetLength = 40;

        // If title is short enough, put it all on first line
        if (upperTitle.length() <= targetLength) {
            return new String[]{"\"" + upperTitle + "\"", ""};
        }

        // Find the last space before target length
        int splitIndex = upperTitle.lastIndexOf(' ', targetLength);

        // If no space found, or space is too early, use target length
        if (splitIndex < 20) {
            splitIndex = targetLength;
        }

        String firstPart = "\"" + upperTitle.substring(0, splitIndex).trim();
        String secondPart = upperTitle.substring(splitIndex).trim() + "\"";

        return new String[]{firstPart, secondPart};
    }

    private void generatePDF(File outputFile) throws Exception {
        LocalDate defenseDate = thesisDetails.getDefenseDate();

        // Split thesis title into two parts
        String[] titleParts = splitThesisTitle(thesisDetails.getTitle());

        DefenseReportDTO dto = DefenseReportDTO.builder()
                .studentFullName(thesisDetails.getStudent().getLastName() + " " +
                        thesisDetails.getStudent().getFirstName())
                .thesisTitle(thesisDetails.getTitle())
                .thesisTitleLine1(titleParts[0])  // First part with opening quote
                .thesisTitleLine2(titleParts[1])  // Second part with closing quote
                .mentorFullName(formatMemberName(thesisDetails.getMentor()))
                .defenseDate(defenseDate)
                .chairmanFullName(formatMemberName(commission.getMember1()))
                .member1FullName(formatMemberName(commission.getMember2()))
                .member2FullName(commission.getMember3() != null ?
                        formatMemberName(commission.getMember3()) : "—")
                .secretaryFullName(formatMemberName(thesisDetails.getSecretary()))
                .finalGrade(thesisDetails.getGrade())
                .build();

        String html = loadTemplate();
        html = html.replace("{{studentFullName}}", dto.getStudentFullName())
                .replace("{{thesisTitleLine1}}", dto.getThesisTitleLine1())
                .replace("{{thesisTitleLine2}}", dto.getThesisTitleLine2())
                .replace("{{mentorFullName}}", dto.getMentorFullName())
                .replace("{{defenseDate}}", dto.getDefenseDate() != null ?
                        dto.getDefenseDate().format(DATE_FORMAT) : "—")
                .replace("{{chairmanFullName}}", dto.getChairmanFullName())
                .replace("{{member1FullName}}", dto.getMember1FullName())
                .replace("{{member2FullName}}", dto.getMember2FullName())
                .replace("{{secretaryFullName}}", dto.getSecretaryFullName())
                .replace("{{finalGrade}}", dto.getFinalGrade() != null ?
                        String.valueOf(dto.getFinalGrade()) : "—");

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
        InputStream is = getClass().getResourceAsStream("/templates/defense_report_template.html");
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