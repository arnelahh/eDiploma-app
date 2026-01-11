package service;

import dao.EmailLogDAO;
import dto.ThesisDetailsDTO;
import model.*;
import utils.AESEncryption;
import utils.UserSession;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class EmailService {

    private final EmailLogDAO emailLogDAO = new EmailLogDAO();

    // ==================== HELPER METHODS ====================

    /**
     * Formatira ime akademskog osoblja sa titulom
     * @param staff AcademicStaff objekat
     * @return Formatirano ime (npr. "Prof. dr. Ime Prezime") ili "N/A"
     */
    private String formatStaffName(AcademicStaff staff) {
        if (staff == null) return "N/A";
        
        String title = (staff.getTitle() != null && !staff.getTitle().isBlank()) 
                ? staff.getTitle() + " " 
                : "";
        
        return title + staff.getFirstName() + " " + staff.getLastName();
    }

    /**
     * Formatira ime studenta
     * @param student Student objekat
     * @return Formatirano ime ili "N/A"
     */
    private String formatStudentName(Student student) {
        if (student == null) return "N/A";
        return student.getFirstName() + " " + student.getLastName();
    }

    /**
     * Dekodira Base64 PDF sadržaj i vraća byte array
     * @param document Document objekat sa Base64 sadržajem
     * @return byte[] PDF sadržaj ili null ako nema sadržaja
     */
    private byte[] decodePdfContent(Document document) {
        if (document == null || document.getContentBase64() == null || document.getContentBase64().isEmpty()) {
            return null;
        }
        
        try {
            return Base64.getDecoder().decode(document.getContentBase64());
        } catch (IllegalArgumentException e) {
            System.err.println("[EmailService] Failed to decode PDF content: " + e.getMessage());
            return null;
        }
    }

    /**
     * Dodaje email u Set ako nije null ili blank
     * @param recipients Set primaoca
     * @param email Email adresa
     */
    private void addIfPresent(Set<String> recipients, String email) {
        if (email != null && !email.isBlank()) {
            recipients.add(email);
        }
    }

    /**
     * Dodaje email akademskog osoblja u Set ako nije null
     * @param recipients Set primaoca
     * @param staff AcademicStaff objekat
     */
    private void addIfPresent(Set<String> recipients, AcademicStaff staff) {
        if (staff != null) {
            addIfPresent(recipients, staff.getEmail());
        }
    }

    /**
     * Generiše konzistentno ime PDF fajla
     * @param prefix Prefiks naziva fajla (npr. "Rjesenje_o_izradi_rada")
     * @param document Document objekat
     * @return Formatirano ime fajla sa .pdf ekstenzijom
     */
    private String generatePdfFileName(String prefix, Document document) {
        String identifier = (document.getDocumentNumber() != null && !document.getDocumentNumber().isBlank())
                ? document.getDocumentNumber()
                : String.valueOf(document.getId());
        
        return String.format("%s_%s.pdf", prefix, identifier);
    }

    // ==================== EMAIL SENDING METHODS ====================

    /**
     * Šalje email sa PDF attachment-om koristeći trenutno prijavljenog korisnika
     * NAPOMENA: Šalje JEDAN email sa svim primaocima u TO polju
     *
     * @param recipients Lista email adresa primaoca
     * @param subject Naslov emaila
     * @param body Sadržaj emaila (HTML)
     * @param pdfBytes PDF dokument kao byte array (može biti null)
     * @param pdfFileName Naziv PDF fajla
     * @param documentId Opcioni ID dokumenta koji je triggerovao email
     * @return true ako je email uspješno poslan
     */
    public boolean sendEmailWithAttachment(List<String> recipients, String subject, String body,
                                          byte[] pdfBytes, String pdfFileName, Integer documentId) {
        AppUser currentUser = UserSession.getUser();

        if (currentUser == null) {
            System.err.println("[EmailService] No user logged in. Cannot send email.");
            return false;
        }

        if (recipients == null || recipients.isEmpty()) {
            System.err.println("[EmailService] No recipients provided.");
            return false;
        }

        String senderEmail = currentUser.getEmail();
        String encryptedAppPassword = currentUser.getAppPassword();

        if (encryptedAppPassword == null || encryptedAppPassword.isEmpty()) {
            System.err.println("[EmailService] User does not have App Password configured.");
            logFailedEmail(currentUser.getId(), recipients, subject, "App Password not configured", documentId);
            return false;
        }

        try {
            String appPassword = AESEncryption.decrypt(encryptedAppPassword);

            // Konfigurisanje SMTP
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, appPassword);
                }
            });

            // Priprema liste primaoca kao comma-separated string
            String allRecipients = String.join(",", recipients);
            System.out.println("[EmailService] Sending to: " + allRecipients);

            try {
                // Kreiranje jedne poruke sa svim primaocima
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(allRecipients));
                message.setSubject(subject);

                // Kreiranje multipart poruke
                Multipart multipart = new MimeMultipart();

                // Body dio
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setContent(body, "text/html; charset=utf-8");
                multipart.addBodyPart(textPart);

                // Attachment dio (ako postoji)
                if (pdfBytes != null && pdfBytes.length > 0) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new ByteArrayDataSource(pdfBytes, "application/pdf");
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(pdfFileName);
                    multipart.addBodyPart(attachmentPart);
                }

                message.setContent(multipart);

                // Šalji JEDAN email sa svim primaocima
                Transport.send(message);

                // Loguj uspješan email za SVAKOG primaoca (za tracking)
                for (String recipient : recipients) {
                    logSuccessfulEmail(currentUser.getId(), recipient, subject, documentId);
                }

                System.out.println("[EmailService] ✓ Email sent successfully to all recipients");
                return true;

            } catch (MessagingException e) {
                System.err.println("[EmailService] ✗ Failed to send email");
                System.err.println("[EmailService] Error: " + e.getMessage());
                e.printStackTrace();
                
                // Loguj neuspjeh za sve primaoce
                logFailedEmail(currentUser.getId(), recipients, subject, e.getMessage(), documentId);
                return false;
            }

        } catch (Exception e) {
            System.err.println("[EmailService] Critical error sending emails: " + e.getMessage());
            e.printStackTrace();
            logFailedEmail(currentUser.getId(), recipients, subject, e.getMessage(), documentId);
            return false;
        }
    }

    /**
     * Šalje "Rješenje o izradi rada" studentu, mentoru i sekretaru
     */
    public boolean sendThesisDecisionDocument(Document document, ThesisDetailsDTO thesisDetails) {
        try {
            if (document == null || thesisDetails == null) {
                System.err.println("[EmailService] Document or ThesisDetails is null.");
                return false;
            }

            byte[] pdfBytes = decodePdfContent(document);
            if (pdfBytes == null) {
                System.err.println("[EmailService] Document has no PDF content.");
                return false;
            }

            Student student = thesisDetails.getStudent();
            AcademicStaff mentor = thesisDetails.getMentor();
            AcademicStaff secretary = thesisDetails.getSecretary();

            if (student == null || student.getEmail() == null) {
                System.err.println("[EmailService] Student email is missing.");
                return false;
            }

            Set<String> recipientsSet = new LinkedHashSet<>();
            addIfPresent(recipientsSet, student.getEmail());
            addIfPresent(recipientsSet, mentor);
            addIfPresent(recipientsSet, secretary);

            List<String> recipients = new java.util.ArrayList<>(recipientsSet);

            String subject = "Rješenje o izradi diplomskog rada";
            String body = generateThesisDecisionEmailBody(thesisDetails);
            String fileName = generatePdfFileName("Rjesenje_o_izradi_rada", document);

            return sendEmailWithAttachment(recipients, subject, body, pdfBytes, fileName, document.getId());

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send thesis decision document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Šalje "Rješenje o formiranju Komisije" studentu, mentoru, sekretaru + predsjedniku i članu komisije
     */
    public boolean sendCommissionDecisionDocument(Document document, ThesisDetailsDTO thesisDetails, Commission commission) {
        try {
            if (document == null || thesisDetails == null || commission == null) {
                System.err.println("[EmailService] Document/ThesisDetails/Commission is null.");
                return false;
            }

            byte[] pdfBytes = decodePdfContent(document);
            if (pdfBytes == null) {
                System.err.println("[EmailService] Document has no PDF content.");
                return false;
            }

            Student student = thesisDetails.getStudent();
            AcademicStaff mentor = thesisDetails.getMentor();
            AcademicStaff secretary = thesisDetails.getSecretary();
            AcademicStaff chairman = commission.getMember1();
            AcademicStaff member = commission.getMember2();

            if (student == null || student.getEmail() == null) {
                System.err.println("[EmailService] Student email is missing.");
                return false;
            }

            Set<String> recipientsSet = new LinkedHashSet<>();
            addIfPresent(recipientsSet, student.getEmail());
            addIfPresent(recipientsSet, mentor);
            addIfPresent(recipientsSet, secretary);
            addIfPresent(recipientsSet, chairman);
            addIfPresent(recipientsSet, member);

            List<String> recipients = new java.util.ArrayList<>(recipientsSet);

            String subject = "Rješenje o formiranju Komisije";
            String body = generateCommissionDecisionEmailBody(thesisDetails, commission);
            String fileName = generatePdfFileName("Rjesenje_o_formiranju_komisije", document);

            return sendEmailWithAttachment(recipients, subject, body, pdfBytes, fileName, document.getId());

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send commission decision document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NOVO: Šalje "Obavijest" studentu, mentoru, sekretaru i članovima komisije (Member1/2/3)
     */
    public boolean sendNoticeDocument(Document document, ThesisDetailsDTO thesisDetails, Commission commission) {
        try {
            if (document == null || thesisDetails == null || commission == null) {
                System.err.println("[EmailService] Document/ThesisDetails/Commission is null.");
                return false;
            }

            byte[] pdfBytes = decodePdfContent(document);
            if (pdfBytes == null) {
                System.err.println("[EmailService] Document has no PDF content.");
                return false;
            }

            Student student = thesisDetails.getStudent();
            AcademicStaff mentor = thesisDetails.getMentor();
            AcademicStaff secretary = thesisDetails.getSecretary();
            AcademicStaff chairman = commission.getMember1();
            AcademicStaff member = commission.getMember2();
            AcademicStaff substitute = commission.getMember3();

            if (student == null || student.getEmail() == null) {
                System.err.println("[EmailService] Student email is missing.");
                return false;
            }

            Set<String> recipientsSet = new LinkedHashSet<>();
            addIfPresent(recipientsSet, student.getEmail());
            addIfPresent(recipientsSet, mentor);
            addIfPresent(recipientsSet, secretary);
            addIfPresent(recipientsSet, chairman);
            addIfPresent(recipientsSet, member);
            addIfPresent(recipientsSet, substitute);

            List<String> recipients = new java.util.ArrayList<>(recipientsSet);

            String subject = "Obavijest o terminu završnog rada";
            String body = generateNoticeEmailBody(thesisDetails, commission);
            String fileName = generatePdfFileName("Obavijest", document);

            return sendEmailWithAttachment(recipients, subject, body, pdfBytes, fileName, document.getId());

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send notice document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NOVO: Šalje "Uvjerenje o završenom ciklusu" isključivo studentu
     */
    public boolean sendCycleCompletionDocument(Document document, ThesisDetailsDTO thesisDetails) {
        try {
            if (document == null || thesisDetails == null) {
                System.err.println("[EmailService] Document or ThesisDetails is null.");
                return false;
            }

            Student student = thesisDetails.getStudent();
            if (student == null || student.getEmail() == null || student.getEmail().isBlank()) {
                System.err.println("[EmailService] Student email is missing.");
                return false;
            }

            byte[] pdfBytes = decodePdfContent(document);
            if (pdfBytes == null) {
                System.err.println("[EmailService] Document has no PDF content.");
                return false;
            }

            String subject = "Uvjerenje o završenom ciklusu";
            String body = generateCycleCompletionEmailBody(thesisDetails);
            String fileName = generatePdfFileName("Uvjerenje_o_zavrsenom_ciklusu", document);

            return sendEmailWithAttachment(List.of(student.getEmail()), subject, body, pdfBytes, fileName, document.getId());

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send cycle completion document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ==================== HTML EMAIL BODY GENERATORS ====================

    /**
     * Generiše HTML body za "Rješenje o izradi rada" email
     */
    private String generateThesisDecisionEmailBody(ThesisDetailsDTO thesisDetails) {
        Student student = thesisDetails.getStudent();
        AcademicStaff mentor = thesisDetails.getMentor();

        String studentName = formatStudentName(student);
        String mentorName = formatStaffName(mentor);
        String thesisTitle = thesisDetails.getTitle() != null ? thesisDetails.getTitle() : "N/A";

        String approvalDate = "";
        if (thesisDetails.getApprovalDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            approvalDate = thesisDetails.getApprovalDate().format(formatter);
        }

        return String.format("""
                        <html>
                        <body style="font-family: Arial, sans-serif; color: #333;">
                            <h2 style="color: #2c3e50;">Rješenje o izradi diplomskog rada</h2>

                            <p>Poštovani/a,</p>

                            <p>U prilogu se nalazi dokument <strong>Rješenja o izradi diplomskog rada</strong>.</p>

                            <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0;">
                                <p style="margin: 5px 0;"><strong>Student:</strong> %s</p>
                                <p style="margin: 5px 0;"><strong>Mentor:</strong> %s</p>
                                <p style="margin: 5px 0;"><strong>Naslov rada:</strong> %s</p>
                                %s
                            </div>

                            <p>Molimo vas da dokument pregledate i sačuvate za svoje evidencije.</p>

                            <br>
                            <p style="color: #7f8c8d; font-size: 12px;">Srdačan pozdrav,<br>Studentska služba<br><i>Ova poruka je automatski generisana iz eDiploma sistema.</i></p>
                        </body>
                        </html>
                        """,
                studentName,
                mentorName,
                thesisTitle,
                !approvalDate.isEmpty() ? "<p style=\"margin: 5px 0;\"><strong>Datum odobrenja:</strong> " + approvalDate + "</p>" : ""
        );
    }

    /**
     * Generiše HTML body za "Rješenje o formiranju Komisije" email
     */
    private String generateCommissionDecisionEmailBody(ThesisDetailsDTO thesisDetails, Commission commission) {
        Student student = thesisDetails.getStudent();
        AcademicStaff mentor = thesisDetails.getMentor();
        AcademicStaff chairman = commission != null ? commission.getMember1() : null;
        AcademicStaff member = commission != null ? commission.getMember2() : null;

        String studentName = formatStudentName(student);
        String mentorName = formatStaffName(mentor);
        String chairmanName = formatStaffName(chairman);
        String memberName = formatStaffName(member);
        String thesisTitle = thesisDetails.getTitle() != null ? thesisDetails.getTitle() : "N/A";

        return String.format("""
                        <html>
                        <body style="font-family: Arial, sans-serif; color: #333;">
                            <h2 style="color: #2c3e50;">Rješenje o formiranju Komisije</h2>

                            <p>Poštovani/a,</p>

                            <p>U prilogu se nalazi dokument <strong>Rješenja o formiranju Komisije</strong>.</p>

                            <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #8e44ad; margin: 20px 0;">
                                <p style="margin: 5px 0;"><strong>Student:</strong> %s</p>
                                <p style="margin: 5px 0;"><strong>Mentor:</strong> %s</p>
                                <p style="margin: 5px 0;"><strong>Naslov rada:</strong> %s</p>
                                <hr style="border: none; border-top: 1px solid #ddd; margin: 12px 0;">
                                <p style="margin: 5px 0;"><strong>Predsjednik komisije:</strong> %s</p>
                                <p style="margin: 5px 0;"><strong>Član komisije:</strong> %s</p>
                            </div>

                            <p>Molimo vas da dokument pregledate i sačuvate za svoje evidencije.</p>

                            <br>
                            <p style="color: #7f8c8d; font-size: 12px;">Srdačan pozdrav,<br>Studentska služba<br><i>Ova poruka je automatski generisana iz eDiploma sistema.</i></p>
                        </body>
                        </html>
                        """,
                studentName,
                mentorName,
                thesisTitle,
                chairmanName,
                memberName
        );
    }

    /**
     * NOVO: Generiše HTML body za "Obavijest" email
     */
    private String generateNoticeEmailBody(ThesisDetailsDTO thesisDetails, Commission commission) {
        Student student = thesisDetails.getStudent();
        AcademicStaff mentor = thesisDetails.getMentor();
        AcademicStaff chairman = commission != null ? commission.getMember1() : null;
        AcademicStaff member = commission != null ? commission.getMember2() : null;
        AcademicStaff substitute = commission != null ? commission.getMember3() : null;

        String studentName = formatStudentName(student);
        String mentorName = formatStaffName(mentor);
        String chairmanName = formatStaffName(chairman);
        String memberName = formatStaffName(member);
        String substituteName = formatStaffName(substitute);
        String thesisTitle = thesisDetails.getTitle() != null ? thesisDetails.getTitle() : "N/A";

        String defenseDate = "";
        if (thesisDetails.getDefenseDate() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            defenseDate = thesisDetails.getDefenseDate().format(formatter);
        }

        String substituteLine = (substitute != null) ? "<p style=\"margin: 5px 0;\"><strong>Zamjenski član:</strong> " + substituteName + "</p>" : "";

        return String.format("""
                        <html>
                        <body style="font-family: Arial, sans-serif; color: #333;">
                            <h2 style="color: #2c3e50;">Obavijest o terminu završnog rada</h2>

                            <p>Poštovani/a,</p>

                            <p>U prilogu se nalazi dokument <strong>Obavijesti o terminu završnog rada</strong>.</p>

                            <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #16a085; margin: 20px 0;">
                                <p style="margin: 5px 0;"><strong>Student:</strong> %s</p>
                                <p style="margin: 5px 0;"><strong>Mentor:</strong> %s</p>
                                <p style="margin: 5px 0;"><strong>Naslov rada:</strong> %s</p>
                                %s
                                <hr style="border: none; border-top: 1px solid #ddd; margin: 12px 0;">
                                <p style="margin: 5px 0;"><strong>Predsjednik komisije:</strong> %s</p>
                                <p style="margin: 5px 0;"><strong>Član komisije:</strong> %s</p>
                                %s
                            </div>

                            <p>Molimo vas da dokument pregledate i sačuvate za svoje evidencije.</p>

                            <br>
                            <p style="color: #7f8c8d; font-size: 12px;">Srdačan pozdrav,<br>Studentska služba<br><i>Ova poruka je automatski generisana iz eDiploma sistema.</i></p>
                        </body>
                        </html>
                        """,
                studentName,
                mentorName,
                thesisTitle,
                !defenseDate.isEmpty() ? "<p style=\"margin: 5px 0;\"><strong>Datum odbrane:</strong> " + defenseDate + "</p>" : "",
                chairmanName,
                memberName,
                substituteLine
        );
    }

    /**
     * NOVO: Generiše HTML body za "Uvjerenje o završenom ciklusu" email
     */
    private String generateCycleCompletionEmailBody(ThesisDetailsDTO thesisDetails) {
        Student student = thesisDetails.getStudent();
        String studentName = formatStudentName(student);
        String thesisTitle = thesisDetails.getTitle() != null ? thesisDetails.getTitle() : "N/A";

        return String.format("""
                        <html>
                        <body style="font-family: Arial, sans-serif; color: #333;">
                            <h2 style="color: #2c3e50;">Uvjerenje o završenom ciklusu</h2>

                            <p>Poštovani/a %s,</p>

                            <p>U prilogu se nalazi Vaše <strong>Uvjerenje o završenom ciklusu</strong>.</p>

                            <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #2980b9; margin: 20px 0;">
                                <p style="margin: 5px 0;"><strong>Naslov rada:</strong> %s</p>
                            </div>

                            <p>Molimo vas da dokument sačuvate za svoje evidencije.</p>

                            <br>
                            <p style="color: #7f8c8d; font-size: 12px;">Srdačan pozdrav,<br>Studentska služba<br><i>Ova poruka je automatski generisana iz eDiploma sistema.</i></p>
                        </body>
                        </html>
                        """,
                studentName,
                thesisTitle
        );
    }

    // ==================== SIMPLE EMAIL METHODS ====================

    /**
     * Šalje email koristeći trenutno prijavljenog korisnika (bez attachment-a)
     */
    public boolean sendEmail(List<String> recipients, String subject, String body, Integer documentId) {
        return sendEmailWithAttachment(recipients, subject, body, null, null, documentId);
    }

    public boolean sendEmail(String recipient, String subject, String body, Integer documentId) {
        return sendEmail(List.of(recipient), subject, body, documentId);
    }

    public boolean sendEmail(List<String> recipients, String subject, String body) {
        return sendEmail(recipients, subject, body, null);
    }

    // ==================== LOGGING METHODS ====================

    private void logSuccessfulEmail(int userId, String recipient, String subject, Integer documentId) {
        try {
            EmailLog log = new EmailLog(
                    userId,
                    recipient,
                    subject,
                    "SUCCESS",
                    null,
                    LocalDateTime.now(),
                    documentId
            );
            emailLogDAO.logEmail(log);
        } catch (Exception e) {
            System.err.println("Failed to log successful email: " + e.getMessage());
        }
    }

    private void logFailedEmail(int userId, List<String> recipients, String subject, String errorMessage, Integer documentId) {
        try {
            for (String recipient : recipients) {
                EmailLog log = new EmailLog(
                        userId,
                        recipient,
                        subject,
                        "FAILED",
                        errorMessage,
                        LocalDateTime.now(),
                        documentId
                );
                emailLogDAO.logEmail(log);
            }
        } catch (Exception e) {
            System.err.println("Failed to log failed email: " + e.getMessage());
        }
    }

    // ==================== TEST EMAIL METHOD ====================

    public boolean sendTestEmail() {
        AppUser currentUser = UserSession.getUser();
        if (currentUser == null) return false;

        String subject = "eDiploma - Test Email";
        String body = """
                <h2>Test Email</h2>
                <p>Ovo je test email iz eDiploma aplikacije.</p>
                <p>Vaš email sistem je uspješno konfigurisan!</p>
                <br>
                <p><i>Ova poruka je automatski generisana.</i></p>
                """;

        return sendEmail(currentUser.getEmail(), subject, body, null);
    }
}
