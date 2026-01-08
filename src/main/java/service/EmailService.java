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
import java.util.List;
import java.util.Properties;

public class EmailService {

    private final EmailLogDAO emailLogDAO = new EmailLogDAO();

    /**
     * Šalje email sa PDF attachment-om koristeći trenutno prijavljenog korisnika
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

        String senderEmail = currentUser.getEmail();
        String encryptedAppPassword = currentUser.getAppPassword();

        System.out.println("[EmailService] Sender: " + senderEmail);
        System.out.println("[EmailService] Has App Password: " + (encryptedAppPassword != null && !encryptedAppPassword.isEmpty()));

        if (encryptedAppPassword == null || encryptedAppPassword.isEmpty()) {
            System.err.println("[EmailService] User does not have App Password configured.");
            logFailedEmail(currentUser.getId(), recipients, subject, "App Password not configured", documentId);
            return false;
        }

        try {
            // Dekriptuj App Password
            System.out.println("[EmailService] Decrypting App Password...");
            String appPassword = AESEncryption.decrypt(encryptedAppPassword);
            System.out.println("[EmailService] App Password decrypted successfully. Length: " + (appPassword != null ? appPassword.length() : 0));

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

            System.out.println("[EmailService] Recipients count: " + recipients.size());
            System.out.println("[EmailService] Has PDF: " + (pdfBytes != null && pdfBytes.length > 0));
            if (pdfBytes != null) {
                System.out.println("[EmailService] PDF size: " + pdfBytes.length + " bytes");
            }

            // Kreiranje i slanje emaila za svakog primaoca
            for (String recipient : recipients) {
                try {
                    System.out.println("[EmailService] Sending to: " + recipient);
                    
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(senderEmail));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
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
                        System.out.println("[EmailService] PDF attached: " + pdfFileName);
                    }

                    message.setContent(multipart);

                    Transport.send(message);

                    // Loguj uspješan email
                    logSuccessfulEmail(currentUser.getId(), recipient, subject, documentId);
                    
                    System.out.println("[EmailService] ✓ Email sent successfully to: " + recipient);

                } catch (MessagingException e) {
                    System.err.println("[EmailService] ✗ Failed to send email to: " + recipient);
                    System.err.println("[EmailService] Error: " + e.getMessage());
                    e.printStackTrace();
                    logFailedEmail(currentUser.getId(), List.of(recipient), subject, e.getMessage(), documentId);
                }
            }

            return true;

        } catch (Exception e) {
            System.err.println("[EmailService] Critical error sending emails: " + e.getMessage());
            e.printStackTrace();
            logFailedEmail(currentUser.getId(), recipients, subject, e.getMessage(), documentId);
            return false;
        }
    }

    /**
     * Šalje "Rješenje o izradi rada" studentu, mentoru i sekretaru
     * 
     * @param document Document objekat sa PDF-om u ContentBase64
     * @param thesisDetails ThesisDetailsDTO objekat sa informacijama o radu
     * @return true ako je uspješno poslano
     */
    public boolean sendThesisDecisionDocument(Document document, ThesisDetailsDTO thesisDetails) {
        try {
            System.out.println("[EmailService] sendThesisDecisionDocument called");
            
            // Validacija
            if (document == null || thesisDetails == null) {
                System.err.println("[EmailService] Document or ThesisDetails is null.");
                return false;
            }

            System.out.println("[EmailService] Document ID: " + document.getId());
            System.out.println("[EmailService] Thesis ID: " + thesisDetails.getId());

            if (document.getContentBase64() == null || document.getContentBase64().isEmpty()) {
                System.err.println("[EmailService] Document has no PDF content.");
                return false;
            }

            System.out.println("[EmailService] Base64 content length: " + document.getContentBase64().length());

            // Dekoduj Base64 PDF
            byte[] pdfBytes = Base64.getDecoder().decode(document.getContentBase64());
            System.out.println("[EmailService] PDF decoded. Size: " + pdfBytes.length + " bytes");

            // Pripremi primaioce
            Student student = thesisDetails.getStudent();
            AcademicStaff mentor = thesisDetails.getMentor();
            AcademicStaff secretary = thesisDetails.getSecretary();

            if (student == null || student.getEmail() == null) {
                System.err.println("[EmailService] Student email is missing.");
                return false;
            }

            List<String> recipients = new java.util.ArrayList<>();
            recipients.add(student.getEmail());
            System.out.println("[EmailService] Student email: " + student.getEmail());

            if (mentor != null && mentor.getEmail() != null) {
                recipients.add(mentor.getEmail());
                System.out.println("[EmailService] Mentor email: " + mentor.getEmail());
            }

            if (secretary != null && secretary.getEmail() != null) {
                recipients.add(secretary.getEmail());
                System.out.println("[EmailService] Secretary email: " + secretary.getEmail());
            }

            // Generiši email sadržaj
            String subject = "Rješenje o izradi diplomskog rada";
            String body = generateThesisDecisionEmailBody(thesisDetails);

            // Generiši filename
            String fileName = String.format("Rjesenje_o_izradi_rada_%s.pdf", 
                document.getDocumentNumber() != null ? document.getDocumentNumber() : document.getId());

            System.out.println("[EmailService] Calling sendEmailWithAttachment...");
            
            // Pošalji email
            return sendEmailWithAttachment(
                recipients, 
                subject, 
                body, 
                pdfBytes, 
                fileName, 
                document.getId()
            );

        } catch (Exception e) {
            System.err.println("[EmailService] Failed to send thesis decision document: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Generiše HTML body za "Rješenje o izradi rada" email
     */
    private String generateThesisDecisionEmailBody(ThesisDetailsDTO thesisDetails) {
        Student student = thesisDetails.getStudent();
        AcademicStaff mentor = thesisDetails.getMentor();
        
        String studentName = (student != null) 
            ? student.getFirstName() + " " + student.getLastName() 
            : "N/A";
        
        String mentorName = (mentor != null) 
            ? (mentor.getTitle() != null ? mentor.getTitle() + " " : "") + mentor.getFirstName() + " " + mentor.getLastName() 
            : "N/A";
        
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
     * Šalje email koristeći trenutno prijavljenog korisnika (bez attachment-a)
     */
    public boolean sendEmail(List<String> recipients, String subject, String body, Integer documentId) {
        return sendEmailWithAttachment(recipients, subject, body, null, null, documentId);
    }

    /**
     * Skraćena verzija za slanje jednom primaocu
     */
    public boolean sendEmail(String recipient, String subject, String body, Integer documentId) {
        return sendEmail(List.of(recipient), subject, body, documentId);
    }

    /**
     * Skraćena verzija bez documentId
     */
    public boolean sendEmail(List<String> recipients, String subject, String body) {
        return sendEmail(recipients, subject, body, null);
    }

    /**
     * Loguje uspješan email u bazu
     */
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

    /**
     * Loguje neuspješan email u bazu
     */
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

    /**
     * Test metoda za slanje test emaila
     */
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
