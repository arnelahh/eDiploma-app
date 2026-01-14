package email;

import dao.CommissionDAO;
import dao.DocumentDAO;
import dao.ThesisDAO;
import dto.ThesisDetailsDTO;
import model.Commission;
import model.Document;
import model.DocumentStatus;
import utils.GlobalErrorHandler;

/**
 * Service za ručno slanje email notifikacija putem dugmeta
 */
public class DocumentEmailNotificationService {

    private final EmailService emailService = new EmailService();
    private final DocumentDAO documentDAO = new DocumentDAO();
    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();


    public boolean sendDocumentEmail(Document document) {
        try {
            if (document == null) {
                GlobalErrorHandler.error("Dokument nije pronađen.");
                return false;
            }

            if (document.getStatus() != DocumentStatus.READY) {
                GlobalErrorHandler.warning("Dokument još nije spreman za slanje. Status mora biti READY.");
                return false;
            }

            String base64Content = documentDAO.getContentBase64(document.getId());
            if (base64Content == null || base64Content.isEmpty()) {
                GlobalErrorHandler.error("Dokument nema sačuvan PDF sadržaj.");
                return false;
            }


            document.setContentBase64(base64Content);

            ThesisDetailsDTO thesisDetails = thesisDAO.getThesisDetails(document.getThesisId());

            if (thesisDetails == null) {
                GlobalErrorHandler.error("Podaci o radu nisu pronađeni.");
                return false;
            }

            boolean success = sendEmailByDocumentType(document, thesisDetails);

            if (success) {
                GlobalErrorHandler.info("✓ Email je uspješno poslan svim relevatnim osobama!");
            } else {
                GlobalErrorHandler.error("✗ Slanje emaila nije uspjelo. Provjerite App Password u podešavanjima.");
            }

            return success;

        } catch (Exception e) {
            System.err.println("Error sending document email: " + e.getMessage());
            e.printStackTrace();
            GlobalErrorHandler.error("Greška pri slanju emaila: " + e.getMessage());
            return false;
        }
    }


    private boolean sendEmailByDocumentType(Document document, ThesisDetailsDTO thesisDetails) {
        if (document.getDocumentType() == null) {
            GlobalErrorHandler.error("Tip dokumenta nije definisan.");
            return false;
        }

        String documentTypeName = document.getDocumentType().getName();

        return switch (documentTypeName) {
            case "Rješenje o izradi rada", "Rješenje o izradi završnog rada" ->
                    emailService.sendThesisDecisionDocument(document, thesisDetails);

            case "Rješenje o formiranju Komisije" -> {
                Commission commission = commissionDAO.getCommissionByThesisId(document.getThesisId());

                if (!validateCommission(commission, true)) {
                    yield false;
                }

                yield emailService.sendCommissionDecisionDocument(document, thesisDetails, commission);
            }

            case "Obavijest" -> {
                Commission commission = commissionDAO.getCommissionByThesisId(document.getThesisId());

                if (!validateCommission(commission, false)) {
                    yield false;
                }

                yield emailService.sendNoticeDocument(document, thesisDetails, commission);
            }

            case "Uvjerenje o završenom ciklusu" ->
                    emailService.sendCycleCompletionDocument(document, thesisDetails);

            default -> {
                GlobalErrorHandler.warning("Email slanje nije podržano za tip dokumenta: " + documentTypeName);
                yield false;
            }
        };
    }


    private boolean validateCommission(Commission commission, boolean requireMembers) {
        if (commission == null) {
            GlobalErrorHandler.error("Komisija nije pronađena za ovaj rad.");
            return false;
        }

        if (requireMembers) {
            if (commission.getMember1() == null || commission.getMember2() == null) {
                GlobalErrorHandler.error("Komisija nije kompletna (nedostaje predsjednik ili član).");
                return false;
            }
        } else {
            // Za Obavijest treba samo member1 (predsjednik)
            if (commission.getMember1() == null) {
                GlobalErrorHandler.error("Komisija mora biti formirana prije slanja obavijesti.");
                return false;
            }
        }

        return true;
    }
}
