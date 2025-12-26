package controller;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import dao.CommissionDAO;
import dao.ThesisDAO;
import dto.CommissionDecisionDTO;
import dto.ThesisDetailsDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import model.AcademicStaff;
import model.Commission;
import utils.GlobalErrorHandler;
import utils.SceneManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;

public class CommissionDecisionController {

    @FXML private Text studentNameText;
    @FXML private Text decisionDateText;
    @FXML private Text thesisTitleText;
    @FXML private Text chairmanText;
    @FXML private Text member1Text;
    @FXML private Text member2Text;
    @FXML private Text secretaryText;
    @FXML private ProgressIndicator loader;

    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();
    private int thesisId;
    private CommissionDecisionDTO decisionData;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy.");

    public void initWithThesisId(int thesisId) {
        this.thesisId = thesisId;
        loadData();
    }

    private void loadData() {
        Task<CommissionDecisionDTO> task = new Task<>() {
            @Override
            protected CommissionDecisionDTO call() throws Exception {
                // Koristi getThesisDetails umjesto getThesisById za potpune podatke
                ThesisDetailsDTO thesisDetails = thesisDAO.getThesisDetails(thesisId);
                if (thesisDetails == null) {
                    throw new Exception("Završni rad nije pronađen.");
                }

                Commission commission = commissionDAO.getCommissionByThesisId(thesisId);
                if (commission == null) {
                    throw new Exception("Komisija nije formirana.");
                }

                CommissionDecisionDTO dto = new CommissionDecisionDTO();

                // Datum rješenja - koristimo approval date ili danasnji datum
                String decisionDate = thesisDetails.getApprovalDate() != null ?
                        thesisDetails.getApprovalDate().format(DATE_FORMATTER) : 
                        java.time.LocalDate.now().format(DATE_FORMATTER);
                dto.setDecisionDate(decisionDate);

                // Podaci o studentu - sada iz ThesisDetailsDTO
                if (thesisDetails.getStudent() != null) {
                    String firstName = thesisDetails.getStudent().getFirstName() != null ? 
                            thesisDetails.getStudent().getFirstName() : "";
                    String lastName = thesisDetails.getStudent().getLastName() != null ? 
                            thesisDetails.getStudent().getLastName() : "";
                    String studentName = (firstName + " " + lastName).trim();
                    dto.setStudentFullName(studentName.isEmpty() ? "Nepoznat student" : studentName.toUpperCase());
                } else {
                    dto.setStudentFullName("Nepoznat student");
                }

                // Naziv rada
                dto.setThesisTitle(thesisDetails.getTitle() != null ? thesisDetails.getTitle() : "Nepoznat naslov");

                // Članovi komisije
                if (commission.getMember1() != null) {
                    dto.setChairmanFullName(formatMemberName(commission.getMember1()));
                } else {
                    dto.setChairmanFullName("Nije imenovan");
                }
                
                if (commission.getMember2() != null) {
                    dto.setMember1FullName(formatMemberName(commission.getMember2()));
                } else {
                    dto.setMember1FullName("Nije imenovan");
                }
                
                // Mentor iz ThesisDetailsDTO
                if (thesisDetails.getMentor() != null) {
                    dto.setMember2FullName(formatMemberName(thesisDetails.getMentor()));
                } else {
                    dto.setMember2FullName("Nije imenovan");
                }
                
                // Sekretar iz ThesisDetailsDTO
                if (thesisDetails.getSecretary() != null) {
                    dto.setSecretaryFullName(formatMemberName(thesisDetails.getSecretary()));
                } else {
                    dto.setSecretaryFullName("Nije imenovan");
                }

                return dto;
            }
        };

        if (loader != null) {
            loader.visibleProperty().bind(task.runningProperty());
        }

        task.setOnSucceeded(e -> {
            decisionData = task.getValue();
            populateFields();
        });

        task.setOnFailed(e -> {
            GlobalErrorHandler.error("Greška pri učitavanju podataka.", task.getException());
            back();
        });

        new Thread(task, "load-commission-decision-data").start();
    }

    private void populateFields() {
        if (decisionData == null) return;

        studentNameText.setText(decisionData.getStudentFullName() != null ?
                decisionData.getStudentFullName() : "—");
        decisionDateText.setText(decisionData.getDecisionDate() != null ?
                decisionData.getDecisionDate() : "—");
        thesisTitleText.setText(decisionData.getThesisTitle() != null ?
                decisionData.getThesisTitle() : "—");
        chairmanText.setText(decisionData.getChairmanFullName() != null ?
                decisionData.getChairmanFullName() : "—");
        member1Text.setText(decisionData.getMember1FullName() != null ?
                decisionData.getMember1FullName() : "—");
        member2Text.setText(decisionData.getMember2FullName() != null ?
                decisionData.getMember2FullName() + " (Mentor)" : "—");
        secretaryText.setText(decisionData.getSecretaryFullName() != null ?
                decisionData.getSecretaryFullName() : "—");
    }

    private String formatMemberName(AcademicStaff member) {
        if (member == null) return "Nije imenovan";
        
        String title = member.getTitle() != null ? member.getTitle() : "";
        String firstName = member.getFirstName() != null ? member.getFirstName() : "";
        String lastName = member.getLastName() != null ? member.getLastName() : "";
        
        String fullName = (title + " " + firstName + " " + lastName).trim();
        return fullName.isEmpty() ? "Nije imenovan" : fullName;
    }

    @FXML
    private void handleDownloadPDF() {
        if (decisionData == null) {
            GlobalErrorHandler.error("Podaci nisu učitani.");
            return;
        }

        // Dodatna validacija - provjerimo da li su svi podaci dostupni
        if (decisionData.getStudentFullName() == null || decisionData.getStudentFullName().isEmpty()) {
            GlobalErrorHandler.error("Podaci o studentu nisu dostupni.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sačuvaj Rješenje o formiranju komisije");
        
        // Sigurno formiraj ime fajla
        String safeFileName = "Rjesenje_Komisija_" + 
                decisionData.getStudentFullName().replace(" ", "_").replaceAll("[^a-zA-Z0-9_-]", "") + ".pdf";
        fileChooser.setInitialFileName(safeFileName);
        
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF fajlovi", "*.pdf")
        );

        File file = fileChooser.showSaveDialog(studentNameText.getScene().getWindow());
        if (file != null) {
            Task<Void> pdfTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    generatePDF(file);
                    return null;
                }
            };

            if (loader != null) {
                loader.visibleProperty().bind(pdfTask.runningProperty());
            }

            pdfTask.setOnSucceeded(e -> {
                GlobalErrorHandler.info("PDF je uspešno sačuvan!");
            });

            pdfTask.setOnFailed(e -> {
                GlobalErrorHandler.error("Greška pri generisanju PDF-a.", pdfTask.getException());
            });

            new Thread(pdfTask, "generate-commission-decision-pdf").start();
        }
    }

    private void generatePDF(File outputFile) throws Exception {
        // Učitaj HTML template
        InputStream templateStream = getClass().getResourceAsStream("/templates/commission_decision_template.html");
        if (templateStream == null) {
            throw new Exception("Template file not found!");
        }

        String htmlTemplate = new String(templateStream.readAllBytes(), StandardCharsets.UTF_8);

        // Zamijeni placeholdere sa stvarnim podacima (sa null-check)
        String html = htmlTemplate
                .replace("${decisionDate}", decisionData.getDecisionDate() != null ? decisionData.getDecisionDate() : "")
                .replace("${studentFullName}", decisionData.getStudentFullName() != null ? decisionData.getStudentFullName() : "")
                .replace("${chairmanFullName}", decisionData.getChairmanFullName() != null ? decisionData.getChairmanFullName() : "")
                .replace("${member1FullName}", decisionData.getMember1FullName() != null ? decisionData.getMember1FullName() : "")
                .replace("${member2FullName}", decisionData.getMember2FullName() != null ? decisionData.getMember2FullName() : "")
                .replace("${secretaryFullName}", decisionData.getSecretaryFullName() != null ? decisionData.getSecretaryFullName() : "");

        // Generiši PDF
        try (FileOutputStream os = new FileOutputStream(outputFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, getClass().getResource("/templates/").toString());

            // Registruj fontove
            String[] fontNames = {"LiberationSerif-Regular.ttf", "LiberationSerif-Bold.ttf",
                    "LiberationSerif-Italic.ttf", "LiberationSerif-BoldItalic.ttf"};
            for (String fontName : fontNames) {
                InputStream fontStream = getClass().getResourceAsStream("/fonts/" + fontName);
                if (fontStream != null) {
                    File tempFontFile = Files.createTempFile("font-", ".ttf").toFile();
                    Files.copy(fontStream, tempFontFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    builder.useFont(tempFontFile, "Liberation Serif");
                    fontStream.close();
                }
            }

            builder.toStream(os);
            builder.run();
        }
    }

    @FXML
    private void back() {
        // Vrati se nazad na detalje rada
        SceneManager.showWithData(
                "/app/thesisDetails.fxml",
                "Detalji završnog rada",
                (ThesisDetailsController controller) -> controller.initWithThesisId(thesisId)
        );
    }
}