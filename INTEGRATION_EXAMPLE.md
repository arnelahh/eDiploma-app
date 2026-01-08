# Integracija Send Email Dugmeta u Kontroler

## 📋 Pregled

Ova dokumentacija pokazuje kako integrirati novo **Send Email** dugme u kontroler koji koristi `DocumentCardFactory`.

---

## 🔧 Korak 1: Import potrebnih klasa u kontroler

Dodaj ove importove na početak kontrolera:

```java
import service.DocumentEmailNotificationService;
```

---

## 🔧 Korak 2: Inicijalizuj servis u kontroleru

Dodaj field u kontroler:

```java
private final DocumentEmailNotificationService emailNotificationService = new DocumentEmailNotificationService();
```

---

## 🔧 Korak 3: Dodaj onSendEmail akciju u Actions

Kada kreiraš `DocumentCardFactory.Actions` objekat, dodaj `onSendEmail` akciju:

### PRIJE:

```java
DocumentCardFactory.Actions actions = new DocumentCardFactory.Actions();
actions.onDownload = this::handleDownloadDocument;
actions.onEdit = this::handleEditDocument;
```

### POSLIJE:

```java
DocumentCardFactory.Actions actions = new DocumentCardFactory.Actions();
actions.onDownload = this::handleDownloadDocument;
actions.onEdit = this::handleEditDocument;
actions.onSendEmail = this::handleSendEmailDocument;  // NOVO
```

---

## 🔧 Korak 4: Implementiraj handleSendEmailDocument metodu

Dodaj ovu metodu u svoj kontroler:

```java
private void handleSendEmailDocument(Document document) {
    if (document == null) {
        GlobalErrorHandler.error("Dokument nije pronađen.");
        return;
    }

    // Prikaži confirmation dialog
    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Pošalji Email");
    confirmation.setHeaderText("Da li ste sigurni da želite poslati email?");
    confirmation.setContentText(
        "Email će biti poslan studentu, mentoru i sekretaru.\n" +
        "Tip dokumenta: " + (document.getDocumentType() != null ? document.getDocumentType().getName() : "N/A")
    );

    Optional<ButtonType> result = confirmation.showAndWait();
    
    if (result.isPresent() && result.get() == ButtonType.OK) {
        // Pošalji email
        emailNotificationService.sendDocumentEmail(document);
    }
}
```

---

## 📝 Kompletan Primjer

Evo kompletnog primjera kako izgleda integrisani kontroler:

```java
import Factory.DocumentCardFactory;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import model.Document;
import service.DocumentEmailNotificationService;
import utils.GlobalErrorHandler;

import java.util.Optional;

public class ThesisDetailsController {

    private final DocumentEmailNotificationService emailNotificationService = new DocumentEmailNotificationService();

    // ... ostali fieldovi ...

    private void loadDocuments() {
        // Kreiranje Actions objekta
        DocumentCardFactory.Actions actions = new DocumentCardFactory.Actions();
        actions.onDownload = this::handleDownloadDocument;
        actions.onEdit = this::handleEditDocument;
        actions.onSendEmail = this::handleSendEmailDocument; // NOVO

        // ... kreiranje document cards ...
    }

    private void handleDownloadDocument(Document document) {
        // Postojeća logika za download
    }

    private void handleEditDocument(DocumentType type) {
        // Postojeća logika za edit
    }

    private void handleSendEmailDocument(Document document) {
        if (document == null) {
            GlobalErrorHandler.error("Dokument nije pronađen.");
            return;
        }

        // Prikaži confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Pošalji Email");
        confirmation.setHeaderText("Da li ste sigurni da želite poslati email?");
        confirmation.setContentText(
            "Email će biti poslan studentu, mentoru i sekretaru.\n" +
            "Tip dokumenta: " + (document.getDocumentType() != null ? document.getDocumentType().getName() : "N/A")
        );

        Optional<ButtonType> result = confirmation.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Pošalji email
            emailNotificationService.sendDocumentEmail(document);
        }
    }
}
```

---

## ✅ Behaviour dugmeta

### Kada je dugme omogućeno:
- ✅ Dokument postoji (nije `null`)
- ✅ Status dokumenta je `READY`
- ✅ Dokument nije blokiran po redosljedu

### Kada je dugme onemogućeno:
- ❌ Dokument ne postoji
- ❌ Status dokumenta nije `READY` (npr. `IN_PROGRESS`)
- ❌ Dokument je blokiran po redosljedu (prethodni nije ready)

---

## 📧 Šta se dešava kada se klikne dugme:

1. **Validation**: Provjerava se da li je dokument READY
2. **Fetch data**: Povlače se podaci o thesis-u, studentu, mentoru
3. **Email generation**: Generiše se HTML email sa attachment-om (PDF)
4. **Send**: Šalje se email:
   - **Primaoci**: Student + Mentor + Sekretar
   - **Attachment**: PDF dokument iz baze
   - **Subject**: Naslov zavisi od tipa dokumenta
5. **Logging**: Svaki poslat email se loguje u `EmailLog` tabelu
6. **Feedback**: Korisniku se prikaže poruka o uspjehu/neuspjehu

---

## 🎨 Izgled Dugmeta

Dugme ima:
- **Emoji**: 📧 (email ikona)
- **Pozicija**: Prvo (lijevo), prije Download i Edit dugmadi
- **Style**: `document-icon-btn` (isti kao ostala dugmad)
- **Tooltip**: Možeš dodati `Tooltip.install()` za dodatni hint

---

## 🔍 Debugging

Ako email ne radi, provjeri:

1. **App Password**: Da li korisnik ima konfigurisan App Password?
   ```java
   AppUser user = UserSession.getUser();
   System.out.println("App Password: " + (user.getAppPassword() != null));
   ```

2. **Document Status**: Da li je dokument READY?
   ```java
   System.out.println("Document Status: " + document.getStatus());
   ```

3. **Email Addresses**: Da li su emailovi postavljeni?
   ```java
   System.out.println("Student Email: " + thesis.getStudent().getEmail());
   System.out.println("Mentor Email: " + thesis.getMentor().getEmail());
   ```

4. **EmailLog tabela**: Provjeri logove u bazi
   ```sql
   SELECT * FROM EmailLog ORDER BY SentAt DESC LIMIT 10;
   ```

---

## 🚀 Dodavanje Novih Tipova Dokumenata

Da dodaš slanje emaila za druge tipove dokumenata:

1. Dodaj novu metodu u `EmailService.java`:
   ```java
   public boolean sendCommissionDocument(Document document, Thesis thesis) {
       // Slično kao sendThesisDecisionDocument
   }
   ```

2. Ažuriraj switch u `DocumentEmailNotificationService.java`:
   ```java
   case "Komisija" -> emailService.sendCommissionDocument(document, thesis);
   ```

Sve ostalo radi automatski! 🎉
