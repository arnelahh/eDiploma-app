package utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailValidator {

    public static boolean testGmailConnection(String email, String appPassword) {
        try {
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
                    return new PasswordAuthentication(email, appPassword);
                }
            });

            // Create test message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("eDiploma - Test konekcije");
            message.setText("Čestitamo! Vaš App Password je uspješno konfigurisan.\n\n" +
                    "Sada možete koristiti email notifikacije u eDiploma aplikaciji.\n\n" +
                    "Ova poruka je automatski generisana.");

            // Send message
            Transport.send(message);

            return true;

        } catch (MessagingException e) {
            System.err.println("Email test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
