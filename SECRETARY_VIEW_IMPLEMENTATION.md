# Secretary View Implementation

Ova implementacija dodaje odvojenu funkcionalnost za **Sekretara** u eDiploma aplikaciji.

## Šta je urađeno?

### 1. **Novi fajlovi**

#### `SecretaryDashboardController.java`
- Kontroler za dashboard sekretara
- Automatski učitava stranicu sa završnim radovima pri pokretanju
- Podržava samo funkcionalnost "Završni radovi" (bez pristupa studentima, mentorima, sekretarima)

#### `secretary-dashboard.fxml`
- FXML za dashboard sekretara
- Pojednostavljena navigacija sa samo jednom opcijom: "Završni radovi"
- Vizualno označen kao "eDiploma - Sekretar" u header-u

### 2. **Modifikovani fajlovi**

#### `LoginController.java`
- Dodata logika za rutiranje korisnika na osnovu `UserRole`
- **SECRETARY** korisnici → `secretary-dashboard.fxml`
- **ADMINISTRATOR** (i ostali) → `dashboard.fxml`

#### `ThesisDAO.java`
- Dodata nova metoda: `getThesisBySecretaryId(int secretaryUserId)`
- Filtrira radove po `SecretaryId` koloni u bazi
- Vraća samo radove dodijeljene određenom sekretaru

#### `ThesisController.java`
- Modifikovan `loadThesises()` metod
- Provjerava `UserRole` ulogovanog korisnika
- **SECRETARY** → poziva `dao.getThesisBySecretaryId(userId)`
- **Ostali korisnici** → poziva `dao.getAllThesis()`

#### `ThesisDetailsController.java`
- Modifikovan `back()` metod
- Provjerava tip korisnika i vraća ga na odgovarajući dashboard
- **SECRETARY** → `/app/secretary-dashboard.fxml`
- **Ostali korisnici** → `/app/dashboard.fxml`

#### `ThesisFormController.java`
- Dodata `returnToDashboard()` pomocna metoda
- Zamijenjeni svi hardkodovani pozivi `SceneManager.show("/app/dashboard.fxml", ...)` sa `returnToDashboard()`
- Metoda provjerava tip korisnika i vraća sekretara na secretary-dashboard

## Kako radi?

### Tok rada za Sekretara:

1. **Login**: Sekretar se loguje sa svojim kredencijalima
2. **Rutiranje**: `LoginController` detektuje da je `role = "SECRETARY"`
3. **Dashboard**: Otvara se `secretary-dashboard.fxml`
4. **Auto-load**: `SecretaryDashboardController` automatski učitava `thesis.fxml`
5. **Filtriranje**: `ThesisController` poziva `getThesisBySecretaryId()` umjesto `getAllThesis()`
6. **Prikaz**: Sekretar vidi **samo radove gdje je `thesis.SecretaryId = appuser.Id`**
7. **Navigacija**: Pri povratku iz detalja ili forme rada, sekretar se uvijek vraća na `secretary-dashboard.fxml`

### Tok rada za Administratora:

1. **Login**: Administrator se loguje
2. **Rutiranje**: `LoginController` detektuje da je `role = "ADMINISTRATOR"`
3. **Dashboard**: Otvara se `dashboard.fxml` (originalni, sa svim opcijama)
4. **Prikaz**: Administrator vidi **sve radove** (čitav sistem)
5. **Navigacija**: Pri povratku, administrator se vraća na `dashboard.fxml`

## Rješeni problemi

### Problem: Sekretar vidi sve opcije nakon povratka iz detalja rada

**Simptom**: Kada sekretar klikne na rad i vrati se nazad, pojavljuju se opcije "Studenti", "Mentori", itd.

**Uzrok**: Kontroleri `ThesisDetailsController` i `ThesisFormController` su hardkodovano koristili `SceneManager.show("/app/dashboard.fxml", ...)` za povratak, bez obzira na tip korisnika.

**Rješenje**: 
- `ThesisDetailsController.back()` sada provjerava `UserSession.getUser().getRole()` i vraća sekretara na `secretary-dashboard.fxml`
- `ThesisFormController` koristi novu `returnToDashboard()` metodu koja radi istu provjeru

## Baza podataka

### Pretpostavke:

- Tabela `Thesis` ima kolonu `SecretaryId` koja pokazuje na `AppUser.Id`
- Tabela `AppUser` ima kolonu `RoleId` koja pokazuje na `UserRole.Id`
- U bazi postoje uloge:
  - `SECRETARY` - za sekretare
  - `ADMINISTRATOR` - za studentsku službu

### SQL filter:

```sql
-- Za sekretara:
SELECT ... FROM Thesis WHERE SecretaryId = ?

-- Za administratora:
SELECT ... FROM Thesis WHERE IsActive = 1
```

## Testiranje

### Testni scenario 1: Sekretar - Osnovni tok

1. Logiraj se kao korisnik sa `role = "SECRETARY"`
2. Verifikuj da se otvara "eDiploma - Sekretar" dashboard
3. Verifikuj da se vide samo opcija "Završni radovi"
4. Verifikuj da se prikazuju samo radovi gdje je `SecretaryId = currentUserId`

### Testni scenario 2: Sekretar - Detalji rada

1. Logiraj se kao sekretar
2. Klikni na jedan od svojih radova
3. Otvoriće se `thesisDetails.fxml`
4. Klikni "Nazad"
5. **Verifikuj da se vraćaš na `secretary-dashboard.fxml`**
6. **Verifikuj da NEMA opcija "Studenti", "Mentori", "Sekretari"**

### Testni scenario 3: Sekretar - Kreiranje/Uređivanje rada

1. Logiraj se kao sekretar
2. Klikni "Dodaj rad" ili "Uredi" na postojecem radu
3. Otvoriće se `thesisForm.fxml`
4. Popuni formu i klikni "Sačuvaj" (ili klikni "Nazad")
5. **Verifikuj da se vraćaš na `secretary-dashboard.fxml`**
6. **Verifikuj da NEMA opcija "Studenti", "Mentori", "Sekretari"**

### Testni scenario 4: Administrator login

1. Logiraj se kao korisnik sa `role = "ADMINISTRATOR"`
2. Verifikuj da se otvara standardni "eDiploma" dashboard
3. Verifikuj da su vidljive sve opcije (Studenti, Mentori, Sekretari, Završni radovi)
4. Verifikuj da se prikazuju SVI radovi u sistemu
5. Testiraj navigaciju (detalji, forma) - uvijek se vraća na `dashboard.fxml`

## Branch informacije

- **Branch name**: `feature/secretary-view`
- **Base branch**: `main`
- **Broj commitova**: 3 (nakon squash-a)
  1. Implement secretary dashboard with separate view
  2. Fix back navigation for secretary to return to secretary dashboard  
  3. Update implementation docs with navigation fixes

## Sledeći koraci

1. ✅ Testirati funkcionalnost sa pravim korisnicima iz baze
2. Kreirati Pull Request ka `main` branchu
3. Code review
4. Merge

---

**Autor**: AI Assistant  
**Datum**: 2026-01-05  
**Status**: Ready for Testing & Review
