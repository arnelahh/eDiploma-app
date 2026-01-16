package dao;

import dto.ThesisDTO;
import dto.ThesisDetailsDTO;
import dto.ThesisLockInfoDTO;
import model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThesisDAO {
    private static final int LOCK_TIMEOUT_MINUTES = 30;

    public List<ThesisDTO> getAllThesis() {
        List<ThesisDTO> thesis = new ArrayList<>();
        String sql = """
                SELECT T.Id,
                    T.Title,
                    CONCAT(S.FirstName,' ',S.LastName) AS StudentFullName,
                    CONCAT(A.FirstName,' ',A.LastName) AS MentorFullName,
                    S.Cycle,
                    TS.Name AS Status,
                    T.ApplicationDate
                FROM Thesis T
                JOIN Student S ON S.Id = T.StudentId
                JOIN AcademicStaff A ON A.Id = T.MentorId
                JOIN ThesisStatus TS ON TS.Id = T.StatusId
                WHERE T.IsActive = 1
                ORDER BY T.Id DESC
                """;

        try (Connection conn = CloudDatabaseConnection.Konekcija()) {

            clearExpiredLocks(conn);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    ThesisDTO thesisDTO = new ThesisDTO();
                    thesisDTO.setId(rs.getInt("Id"));
                    thesisDTO.setTitle(rs.getString("Title"));
                    thesisDTO.setStudentFullName(rs.getString("StudentFullName"));
                    thesisDTO.setMentorFullName(rs.getString("MentorFullName"));
                    thesisDTO.setCycle(rs.getInt("Cycle"));
                    thesisDTO.setStatus(rs.getString("Status"));
                    thesisDTO.setApplicationDate(rs.getDate("ApplicationDate").toLocalDate());
                    thesis.add(thesisDTO);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return thesis;
    }

    /**
     * NOVI METOD: Dohvata sve radove OSIM odbranenih (Odbranjen status)
     * Koristi se za inicijalno učitavanje stranice
     */
    public List<ThesisDTO> getAllThesisExcludingGraduated() {
        List<ThesisDTO> thesis = new ArrayList<>();
        String sql = """
                SELECT T.Id,
                    T.Title,
                    CONCAT(S.FirstName,' ',S.LastName) AS StudentFullName,
                    CONCAT(A.FirstName,' ',A.LastName) AS MentorFullName,
                    S.Cycle,
                    TS.Name AS Status,
                    T.ApplicationDate
                FROM Thesis T
                JOIN Student S ON S.Id = T.StudentId
                JOIN AcademicStaff A ON A.Id = T.MentorId
                JOIN ThesisStatus TS ON TS.Id = T.StatusId
                WHERE T.IsActive = 1 AND TS.Name <> 'Odbranjen'
                ORDER BY T.Id DESC
                """;

        try (Connection conn = CloudDatabaseConnection.Konekcija()) {
            clearExpiredLocks(conn);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    ThesisDTO thesisDTO = new ThesisDTO();
                    thesisDTO.setId(rs.getInt("Id"));
                    thesisDTO.setTitle(rs.getString("Title"));
                    thesisDTO.setStudentFullName(rs.getString("StudentFullName"));
                    thesisDTO.setMentorFullName(rs.getString("MentorFullName"));
                    thesisDTO.setCycle(rs.getInt("Cycle"));
                    thesisDTO.setStatus(rs.getString("Status"));
                    thesisDTO.setApplicationDate(rs.getDate("ApplicationDate").toLocalDate());
                    thesis.add(thesisDTO);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju aktivnih radova: " + e.getMessage(), e);
        }
        return thesis;
    }

    /**
     * NOVI METOD: Dohvata samo odbranene radove (Odbranjen status)
     * Koristi se za lazy loading kada korisnik klikne na filter
     */
    public List<ThesisDTO> getGraduatedTheses() {
        List<ThesisDTO> thesis = new ArrayList<>();
        String sql = """
                SELECT T.Id,
                    T.Title,
                    CONCAT(S.FirstName,' ',S.LastName) AS StudentFullName,
                    CONCAT(A.FirstName,' ',A.LastName) AS MentorFullName,
                    S.Cycle,
                    TS.Name AS Status,
                    T.ApplicationDate
                FROM Thesis T
                JOIN Student S ON S.Id = T.StudentId
                JOIN AcademicStaff A ON A.Id = T.MentorId
                JOIN ThesisStatus TS ON TS.Id = T.StatusId
                WHERE T.IsActive = 1 AND TS.Name = 'Odbranjen'
                ORDER BY T.Id DESC
                """;

        try (Connection conn = CloudDatabaseConnection.Konekcija()) {
            clearExpiredLocks(conn);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    ThesisDTO thesisDTO = new ThesisDTO();
                    thesisDTO.setId(rs.getInt("Id"));
                    thesisDTO.setTitle(rs.getString("Title"));
                    thesisDTO.setStudentFullName(rs.getString("StudentFullName"));
                    thesisDTO.setMentorFullName(rs.getString("MentorFullName"));
                    thesisDTO.setCycle(rs.getInt("Cycle"));
                    thesisDTO.setStatus(rs.getString("Status"));
                    thesisDTO.setApplicationDate(rs.getDate("ApplicationDate").toLocalDate());
                    thesis.add(thesisDTO);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju odbranenih radova: " + e.getMessage(), e);
        }
        return thesis;
    }

    /**
     * Dohvata sve radove dodijeljene određenom sekretaru
     * @param secretaryUserId ID korisnika (AppUser.Id) sekretara
     * @return Lista ThesisDTO objekata koje pripadaju sekretaru
     */
    public List<ThesisDTO> getThesisBySecretaryId(int secretaryUserId) {
        List<ThesisDTO> thesis = new ArrayList<>();
        String sql = """
                SELECT T.Id,
                    T.Title,
                    CONCAT(S.FirstName,' ',S.LastName) AS StudentFullName,
                    CONCAT(A.FirstName,' ',A.LastName) AS MentorFullName,
                    S.Cycle,
                    TS.Name AS Status,
                    T.ApplicationDate
                FROM Thesis T
                JOIN Student S ON S.Id = T.StudentId
                JOIN AcademicStaff A ON A.Id = T.MentorId
                JOIN ThesisStatus TS ON TS.Id = T.StatusId
                WHERE T.IsActive = 1 AND T.SecretaryId = ?
                ORDER BY T.Id DESC
                """;

        try (Connection conn = CloudDatabaseConnection.Konekcija()) {

            clearExpiredLocks(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, secretaryUserId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    ThesisDTO thesisDTO = new ThesisDTO();
                    thesisDTO.setId(rs.getInt("Id"));
                    thesisDTO.setTitle(rs.getString("Title"));
                    thesisDTO.setStudentFullName(rs.getString("StudentFullName"));
                    thesisDTO.setMentorFullName(rs.getString("MentorFullName"));
                    thesisDTO.setCycle(rs.getInt("Cycle"));
                    thesisDTO.setStatus(rs.getString("Status"));
                    thesisDTO.setApplicationDate(rs.getDate("ApplicationDate").toLocalDate());
                    thesis.add(thesisDTO);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju radova sekretara: " + e.getMessage(), e);
        }
        return thesis;
    }

    /**
     * NOVI METOD: Dohvata radove sekretara OSIM odbranenih
     */
    public List<ThesisDTO> getThesisBySecretaryIdExcludingGraduated(int secretaryUserId) {
        List<ThesisDTO> thesis = new ArrayList<>();
        String sql = """
                SELECT T.Id,
                    T.Title,
                    CONCAT(S.FirstName,' ',S.LastName) AS StudentFullName,
                    CONCAT(A.FirstName,' ',A.LastName) AS MentorFullName,
                    S.Cycle,
                    TS.Name AS Status,
                    T.ApplicationDate
                FROM Thesis T
                JOIN Student S ON S.Id = T.StudentId
                JOIN AcademicStaff A ON A.Id = T.MentorId
                JOIN ThesisStatus TS ON TS.Id = T.StatusId
                WHERE T.IsActive = 1 AND T.SecretaryId = ? AND TS.Name <> 'Odbranjen'
                ORDER BY T.Id DESC
                """;

        try (Connection conn = CloudDatabaseConnection.Konekcija()) {
            clearExpiredLocks(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, secretaryUserId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    ThesisDTO thesisDTO = new ThesisDTO();
                    thesisDTO.setId(rs.getInt("Id"));
                    thesisDTO.setTitle(rs.getString("Title"));
                    thesisDTO.setStudentFullName(rs.getString("StudentFullName"));
                    thesisDTO.setMentorFullName(rs.getString("MentorFullName"));
                    thesisDTO.setCycle(rs.getInt("Cycle"));
                    thesisDTO.setStatus(rs.getString("Status"));
                    thesisDTO.setApplicationDate(rs.getDate("ApplicationDate").toLocalDate());
                    thesis.add(thesisDTO);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju aktivnih radova sekretara: " + e.getMessage(), e);
        }
        return thesis;
    }

    /**
     * NOVI METOD: Dohvata samo odbranene radove sekretara
     */
    public List<ThesisDTO> getGraduatedThesisBySecretaryId(int secretaryUserId) {
        List<ThesisDTO> thesis = new ArrayList<>();
        String sql = """
                SELECT T.Id,
                    T.Title,
                    CONCAT(S.FirstName,' ',S.LastName) AS StudentFullName,
                    CONCAT(A.FirstName,' ',A.LastName) AS MentorFullName,
                    S.Cycle,
                    TS.Name AS Status,
                    T.ApplicationDate
                FROM Thesis T
                JOIN Student S ON S.Id = T.StudentId
                JOIN AcademicStaff A ON A.Id = T.MentorId
                JOIN ThesisStatus TS ON TS.Id = T.StatusId
                WHERE T.IsActive = 1 AND T.SecretaryId = ? AND TS.Name = 'Odbranjen'
                ORDER BY T.Id DESC
                """;

        try (Connection conn = CloudDatabaseConnection.Konekcija()) {
            clearExpiredLocks(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, secretaryUserId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    ThesisDTO thesisDTO = new ThesisDTO();
                    thesisDTO.setId(rs.getInt("Id"));
                    thesisDTO.setTitle(rs.getString("Title"));
                    thesisDTO.setStudentFullName(rs.getString("StudentFullName"));
                    thesisDTO.setMentorFullName(rs.getString("MentorFullName"));
                    thesisDTO.setCycle(rs.getInt("Cycle"));
                    thesisDTO.setStatus(rs.getString("Status"));
                    thesisDTO.setApplicationDate(rs.getDate("ApplicationDate").toLocalDate());
                    thesis.add(thesisDTO);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju odbranenih radova sekretara: " + e.getMessage(), e);
        }
        return thesis;
    }

    public int insertThesis(Thesis thesis) {
        String sql = """
        INSERT INTO Thesis(Title,ApplicationDate,DepartmentId,StudentId,MentorId,SecretaryId,SubjectId, Description, Structure, Literature, PassedSubjects)
        VALUES(?,?,?,?,?,?,?,?,?, ?, ?)
        """;

        try (Connection connection = CloudDatabaseConnection.Konekcija()) {
            connection.setAutoCommit(false);

            int thesisId;

            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                LocalDate applicationLocalDate = thesis.getApplicationDate();
                java.sql.Date sqlDate = java.sql.Date.valueOf(applicationLocalDate);

                stmt.setString(1, thesis.getTitle());
                stmt.setDate(2, sqlDate);
                stmt.setInt(3, thesis.getDepartmentId());
                stmt.setInt(4, thesis.getStudentId());
                stmt.setInt(5, thesis.getAcademicStaffId());
                stmt.setInt(6, thesis.getSecretaryId());
                stmt.setInt(7, thesis.getSubjectId());
                stmt.setString(8, thesis.getDescription());
                stmt.setString(9, thesis.getStructure());
                stmt.setString(10, thesis.getLiterature());
                stmt.setBoolean(11, thesis.isPassedSubjects());

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new RuntimeException("Error in inserting thesis");
                }

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new RuntimeException("Nije moguće dobiti ID novog rada (generated key).");
                    }
                    thesisId = keys.getInt(1);
                }
            }

            // 1) Kreiraj prvi dokument odmah po kreiranju rada: IN_PROGRESS
            //    Tip dokumenta: "Rješenje o izradi završnog rada"
            DocumentTypeDAO documentTypeDAO = new DocumentTypeDAO();
            DocumentDAO documentDAO = new DocumentDAO();

            DocumentType firstDocType = documentTypeDAO.getByName("Rješenje o izradi završnog rada");
            if (firstDocType == null) {
                throw new RuntimeException("DocumentType 'Rješenje o izradi završnog rada' nije pronađen.");
            }

            Integer uploadedByUserId = thesis.getSecretaryId();

            // Kreira placeholder zapis dokumenta (bez content/number) -> IN_PROGRESS
            documentDAO.ensureDocumentExists(connection, thesisId, firstDocType.getId(), DocumentStatus.IN_PROGRESS, uploadedByUserId);

            connection.commit();
            return thesisId;

        } catch (Exception e) {
            throw new RuntimeException("Greška pri kreiranju rada i inicijalnog dokumenta: " + e.getMessage(), e);
        }
    }


    public void updateThesis(Thesis thesis) {
        String sql = """
        UPDATE Thesis SET
            Title = ?,
            ApplicationDate = ?,
            ApprovalDate = ?,
            DefenseDate = ?,
            FinalThesisApprovalDate = ?,
            Grade = ?,
            StudentId = ?,
            MentorId = ?,
            DepartmentId = ?,
            SubjectId = ?,
            StatusId = ?,
            SecretaryId = ?,
            UpdatedAt = ?,
            Description = ?,
            Literature = ?,
            Structure = ?,
            PassedSubjects = ?
        WHERE Id = ?
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, thesis.getTitle());
            ps.setDate(2, thesis.getApplicationDate() != null ?
                    java.sql.Date.valueOf(thesis.getApplicationDate()) : null);
            ps.setDate(3, thesis.getApprovalDate() != null ?
                    java.sql.Date.valueOf(thesis.getApprovalDate()) : null);
            ps.setDate(4, thesis.getDefenseDate() != null ?
                    java.sql.Date.valueOf(thesis.getDefenseDate()) : null);
            ps.setDate(5, thesis.getFinalThesisApprovalDate() != null ?
                    java.sql.Date.valueOf(thesis.getFinalThesisApprovalDate()) : null);
            if (thesis.getGrade() != null) {
                ps.setInt(6, thesis.getGrade());
            } else {
                // Ako je null, moramo eksplicitno reći bazi da je NULL
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setInt(7, thesis.getStudentId());
            ps.setInt(8, thesis.getAcademicStaffId());
            ps.setInt(9, thesis.getDepartmentId());
            ps.setInt(10, thesis.getSubjectId());
            ps.setInt(11, thesis.getStatusId());
            ps.setInt(12, thesis.getSecretaryId());
            ps.setTimestamp(13, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(14, thesis.getDescription());
            ps.setString(15, thesis.getLiterature());
            ps.setString(16, thesis.getStructure());
            ps.setBoolean(17, thesis.isPassedSubjects());
            ps.setInt(18, thesis.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju rada: " + e.getMessage(), e);
        }
    }

    public void deleteThesis(int id) {
        String sql = "UPDATE Thesis SET IsActive = 0, UpdatedAt = ? WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri brisanju rada: " + e.getMessage(), e);
        }
    }

    public Thesis getThesisById(int id) {
        // FIXED: Now correctly returns T.SecretaryId (AppUser.Id) instead of AcademicStaffId
        String sql = """
        SELECT 
            T.Id,
            T.Title,
            T.ApplicationDate,
            T.ApprovalDate,
            T.DefenseDate,
            T.FinalThesisApprovalDate,
            T.Grade,
            T.SubjectId,
            T.StatusId,
            T.MentorId,
            T.StudentId,
            T.CreatedAt,
            T.UpdatedAt,
            T.IsActive,
            T.DepartmentId,
            T.SecretaryId,
            T.Description,
            T.Literature,
            T.Structure,
            T.PassedSubjects
        FROM Thesis T 
        WHERE T.Id = ? AND T.IsActive = 1
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Thesis thesis = new Thesis();
                thesis.setId(rs.getInt("Id"));
                thesis.setTitle(rs.getString("Title"));
                thesis.setApplicationDate(rs.getDate("ApplicationDate") != null ?
                        rs.getDate("ApplicationDate").toLocalDate() : null);
                thesis.setApprovalDate(rs.getDate("ApprovalDate") != null ?
                        rs.getDate("ApprovalDate").toLocalDate() : null);
                thesis.setDefenseDate(rs.getDate("DefenseDate") != null ?
                        rs.getDate("DefenseDate").toLocalDate() : null);
                thesis.setFinalThesisApprovalDate(rs.getDate("FinalThesisApprovalDate") != null ?
                        rs.getDate("FinalThesisApprovalDate").toLocalDate() : null);

                // Handling Grade correctly (checking for NULL)
                int grade = rs.getInt("Grade");
                if (rs.wasNull()) {
                    thesis.setGrade(null);
                } else {
                    thesis.setGrade(grade);
                }

                thesis.setStudentId(rs.getInt("StudentId"));
                thesis.setAcademicStaffId(rs.getInt("MentorId"));
                thesis.setDepartmentId(rs.getInt("DepartmentId"));
                thesis.setSubjectId(rs.getInt("SubjectId"));
                thesis.setStatusId(rs.getInt("StatusId"));
                // FIXED: Now correctly sets AppUser.Id for secretary
                thesis.setSecretaryId(rs.getInt("SecretaryId"));
                thesis.setActive(rs.getBoolean("IsActive"));

                // Fields mapping for Description and Literature
                thesis.setDescription(rs.getString("Description"));
                thesis.setLiterature(rs.getString("Literature"));
                thesis.setStructure(rs.getString("Structure"));
                thesis.setPassedSubjects(rs.getBoolean("PassedSubjects"));

                if (rs.getTimestamp("CreatedAt") != null) {
                    thesis.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                }
                if (rs.getTimestamp("UpdatedAt") != null) {
                    thesis.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
                }

                return thesis;
            }
            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju rada: " + e.getMessage(), e);
        }
    }

    public ThesisDetailsDTO getThesisDetails(int thesisId) {
        // UPDATED SQL to select NoticeDate, CommisionTime, CycleCompletionDate, and ApprovalDate (commission meeting date)
        String sql = """
        SELECT 
            T.Id, T.Title, T.ApplicationDate, T.ApprovalDate, T.DefenseDate, T.FinalThesisApprovalDate, 
            T.CommisionDate, T.NoticeDate, T.CommisionTime, T.CycleCompletionDate, T.Grade,
            T.Description, T.Literature, T.Structure, T.PassedSubjects,
            TS.Name AS StatusName,
            U.AcademicStaffId as SecretaryAcademicStaffId, 
            S.Id AS StudentId, S.FirstName AS StudentFirstName, S.LastName AS StudentLastName,
            S.FatherName AS StudentFatherName, S.IndexNumber, S.BirthDate, S.BirthPlace,
            S.Municipality, S.Country, S.StudyProgram, S.ECTS, S.Cycle, S.CycleDuration,
            S.Email AS StudentEmail,
            SS.Id AS StudentStatusId, SS.Name AS StudentStatusName,
            A.Id AS MentorId, A.Title AS MentorTitle, A.FirstName AS MentorFirstName,
            A.LastName AS MentorLastName, A.Email AS MentorEmail,
            SEC.Id AS SecretaryId, SEC.Title AS SecretaryTitle, 
            SEC.FirstName AS SecretaryFirstName, SEC.LastName AS SecretaryLastName,
            SEC.Email AS SecretaryEmail,
            D.Id AS DepartmentId, D.Name AS DepartmentName,
            SUB.Id AS SubjectId, SUB.Name AS SubjectName
        FROM Thesis T
        JOIN ThesisStatus TS ON T.StatusId = TS.Id
        JOIN Student S ON T.StudentId = S.Id
        JOIN StudentStatus SS ON S.StatusId = SS.Id
        JOIN AcademicStaff A ON T.MentorId = A.Id
        JOIN AppUser U ON T.SecretaryId = U.Id
        JOIN AcademicStaff SEC ON U.AcademicStaffId = SEC.Id
        JOIN Department D ON T.DepartmentId = D.Id
        JOIN Subject SUB ON T.SubjectId = SUB.Id
        WHERE T.Id = ? AND T.IsActive = 1
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, thesisId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                StudentStatus studentStatus = StudentStatus.builder()
                        .Id(rs.getInt("StudentStatusId"))
                        .Name(rs.getString("StudentStatusName"))
                        .build();

                Student student = Student.builder()
                        .Id(rs.getInt("StudentId"))
                        .FirstName(rs.getString("StudentFirstName"))
                        .LastName(rs.getString("StudentLastName"))
                        .FatherName(rs.getString("StudentFatherName"))
                        .IndexNumber(rs.getInt("IndexNumber"))
                        .BirthDate(rs.getDate("BirthDate") != null ?
                                rs.getDate("BirthDate").toLocalDate() : null)
                        .BirthPlace(rs.getString("BirthPlace"))
                        .Municipality(rs.getString("Municipality"))
                        .Country(rs.getString("Country"))
                        .StudyProgram(rs.getString("StudyProgram"))
                        .ECTS(rs.getInt("ECTS"))
                        .Cycle(rs.getInt("Cycle"))
                        .CycleDuration(rs.getInt("CycleDuration"))
                        .Status(studentStatus)
                        .Email(rs.getString("StudentEmail"))
                        .build();

                AcademicStaff mentor = AcademicStaff.builder()
                        .Id(rs.getInt("MentorId"))
                        .Title(rs.getString("MentorTitle"))
                        .FirstName(rs.getString("MentorFirstName"))
                        .LastName(rs.getString("MentorLastName"))
                        .Email(rs.getString("MentorEmail"))
                        .build();

                AcademicStaff secretary = AcademicStaff.builder()
                        .Id(rs.getInt("SecretaryId"))
                        .Title(rs.getString("SecretaryTitle"))
                        .FirstName(rs.getString("SecretaryFirstName"))
                        .LastName(rs.getString("SecretaryLastName"))
                        .Email(rs.getString("SecretaryEmail"))
                        .build();

                Department department = new Department();
                department.setId(rs.getInt("DepartmentId"));
                department.setName(rs.getString("DepartmentName"));

                Subject subject = new Subject();
                subject.setId(rs.getInt("SubjectId"));
                subject.setName(rs.getString("SubjectName"));

                return ThesisDetailsDTO.builder()
                        .id(rs.getInt("Id"))
                        .title(rs.getString("Title"))
                        .applicationDate(rs.getDate("ApplicationDate") != null ?
                                rs.getDate("ApplicationDate").toLocalDate() : null)
                        .approvalDate(rs.getDate("ApprovalDate") != null ?
                                rs.getDate("ApprovalDate").toLocalDate() : null) // Commission meeting date
                        .defenseDate(rs.getDate("DefenseDate") != null ?
                                rs.getDate("DefenseDate").toLocalDate() : null)
                        .finalThesisApprovalDate(rs.getDate("FinalThesisApprovalDate") != null ?
                                rs.getDate("FinalThesisApprovalDate").toLocalDate() : null)
                        .commisionDate(rs.getDate("CommisionDate") != null ?
                                rs.getDate("CommisionDate").toLocalDate() : null)
                        .noticeDate(rs.getDate("NoticeDate") != null ?
                                rs.getDate("NoticeDate").toLocalDate() : null)
                        .commisionTime(rs.getString("CommisionTime"))
                        .cycleCompletionDate(rs.getDate("CycleCompletionDate") != null ?
                                rs.getDate("CycleCompletionDate").toLocalDate() : null)
                        .grade(rs.getInt("Grade"))
                        .description(rs.getString("Description"))
                        .literature(rs.getString("Literature"))
                        .structure(rs.getString("Structure"))
                        .passedSubjects(rs.getBoolean("PassedSubjects"))
                        .status(rs.getString("StatusName"))
                        .student(student)
                        .mentor(mentor)
                        .secretary(secretary)
                        .department(department)
                        .subject(subject)
                        .build();
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju detalja rada: " + e.getMessage(), e);
        }
    }

    public boolean lockThesis(int thesisId, int userId) {
        String sql = """
        UPDATE Thesis
        SET LockedBy = ?, LockedAt = CURRENT_TIMESTAMP
        WHERE Id = ?
          AND IsActive = 1
          AND (LockedBy IS NULL OR LockedBy = ?)
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            clearExpiredLocks(conn);

            ps.setInt(1, userId);
            ps.setInt(2, thesisId);
            ps.setInt(3, userId);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri zaključavanju rada", e);
        }
    }


    public void unlockThesis(int thesisId, int userId) {
        String sql = """
        UPDATE Thesis
        SET LockedBy = NULL,
            LockedAt = NULL
        WHERE Id = ?
          AND LockedBy = ?
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, thesisId);
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri otključavanju rada", e);
        }
    }

    public ThesisLockInfoDTO getLockInfo(int thesisId) {
        String sql = """
        SELECT t.LockedBy,
               t.LockedAt,
               u.Username
        FROM Thesis t
        LEFT JOIN AppUser u ON u.Id = t.LockedBy
        WHERE t.Id = ?
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            clearExpiredLocks(conn);

            ps.setInt(1, thesisId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Integer lockedById = (Integer) rs.getObject("LockedBy"); // null-safe
                Timestamp lockedAt = rs.getTimestamp("LockedAt");
                String username = rs.getString("Username");

                return new ThesisLockInfoDTO(lockedById, username, lockedAt);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju lock info.", e);
        }
    }

    public boolean isLockedByUser(int thesisId, int userId) {
        String sql = """
        SELECT 1
        FROM Thesis
        WHERE Id = ?
          AND LockedBy = ?
          AND LockedAt IS NOT NULL
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            clearExpiredLocks(conn);

            ps.setInt(1, thesisId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri provjeri lock-a.", e);
        }
    }

    private void clearExpiredLocks(Connection conn) throws SQLException {

        String sql = """
        UPDATE Thesis
        SET LockedBy = NULL,
            LockedAt = NULL
        WHERE LockedAt IS NOT NULL
          AND LockedAt < CURRENT_TIMESTAMP - INTERVAL ? MINUTE
    """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, LOCK_TIMEOUT_MINUTES);
            ps.executeUpdate();
        }
    }

    public void updateDefenseDate(int thesisId, LocalDate defenseDate) {
        String sql = "UPDATE Thesis SET DefenseDate = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (defenseDate != null) {
                ps.setDate(1, java.sql.Date.valueOf(defenseDate));
            } else {
                ps.setNull(1, java.sql.Types.DATE);
            }
            ps.setInt(2, thesisId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Thesis sa ID " + thesisId + " nije pronađen.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju datuma odbrane.", e);
        }
    }

    /**
     * NOVI METOD: Ažurira datum final thesis approval za specifičan rad
     */
    public void updateFinalThesisApprovalDate(int thesisId, LocalDate finalThesisApprovalDate) {
        String sql = "UPDATE Thesis SET FinalThesisApprovalDate = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (finalThesisApprovalDate != null) {
                ps.setDate(1, java.sql.Date.valueOf(finalThesisApprovalDate));
            } else {
                ps.setNull(1, java.sql.Types.DATE);
            }
            ps.setInt(2, thesisId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Thesis sa ID " + thesisId + " nije pronađen.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju datuma odobrenja završnog rada.", e);
        }
    }

    /**
     * NOVI METOD: Ažurira datum komisije za specifičan rad
     */
    public void updateCommisionDate(int thesisId, LocalDate commisionDate) {
        String sql = "UPDATE Thesis SET CommisionDate = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (commisionDate != null) {
                ps.setDate(1, java.sql.Date.valueOf(commisionDate));
            } else {
                ps.setNull(1, java.sql.Types.DATE);
            }
            ps.setInt(2, thesisId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Thesis sa ID " + thesisId + " nije pronađen.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju datuma komisije.", e);
        }
    }

    /**
     * NOVI METOD: Ažurira datum rješenja obavijesti (NoticeDate)
     */
    public void updateNoticeDate(int thesisId, LocalDate noticeDate) {
        String sql = "UPDATE Thesis SET NoticeDate = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (noticeDate != null) {
                ps.setDate(1, java.sql.Date.valueOf(noticeDate));
            } else {
                ps.setNull(1, java.sql.Types.DATE);
            }
            ps.setInt(2, thesisId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Thesis sa ID " + thesisId + " nije pronađen.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju datuma obavijesti.", e);
        }
    }

    /**
     * NOVI METOD: Ažurira datum sjednice komisije (ApprovalDate)
     */
    public void updateCommissionMeetingDate(int thesisId, LocalDate meetingDate) {
        String sql = "UPDATE Thesis SET ApprovalDate = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (meetingDate != null) {
                ps.setDate(1, java.sql.Date.valueOf(meetingDate));
            } else {
                ps.setNull(1, java.sql.Types.DATE);
            }
            ps.setInt(2, thesisId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Thesis sa ID " + thesisId + " nije pronađen.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju datuma sjednice.", e);
        }
    }

    /**
     * NOVI METOD: Ažurira vrijeme sjednice komisije (CommisionTime)
     */
    public void updateCommisionTime(int thesisId, String commisionTime) {
        String sql = "UPDATE Thesis SET CommisionTime = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (commisionTime != null && !commisionTime.isBlank()) {
                ps.setString(1, commisionTime);
            } else {
                ps.setNull(1, java.sql.Types.VARCHAR);
            }
            ps.setInt(2, thesisId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Thesis sa ID " + thesisId + " nije pronađen.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju vremena sjednice.", e);
        }
    }

    /**
     * NOVI METOD: Ažurira datum završetka ciklusa (CycleCompletionDate)
     */
    public void updateCycleCompletionDate(int thesisId, LocalDate cycleCompletionDate) {
        String sql = "UPDATE Thesis SET CycleCompletionDate = ?, UpdatedAt = CURRENT_TIMESTAMP WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (cycleCompletionDate != null) {
                ps.setDate(1, java.sql.Date.valueOf(cycleCompletionDate));
            } else {
                ps.setNull(1, java.sql.Types.DATE);
            }
            ps.setInt(2, thesisId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Thesis sa ID " + thesisId + " nije pronađen.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju datuma završetka ciklusa.", e);
        }
    }

    public void updateStatusByName(int thesisId, String statusName) {
        String sql = """
        UPDATE Thesis
        SET StatusId = (SELECT Id FROM ThesisStatus WHERE Name = ? LIMIT 1),
            UpdatedAt = CURRENT_TIMESTAMP
        WHERE Id = ? AND IsActive = 1
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statusName);
            ps.setInt(2, thesisId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Status nije ažuriran. Provjeri da li status postoji u bazi i da li je rad aktivan.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri ažuriranju statusa rada.", e);
        }
    }

    public String getStatusName(int thesisId) {
        String sql = """
        SELECT TS.Name
        FROM Thesis T
        JOIN ThesisStatus TS ON TS.Id = T.StatusId
        WHERE T.Id = ? AND T.IsActive = 1
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, thesisId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getString(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri čitanju statusa rada.", e);
        }
    }

    public int getTotalThesisCount() {
        String sql = "SELECT COUNT(*) FROM Thesis WHERE IsActive = 1";
        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getActiveThesisCount() {
        String sql = """
            
                SELECT COUNT(*) 
            FROM Thesis T
            JOIN ThesisStatus TS ON T.StatusId = TS.Id
            WHERE T.IsActive = 1 AND TS.Name <> 'Odbranjen'
            """;
        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Vraća mapu gdje je ključ ime i prezime sekretara, a vrijednost broj radova koje vodi.
     */
    public java.util.Map<String, Integer> getSecretaryThesisCounts() {
        // IZMJENA 1: Koristi LinkedHashMap umjesto HashMap.
        // LinkedHashMap čuva redoslijed umetanja (insertion order).
        java.util.Map<String, Integer> counts = new java.util.LinkedHashMap<>();

        // IZMJENA 2: Dodan "ORDER BY Total DESC" na kraju SQL-a
        String sql = """
        SELECT CONCAT(S.FirstName, ' ', S.LastName) AS SecretaryName, COUNT(T.Id) AS Total
        FROM Thesis T
        JOIN AppUser U ON T.SecretaryId = U.Id
        JOIN AcademicStaff S ON U.AcademicStaffId = S.Id
        WHERE T.IsActive = 1
        GROUP BY S.Id, S.FirstName, S.LastName
        ORDER BY Total DESC
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String name = rs.getString("SecretaryName");
                int total = rs.getInt("Total");
                counts.put(name, total);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return counts;
    }

    public int getLateThesisCount() {
        String sql = """
            SELECT COUNT(*)
            FROM Thesis T
            JOIN ThesisStatus TS ON T.StatusId = TS.Id
            WHERE T.IsActive = 1
              AND TS.Name <> 'Odbranjen'
              AND DATEDIFF(CURRENT_DATE, T.ApplicationDate) > 90
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Map<String, Integer> getTopMentorsFiltered(LocalDate startDate, LocalDate endDate) {
        Map<String, Integer> stats = new LinkedHashMap<>();

        StringBuilder sql = new StringBuilder("""
            SELECT CONCAT(A.Title, ' ', A.FirstName, ' ', A.LastName) AS MentorName, COUNT(T.Id) AS Total
            FROM Thesis T
            JOIN AcademicStaff A ON T.MentorId = A.Id
            JOIN ThesisStatus TS ON T.StatusId = TS.Id
            WHERE T.IsActive = 1
        """);

        // Ako imamo filter datuma, dodajemo uvjet
        if (startDate != null && endDate != null) {
            sql.append(" AND T.ApplicationDate >= ? AND T.ApplicationDate <= ?");
        }

        sql.append("""
            GROUP BY A.FirstName, A.LastName, A.Title
            ORDER BY Total DESC
        """);

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            // Postavljanje parametara za datum ako su proslijedđeni
            if (startDate != null && endDate != null) {
                ps.setDate(1, java.sql.Date.valueOf(startDate));
                ps.setDate(2, java.sql.Date.valueOf(endDate));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("MentorName"), rs.getInt("Total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
