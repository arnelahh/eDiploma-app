package dao;

import dto.ThesisDTO;
import dto.ThesisDetailsDTO;
import dto.ThesisLockInfoDTO;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
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
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return thesis;
    }

    public void insertThesis(Thesis thesis) {
        String sql = """
                INSERT INTO Thesis(Title,ApplicationDate,DepartmentId,StudentId,MentorId,SecretaryId,SubjectId, Description, Literature)
                VALUES(?,?,?,?,?,?,?,?,?)
                """;
        try (Connection connection = CloudDatabaseConnection.Konekcija();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            java.time.LocalDate applicationLocalDate = thesis.getApplicationDate();
            java.sql.Date sqlDate = java.sql.Date.valueOf(applicationLocalDate);

            stmt.setString(1, thesis.getTitle());
            stmt.setDate(2, sqlDate);
            stmt.setInt(3, thesis.getDepartmentId());
            stmt.setInt(4, thesis.getStudentId());
            stmt.setInt(5, thesis.getAcademicStaffId());
            stmt.setInt(6, thesis.getSecretaryId());
            stmt.setInt(7, thesis.getSubjectId());
            stmt.setString(8, thesis.getDescription());
            stmt.setString(9, thesis.getLiterature());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Error in inserting thesis");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateThesis(Thesis thesis) {
        String sql = """
        UPDATE Thesis SET
            Title = ?,
            ApplicationDate = ?,
            ApprovalDate = ?,
            DefenseDate = ?,
            Grade = ?,
            StudentId = ?,
            MentorId = ?,
            DepartmentId = ?,
            SubjectId = ?,
            StatusId = ?,
            SecretaryId = ?,
            UpdatedAt = ?,
            Description = ?,
            Literature = ?
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
            if (thesis.getGrade() != null) {
                ps.setInt(5, thesis.getGrade());
            } else {
                // Ako je null, moramo eksplicitno reći bazi da je NULL
                ps.setNull(5, java.sql.Types.INTEGER);
            }
            ps.setInt(6, thesis.getStudentId());
            ps.setInt(7, thesis.getAcademicStaffId());
            ps.setInt(8, thesis.getDepartmentId());
            ps.setInt(9, thesis.getSubjectId());
            ps.setInt(10, thesis.getStatusId());
            ps.setInt(11, thesis.getSecretaryId());
            ps.setTimestamp(12, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setString(13, thesis.getDescription());
            ps.setString(14, thesis.getLiterature());
            ps.setInt(15, thesis.getId());
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
            T.Literature
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
        // UPDATED SQL to select Description and Literature
        String sql = """
        SELECT 
            T.Id, T.Title, T.ApplicationDate, T.ApprovalDate, T.DefenseDate, T.Grade,
            T.Description, T.Literature,
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
                // ... (Student, AcademicStaff, etc. mappings remain same) ...
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
                                rs.getDate("ApprovalDate").toLocalDate() : null)
                        .defenseDate(rs.getDate("DefenseDate") != null ?
                                rs.getDate("DefenseDate").toLocalDate() : null)
                        .grade(rs.getInt("Grade"))
                        // NEW FIELDS MAPPED HERE
                        .description(rs.getString("Description"))
                        .literature(rs.getString("Literature"))
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
          AND LockedBy IS NULL
          AND IsActive = 1
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            clearExpiredLocks(conn);
            ps.setInt(1, userId);
            ps.setInt(2, thesisId);
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
}