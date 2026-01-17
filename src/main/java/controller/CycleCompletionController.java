package controller;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.*;
import dto.CycleCompletionDTO;
import dto.ThesisDetailsDTO;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import model.*;
import utils.DeanService;
import utils.GlobalErrorHandler;
import utils.SceneManager;
import utils.UserSession;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class CycleCompletionController {

    @FXML private Text fatherNameText;
    @FXML private Text firstNameText;
    @FXML private Text lastNameText;
    @FXML private Text birthDateText;
    @FXML private Text birthPlaceText;
    @FXML private Text municipalityText;
    @FXML private Text countryText;
    @FXML private Text studyProgramText;
    @FXML private Text ectsText;
    @FXML private Text cycleText;
    @FXML private Text cycleDurationText;
    @FXML private TextField documentNumberField;
    @FXML private TextField studentGenitiveField;
    @FXML private TextField academicTitleField;
    @FXML private DatePicker cycleCompletionDatePicker;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();
    private DocumentType thisDocType;

    private int thesisId;
    private ThesisDetailsDTO thesisDetails;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy.");
    private static final String DOC_NUMBER_PREFIX = "11-403-105-";

    public void initWithThesisId(int thesisId) {
        this.thesisId = thesisId;
        loadData();
    }

    private void loadData() {
        try {
            thesisDetails = thesisDAO.getThesisDetails(thesisId);

            if (thesisDetails == null) {
                GlobalErrorHandler.error("Završni rad nije pronađen.");
                back();
                return;
            }

            thisDocType = documentTypeDAO.getByName("Uvjerenje o završenom ciklusu");
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
        if (thesisDetails.getStudent() != null) {
            Student student = thesisDetails.getStudent();

            // Ime
            firstNameText.setText(student.getFirstName());

            // Prezime
            lastNameText.setText(student.getLastName());

            //Ime oca
            fatherNameText.setText(student.getFatherName());

            // Datum rođenja
            birthDateText.setText(student.getBirthDate() != null ?
                    student.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")) : "—");

            // Mjesto rođenja
            birthPlaceText.setText(student.getBirthPlace() != null ? student.getBirthPlace() : "—");

            // Općina
            municipalityText.setText(student.getMunicipality() != null ? student.getMunicipality() : "—");

            // Država
            countryText.setText(student.getCountry() != null ? student.getCountry() : "—");

            // Studijski program
            studyProgramText.setText(student.getStudyProgram() != null ? student.getStudyProgram() : "—");

            // ECTS
            ectsText.setText(String.valueOf(student.getECTS()));

            // Ciklus
            String cycleRoman = convertToRoman(student.getCycle());
            cycleText.setText(cycleRoman);

            // Trajanje ciklusa
            cycleDurationText.setText(String.valueOf(student.getCycleDuration()));

            // Predlog za genitiv (može se urediti)
            if (studentGenitiveField != null &&
                    (studentGenitiveField.getText() == null || studentGenitiveField.getText().isBlank())) {
                String genitiveProposal = student.getLastName() + " " + student.getFirstName() + "a";
                studentGenitiveField.setText(genitiveProposal);
            }
        }

        // Load CycleCompletionDate from DB if exists
        if (cycleCompletionDatePicker != null) {
            if (thesisDetails.getCycleCompletionDate() != null) {
                cycleCompletionDatePicker.setValue(thesisDetails.getCycleCompletionDate());
            } else {
                cycleCompletionDatePicker.setValue(LocalDate.now());
            }
        }
    }

    private String convertToRoman(int cycle) {
        return switch (cycle) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(cycle);
        };
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        try {
            // Save CycleCompletionDate to database
            LocalDate cycleCompletionDate = cycleCompletionDatePicker.getValue();
            thesisDAO.updateCycleCompletionDate(thesisId, cycleCompletionDate);

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

            String current = thesisDAO.getStatusName(thesisId);

            if (status == DocumentStatus.READY) {
                if (ThesisStatuses.KREIRANJE_UVJERENJA.equals(current)) {

                    thesisDAO.updateStatusByName(
                            thesisId,
                            ThesisStatuses.ODBRANJEN
                    );
                }
            } else {
                if (!ThesisStatuses.KREIRANJE_UVJERENJA.equals(current)) {

                    thesisDAO.updateStatusByName(
                            thesisId,
                            ThesisStatuses.KREIRANJE_UVJERENJA
                    );
                }
            }


            GlobalErrorHandler.info("Dokument je uspješno sačuvan.");
            back();

        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri snimanju dokumenta.", e);
        }
    }

    private boolean validateInput() {
        if (cycleCompletionDatePicker == null || cycleCompletionDatePicker.getValue() == null) {
            GlobalErrorHandler.error("Molimo odaberite datum izdavanja uvjerenja.");
            return false;
        }

        String input = documentNumberField != null ? documentNumberField.getText().trim() : "";
        if (!input.isBlank() && !input.matches("\\d{4}")) {
            GlobalErrorHandler.error("Broj uvjerenja mora biti tačno 4 cifre (npr. 1295).");
            return false;
        }

        String studentGenitive = studentGenitiveField != null ? studentGenitiveField.getText().trim() : "";
        if (studentGenitive.isBlank()) {
            GlobalErrorHandler.error("Ime studenta u genitivu je obavezno.");
            return false;
        }


        return true;
    }

    private String formatBirthDate(LocalDate birthDate) {
        if (birthDate == null) return "";
        return birthDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy."));
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
        Student student = thesisDetails.getStudent();

        String studentGenitiveForm = studentGenitiveField.getText().trim();
        String academicTitle = getAcademicTitleByDepartment(thesisDetails.getDepartment().getName());

        // Build document number
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

        String cycleRoman = convertToRoman(student.getCycle());
        String cycleTextWord = convertCycleToText(student.getCycle());
        String cycleText = cycleRoman + " (" + cycleTextWord + ")";

        String cycleDurationWord = convertDurationToText(student.getCycleDuration());
        String cycleDurationText = student.getCycleDuration() + " (" + cycleDurationWord + ")";

        // Get dean name dynamically from database
        String deanName = DeanService.getCurrentDeanFullName();

        // Get dates
        LocalDate cycleCompletionDate = cycleCompletionDatePicker.getValue(); // For header
        LocalDate defenseDate = thesisDetails.getDefenseDate(); // For text "dana ... godine"
        if (defenseDate == null) {
            defenseDate = LocalDate.now(); // Fallback if not set
        }

        CycleCompletionDTO dto = CycleCompletionDTO.builder()
                .studentFullName(student.getFirstName() + " (" +student.getFatherName() +") " + student.getLastName())
                .studentGenitiveForm(studentGenitiveForm)
                .birthDate(student.getBirthDate())
                .birthPlace(student.getBirthPlace())
                .municipality(student.getMunicipality())
                .country(student.getCountry())
                .studyProgram(student.getStudyProgram())
                .cycle(cycleText)
                .cycleDuration(cycleDurationText)
                .ects(String.valueOf(student.getECTS()))
                .academicTitle(academicTitle)
                .cycleCompletionDate(cycleCompletionDate)
                .defenseDate(defenseDate)
                .deanFullName(deanName)
                .build();

        String html = loadTemplate();

        html = html.replace("{{cycleCompletionDate}}", dto.getCycleCompletionDate().format(DATE_FORMAT))
                .replace("{{defenseDate}}", dto.getDefenseDate().format(DATE_FORMAT))
                .replace("{{studentFullName}}", dto.getStudentFullName())
                .replace("{{studentGenitiveForm}}", dto.getStudentGenitiveForm())
                .replace("{{birthDate}}", formatBirthDate(dto.getBirthDate()))
                .replace("{{birthPlace}}", dto.getBirthPlace())
                .replace("{{municipality}}", dto.getMunicipality())
                .replace("{{country}}", dto.getCountry())
                .replace("{{studyProgram}}", dto.getStudyProgram())
                .replace("{{cycle}}", dto.getCycle())
                .replace("{{cycleDuration}}", dto.getCycleDuration())
                .replace("{{ects}}", dto.getEcts())
                .replace("{{academicTitle}}", dto.getAcademicTitle())
                .replace("{{deanFullName}}", dto.getDeanFullName());

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

    private String generateGenitiveName(String firstName, String lastName) {
        // Jednostavna logika za genitiv - može se proširiti
        return lastName + " " + firstName;
    }

    private String convertCycleToText(int cycle) {
        return switch (cycle) {
            case 1 -> "prvi";
            case 2 -> "drugi";
            case 3 -> "treći";
            default -> String.valueOf(cycle);
        };
    }

    private String convertDurationToText(int duration) {
        return switch (duration) {
            case 1 -> "jedne";
            case 2 -> "dvije";
            case 3 -> "tri";
            case 4 -> "četiri";
            case 5 -> "pet";
            case 6 -> "šest";
            default -> String.valueOf(duration);
        };
    }

    private String convertNumberToText(int number) {
        return switch (number) {
            case 1 -> "jednu";
            case 2 -> "dvije";
            case 3 -> "tri";
            case 4 -> "četiri";
            case 5 -> "pet";
            case 6 -> "šest";
            default -> String.valueOf(number);
        };
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
        InputStream is = getClass().getResourceAsStream("/templates/cycle_completion_template.html");
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

    private String getAcademicTitleByDepartment(String departmentName){
        if(departmentName == null || departmentName.isBlank()) return "";
        String normalized = departmentName.trim().toLowerCase();
        if(normalized.contains("softversko")){
            return "SOFTVER INŽENJER";
        }else if(normalized.contains("građevinarstvo")){
            return "DIPLOMIRANI INŽENJER GRAĐEVINARSTVA";
        }else if(normalized.contains("proizvodni")){
            return "PROIZVODNI INŽENJER";
        }
        return "";
    }
}