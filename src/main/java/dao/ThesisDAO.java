package dao;

import dto.ThesisDTO;
import dto.ThesisDetailsDTO;
import model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThesisDAO {

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
                INSERT INTO Thesis(Title,ApplicationDate,DepartmentId,StudentId,MentorId,SecretaryId,SubjectId)
                VALUES(?,?,?,?,?,?,?)
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
            UpdatedAt = ?
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
            ps.setInt(5, thesis.getGrade());
            ps.setInt(6, thesis.getStudentId());
            ps.setInt(7, thesis.getAcademicStaffId());
            ps.setInt(8, thesis.getDepartmentId());
            ps.setInt(9, thesis.getSubjectId());
            ps.setInt(10, thesis.getStatusId());
            ps.setInt(11, thesis.getSecretaryId());
            ps.setTimestamp(12, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(13, thesis.getId());

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
            U.AcademicStaffId AS SecretaryAcademicStaffId
        FROM Thesis T 
        JOIN AppUser U ON U.Id = T.SecretaryId
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
                thesis.setGrade(rs.getInt("Grade"));
                thesis.setStudentId(rs.getInt("StudentId"));
                thesis.setAcademicStaffId(rs.getInt("MentorId"));
                thesis.setDepartmentId(rs.getInt("DepartmentId"));
                thesis.setSubjectId(rs.getInt("SubjectId"));
                thesis.setStatusId(rs.getInt("StatusId"));
                thesis.setSecretaryId(rs.getInt("SecretaryAcademicStaffId"));
                thesis.setActive(rs.getBoolean("IsActive"));

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
        String sql = """
        SELECT 
            T.Id, T.Title, T.ApplicationDate, T.ApprovalDate, T.DefenseDate, T.Grade,
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
                                rs.getDate("ApprovalDate").toLocalDate() : null)
                        .defenseDate(rs.getDate("DefenseDate") != null ?
                                rs.getDate("DefenseDate").toLocalDate() : null)
                        .grade(rs.getInt("Grade"))
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
}