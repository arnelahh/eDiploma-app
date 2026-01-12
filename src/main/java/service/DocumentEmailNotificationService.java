package service;

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

    /**
     * Šalje email za dokument - poziva se iz kontrolera kada korisnik klikne na dugme
     * 
     * @param document Document objekat (može biti proslijeđen direktno iz kartice)
     * @return true ako je uspješno poslano
     */
    public boolean sendDocumentEmail(Document document) {
        try {
            // Validacija
            if (document == null) {
                GlobalErrorHandler.error("Dokument nije pronađen.");
                return false;
            }

            // Provjeri status - mora biti READY
            if (document.getStatus() != DocumentStatus.READY) {
                GlobalErrorHandler.warning("Dokument još nije spreman za slanje. Status mora biti READY.");
                return false;
            }

            // VAŽNO: Eksplicitno povuci PDF content iz baze
            String base64Content = documentDAO.getContentBase64(document.getId());
            if (base64Content == null || base64Content.isEmpty()) {
                GlobalErrorHandler.error("Dokument nema sačuvan PDF sadržaj.");
                return false;
            }

            // Postavi content na document objekat
            document.setContentBase64(base64Content);

            // Povuci thesis informacije sa svim detaljima
            ThesisDetailsDTO thesisDetails = thesisDAO.getThesisDetails(document.getThesisId());

            if (thesisDetails == null) {
                GlobalErrorHandler.error("Podaci o radu nisu pronađeni.");
                return false;
            }

            // Šalji email na osnovu tipa dokumenta
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

    /**
     * Rutira slanje emaila na osnovu tipa dokumenta
     */
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

    /**
     * Validira Commission objekat i članove
     * 
     * @param commission Commission objekat za validaciju
     * @param requireMembers Da li se zahtijeva postojanje member1 i member2
     * @return true ako je komisija validna, false inače
     */
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
