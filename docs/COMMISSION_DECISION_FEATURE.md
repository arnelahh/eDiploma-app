# Rješenje o formiranju Komisije za Diplomski rad

## Pregled

Ova funkcionalnost omogućava automatsko generisanje PDF dokumenta "Rješenje o formiranju Komisije za Diplomski rad" na osnovu podataka iz baze. Dokument se generiše sa svim potrebnim podacima bez potrebe za ručnim unosom broja rješenja.

## Kreirani/Izmijenjeni fajlovi

### 1. DTO klasa
- **Putanja**: `src/main/java/dto/CommissionDecisionDTO.java`
- **Svrha**: Sadrži podatke potrebne za generisanje dokumenta
- **Polja**:
  - `decisionDate` - Datum rješenja (automatski iz baze)
  - `studentFullName` - Ime i prezime studenta
  - `thesisTitle` - Naziv diplomskog rada
  - `chairmanFullName` - Predsjednik komisije
  - `member1FullName` - Član 1
  - `member2FullName` - Član 2 (Mentor)
  - `secretaryFullName` - Sekretar komisije

### 2. Controller
- **Putanja**: `src/main/java/controller/CommissionDecisionController.java`
- **Svrha**: Upravlja formom i generisanjem PDF-a
- **Ključne metode**:
  - `initWithThesisId(int thesisId)` - Inicijalizuje controller sa ID-jem rada
  - `loadData()` - Učitava podatke iz baze
  - `handleDownloadPDF()` - Generiše i snima PDF
  - `generatePDF(File outputFile)` - Kreira PDF iz HTML template-a

### 3. HTML Template
- **Putanja**: `src/main/resources/templates/commission_decision_template.html`
- **Svrha**: Template za PDF dokument
- **Dizajn**: Identican sa zvanicnim dokumentom fakulteta
- **Sekcije**:
  - Zaglavlje sa nazivom fakulteta, službom i datumom
  - Pravni osnov (Statut, Pravilnik)
  - Naslov rješenja
  - Članovi (1-4) sa detaljima komisije i zadacima
  - Dostavljeno sekcija
  - Prostor za potpis dekana

### 4. FXML Forma
- **Putanja**: `src/main/resources/app/commissionDecision.fxml`
- **Svrha**: Korisnički interfejs za pregled podataka prije generisanja
- **Elementi**:
  - Prikaz podataka o studentu i radu
  - Prikaz datuma rješenja
  - Prikaz sastava komisije
  - Dugmad za generisanje PDF-a i povratak

### 5. Integracija u ThesisDetailsController
- **Putanja**: `src/main/java/controller/ThesisDetailsController.java`
- **Izmjene**: Dodata metoda `handleOpenCommissionDecision()`
- **Validacija**: Provjerava da li je komisija formirana prije otvaranja forme

### 6. Integracija u thesisDetails.fxml
- **Putanja**: `src/main/resources/app/thesisDetails.fxml`
- **Izmjene**: Povezana dugmad (prikaz 👁 i uređivanje ✏) za "Rješenje o formiranju Komisije" sa `handleOpenCommissionDecision` akcijom

## Kako koristiti

### Pristup funkcionalnosti

1. Otvori detalje završnog rada
2. U sekciji "Dokumenti" pronađi "Rješenje o formiranju Komisije"
3. Klikni na dugme 👁 (pregled) ili ✏ (uredi)

**Napomena**: Dugmad su aktivna samo ako je komisija već formirana za dati rad.

## Tok rada

1. Korisnik otvara detalje završnog rada
2. Klikne na dugme za "Rješenje o formiranju Komisije" (👁 ili ✏)
3. Sistem provjerava da li je komisija formirana:
   - Ako nije → Prikazuje se poruka greške
   - Ako jeste → Otvara se forma sa automatski popunjenim podacima
4. Forma prikazuje:
   - Ime i prezime studenta
   - Naziv diplomskog rada
   - Datum rješenja (automatski iz baze)
   - Sve članove komisije (predsjednik, član 1, član 2/mentor, sekretar)
5. Korisnik klikne "Generiši i preuzmi PDF"
6. Bira lokaciju gdje će snimiti PDF fajl
7. PDF se generiše i automatski snima
8. Prikazuje se potvrda o uspješnom snimanju

## Preduslov

Za uspješno generisanje dokumenta potrebno je da:
- Završni rad postoji u bazi
- **Komisija je formirana** (ima sve članove)
- Postoje podaci o sekretaru
- Postoji datum odobravanja ili aplikacije

## Validacija

### Pri otvaranju forme:
- Provjerava se da li je komisija formirana
- Ako nije, prikazuje se poruka: "Komisija mora biti formirana prije kreiranja rješenja."

### Pri generisanju PDF-a:
- Svi podaci se automatski preuzimaju iz baze
- Nema potrebe za dodatnom validacijom

## Izgled dokumenta

Dokument sadrži (identican dizajn kao zvanični dokument):

- **Zaglavlje**: 
  - "POLITEHNIČKI FAKULTET" (bold, centrirano, uppercase)
  - "Studentska služba"
  - "Datum: DD.MM.GGGG."
  
- **Pravni osnov**: 
  - "Na osnovu člana 206. Statuta Univerziteta u Zenici, a u skladu sa članovima 45 i 46. Pravilnika o organizovanju dodiplomskog, magistarskog i doktorskog studija na Univerzitetu u Zenici, dekan Fakulteta donosi"
  
- **Naslov**:
  - "RJEŠENJE" (bold, centrirano)
  - "o formiranju Komisije za Diplomski rad"
  
- **Sadržaj** (Članovi 1-4):
  - **Član 1**: "Formira se Komisija za polaganje diplomskog ispita studenta [IME PREZIME]."
  - **Član 2**: "U Komisiju iz prethodne tačke imenuju se:"
    1. Predsjednik Komisije
    2. Član Komisije
    3. Član Komisije i Mentor
    4. Sekretar Komisije
  - **Član 3**: Zadatak komisije (pregled rada, odluka o pismenom dijelu, određivanje usmenog dijela)
  - **Član 4**: "Ovo rješenje stupa na snagu danom donošenja."
  
- **Dostavljeno**: 
  - Komisiji
  - Studentskoj službi
  - a/a
  
- **Potpis**:
  - Linija za potpis
  - "Dekan"

## Formatiranje

Dokument koristi:
- **Font**: Times New Roman (Liberation Serif)
- **Veličina papira**: A4
- **Margine**: 15mm gore/dole, 20mm lijevo/desno
- **Vodeni žig**: Logo fakulteta u pozadini (pozadina_2.png)
- **Stil**: Službeni dokument sa jasnom strukturom i numeraćijom članova
- **Font sizes**: 12pt - osnovni tekst, 14pt - naslov fakulteta, 13pt - RJEŠENJE

## Testiranje

### Test scenario 1: Komisija nije formirana
1. Otvori detalje rada gdje komisija nije formirana
2. Pokušaj kliknuti na dugme za rješenje
3. **Očekivano**: Poruka greške "Komisija mora biti formirana..."

### Test scenario 2: Uspješno generisanje
1. Otvori detalje rada sa formiranom komisijom
2. Klikni na dugme za rješenje (👁 ili ✏)
3. Provjeri da li su svi podaci korektno učitani
4. Klikni "Generiši i preuzmi PDF"
5. Izaberi lokaciju za snimanje
6. **Očekivano**: 
   - PDF se kreira
   - Prikazuje se poruka "PDF je uspešno sačuvan!"
   - Dokument sadrži sve podatke korektno formatiran
   - Izgled je identican zvaničnom dokumentu

### Šta provjeriti u generisanom PDF-u:
- [ ] Tačnost svih podataka (student, datum, komisija)
- [ ] Formatiranje teksta (bold, razmaci, poravnanje)
- [ ] Vodeni žig (da li je vidljiv u pozadini)
- [ ] Font rendering (da li su sva slova pravilno prikazana)
- [ ] Datum u zaglavlju
- [ ] Svi članovi komisije sa pravilnim ulogama
- [ ] Član struktura (1-4) sa pravilnim formatiranjem
- [ ] Dostavljeno sekcija
- [ ] Potpis sekcija sa linijom

## Poređenje sa originalnim dokumentom

Dokument je dizajniran da bude **identican** zvaničnom dokumentu:
- Isto zaglavlje i struktura
- Isti raspored članova
- Isti font i veličine teksta
- Ista margine i razmaci
- Vodeni žig fakulteta u pozadini

## Buduća poboljašnja

- [ ] Mogućnost dodavanja broja rješenja (opciono)
- [ ] Predpregled PDF-a prije snimanja
- [ ] Email funkcionalnost za slanje dokumenta članovima komisije
- [ ] Digitalni potpis dekana
- [ ] Verzionisanje dokumenata (čuvanje istorije)
- [ ] Eksport u druge formate (Word, HTML)
- [ ] Automatsko arhiviranje dokumenata

## Greške i rješavanje problema

### Problem: "Template file not found!"
**Uzrok**: HTML template nije pronađen u resources  
**Rješenje**: Provjeriti da li postoji `src/main/resources/templates/commission_decision_template.html`

### Problem: "Font file not found in resources"
**Uzrok**: Liberation Serif fontovi nisu dostupni  
**Rješenje**: Provjeriti da li postoje fontovi u `src/main/resources/fonts/`:
- LiberationSerif-Regular.ttf
- LiberationSerif-Bold.ttf
- LiberationSerif-Italic.ttf
- LiberationSerif-BoldItalic.ttf

### Problem: Podaci se ne prikazuju korektno
**Uzrok**: Null vrijednosti ili pogrešna konekcija sa bazom  
**Rješenje**: 
1. Provjeriti konzolu za SQL greške
2. Provjeriti da li su sve relacije u bazi pravilno postavljene
3. Provjeriti `ThesisDAO.getThesisById()` i `CommissionDAO.getCommissionByThesisId()`

### Problem: Vodeni žig se ne prikazuje
**Uzrok**: Fajl pozadina_2.png nije pronađen  
**Rješenje**: Provjeriti da li postoji `src/main/resources/templates/pozadina_2.png`

## Podrška

Za dodatna pitanja ili probleme, kontaktirajte programera ili konsultujte:
- GitHub Issues na repository-u
- Internu dokumentaciju projekta
- Tehniku podršku
