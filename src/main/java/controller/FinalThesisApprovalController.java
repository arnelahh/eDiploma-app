package controller;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.ThesisDAO;
import dto.FinalThesisApprovalDTO;
import dto.ThesisDetailsDTO;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.AcademicStaff;
import model.StudentStatus;
import utils.GlobalErrorHandler;
import utils.SceneManager;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class FinalThesisApprovalController {

    // --- FXML Fields ---
    @FXML private Text studentNameText;
    @FXML private Text titleText;
    @FXML private Text mentorText;
    @FXML private Text subjectText;
    @FXML private TextArea descriptionPreview;
    @FXML private TextArea literaturePreview;

    @FXML private TextField decisionNumberField;
    @FXML private DatePicker decisionDatePicker;
    @FXML private TextField studentGenitiveField;

    // --- Data & Services ---
    private final ThesisDAO thesisDAO = new ThesisDAO();
    private int thesisId;
    private ThesisDetailsDTO thesisDetails;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    // --- Initialization ---

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

            populateReadOnlyFields();

            // Postavi današnji datum kao default
            decisionDatePicker.setValue(LocalDate.now());

            // Pokušaj predložiti ime u genitivu (korisnik ovo može/treba ispraviti)
            if (thesisDetails.getStudent() != null) {
                // Heuristika: Prezime Ime
                String suggestion = thesisDetails.getStudent().getLastName() + " " + thesisDetails.getStudent().getFirstName();
                studentGenitiveField.setText(suggestion);
            }

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri učitavanju podataka.", e);
            e.printStackTrace();
        }
    }

    private void populateReadOnlyFields() {
        // 1. Student Name Formatting: "Ime (ImeOca) Prezime"
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

        // 2. Thesis Data
        titleText.setText(thesisDetails.getTitle() != null ? thesisDetails.getTitle().toUpperCase() : "");
        subjectText.setText(thesisDetails.getSubject() != null ? thesisDetails.getSubject().getName() : "—");

        if (thesisDetails.getMentor() != null) {
            mentorText.setText(formatMemberName(thesisDetails.getMentor()));
        }

        // 3. Previews
        descriptionPreview.setText(thesisDetails.getDescription());
        literaturePreview.setText(thesisDetails.getLiterature());
    }

    // --- Actions ---

    @FXML
    private void handleDownloadPDF() {
        if (!validateInput()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sačuvaj Rješenje");

        // Sigurno ime fajla
        String safeStudentName = "Student";
        if (thesisDetails.getStudent() != null) {
            safeStudentName = thesisDetails.getStudent().getLastName();
        }

        fileChooser.setInitialFileName("Rjesenje_o_izradi_" + safeStudentName + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(studentNameText.getScene().getWindow());

        if (file != null) {
            try {
                generatePDF(file);
                GlobalErrorHandler.info("Rješenje uspješno kreirano!");
            } catch (Exception e) {
                GlobalErrorHandler.error("Greška pri generisanju PDF-a.", e);
                e.printStackTrace();
            }
        }
    }

    private boolean validateInput() {
        if (decisionNumberField.getText() == null || decisionNumberField.getText().trim().isEmpty()) {
            GlobalErrorHandler.error("Molimo unesite broj rješenja.");
            return false;
        }
        if (decisionDatePicker.getValue() == null) {
            GlobalErrorHandler.error("Molimo odaberite datum rješenja.");
            return false;
        }
        if (studentGenitiveField.getText() == null || studentGenitiveField.getText().trim().isEmpty()) {
            GlobalErrorHandler.error("Molimo unesite ime studenta u genitivu.");
            return false;
        }
        return true;
    }

    // --- PDF Generation Logic ---

    private void generatePDF(File outputFile) throws Exception {
        // 1. Priprema Statusa (Genitiv iz baze -> Nominativ konverzija)
        String statusGenitive = "studenta"; // Default
        if (thesisDetails.getStudent() != null && thesisDetails.getStudent().getStatus() != null) {
            if(Objects.equals(thesisDetails.getStudent().getStatus().getName(), "redovan")){
                statusGenitive = "redovnog studenta";
            }else {
                statusGenitive = statusGenitive+ " "+ thesisDetails.getStudent().getStatus().getName()+"a";
            }

        }

        String statusNominative = convertToNominative(statusGenitive);
        int cycleInt = 1; // Default
        if (thesisDetails.getStudent() != null) {
            cycleInt = thesisDetails.getStudent().getCycle();
        }
        String cycleRoman = convertToRoman(cycleInt);
        // 2. Priprema Teksta (Description & Literature)
        String formattedDesc = formatTextToHtml(thesisDetails.getDescription());
        String formattedLit = formatTextToHtml(thesisDetails.getLiterature());
        String firstName = thesisDetails.getStudent().getFirstName();
        String lastName = thesisDetails.getStudent().getLastName();
        // 3. Build DTO
        FinalThesisApprovalDTO dto = FinalThesisApprovalDTO.builder()
                .decisionNumber(decisionNumberField.getText().trim())
                .decisionDate(decisionDatePicker.getValue())
                .studentFullName(studentNameText.getText()) // Već formatirano u populateReadOnlyFields
                .studentNameGenitive(studentGenitiveField.getText().trim())
                .studentFirstName(firstName)
                .studentLastName(lastName)
                // Statusi
                .studentStatusGenitive(statusGenitive)
                .studentStatusNominative(statusNominative)
                .studentCycle(cycleRoman)
                // Ostali podaci
                .departmentName(thesisDetails.getDepartment() != null ? thesisDetails.getDepartment().getName().toLowerCase() : "")
                .thesisTitle(thesisDetails.getTitle() != null ? thesisDetails.getTitle().toUpperCase() : "")
                .subjectName(thesisDetails.getSubject() != null ? thesisDetails.getSubject().getName() : "")
                .mentorFullNameAndTitle(thesisDetails.getMentor().getTitle()+" "+thesisDetails.getMentor().getFirstName()+" "+thesisDetails.getMentor().getLastName())
                .description(formattedDesc)
                .structure(thesisDetails.getStructure())
                .literature(formattedLit)
                .applicationDate(thesisDetails.getApplicationDate())
                .build();

        // 4. Load & Replace Template
        String html = loadTemplate();

        html = html.replace("{{decisionNumber}}", dto.getDecisionNumber())
                .replace("{{decisionDate}}", dto.getDecisionDate().format(DATE_FORMAT))
                .replace("{{studentNameGenitive}}", dto.getStudentNameGenitive())
                .replace("{{studentStatusGenitive}}", dto.getStudentStatusGenitive())
                .replace("{{studentFullName}}", dto.getStudentFullName())
                .replace("{{studentStatusNominative}}", dto.getStudentStatusNominative())
                .replace("{{studentCycle}}", escapeXml(dto.getStudentCycle()))
                .replace("{{departmentName}}", dto.getDepartmentName())
                .replace("{{thesisTitle}}",escapeXml(dto.getThesisTitle()))
                .replace("{{subjectName}}", dto.getSubjectName())
                .replace("{{mentorFullName}}", dto.getMentorFullNameAndTitle())
                .replace("{{description}}", dto.getDescription())
                .replace("{{literature}}", dto.getLiterature())
                .replace("{{structure}}", formatTextToHtml(dto.getStructure()))
                .replace("{{studentFirstName}}", escapeXml(dto.getStudentFirstName()))
                .replace("{{studentLastName}}", escapeXml(dto.getStudentLastName()))
                .replace("{{applicationDate}}", dto.getApplicationDate().format(DATE_FORMAT))
                // Dean hardkodiran ili dodati u DTO ako treba
                .replace("{{deanName}}", "Prof. dr. sc. Samir Lemeš");

        // 5. Render PDF
        // Generisanje (isto kao u prethodnom primjeru)
        try (OutputStream os = new FileOutputStream(outputFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Učitaj fontove (putanja mora biti tačna)
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
    // --- Helper Methods ---

    /**
     * Konvertuje status iz genitiva (kako je u bazi) u nominativ.
     * Prilagođeno riječima iz baze podataka korisnika.
     */
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

        // Fallback
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
        return member.getTitle()+ " " + member.getFirstName() + " " + member.getLastName();
    }

    private String loadTemplate() throws IOException {
        InputStream is = getClass().getResourceAsStream("/templates/final_thesis_approval_template.html");
        if (is == null) throw new FileNotFoundException("Template not found in /templates/");
        return new String(is.readAllBytes(), "UTF-8");
    }

    // Pomoćna za učitavanje fonta da kod bude čišći
    private InputStream getFontStream(String fontName) throws IOException {
        InputStream is = getClass().getResourceAsStream("/fonts/" + fontName);
        if (is == null) throw new FileNotFoundException("Font not found: " + fontName);
        return is;
    }

    @FXML
    private void back() {
        SceneManager.showWithData("/app/thesisDetails.fxml", "Detalji završnog rada",
                (Object controller) -> {
                    ((ThesisDetailsController) controller).initWithThesisId(thesisId);
                }
        );
    }

    // Dodajte ovu pomoćnu metodu za naslove i imena
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
    private String convertToRoman(int cycle) {
        switch (cycle) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            default: return String.valueOf(cycle); // Fallback ako je nešto čudno
        }
    }
}