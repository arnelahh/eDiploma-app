package dao;

import dto.ThesisDTO;
import model.Thesis;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ThesisDAO {
    public List<ThesisDTO> getAllThesis(){
        List<ThesisDTO> thesis = new ArrayList<>();
        String sql = "select T.Id,T.Title, CONCAT(S.FirstName,' ',S.LastName) as StudentFullName, CONCAT(A.FirstName,' ',A.LastName) AS MentorFullName, S.Cycle\n,TS.Name as Status" +
                "  FROM Thesis T\n" +
                "  JOIN Student S on S.Id=T.StudentId\n" +
                "  join AcademicStaff A on A.Id=T.MentorId"+
                " join ThesisStatus TS on TS.Id=T.StatusId"+
                " where T.IsActive=1";

        try(Connection conn=CloudDatabaseConnection.Konekcija();
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql);)
        {
            while (rs.next()) {
                ThesisDTO thesisDTO = new ThesisDTO();
                thesisDTO.setId(rs.getInt("Id"));
                thesisDTO.setTitle(rs.getString("Title"));
                thesisDTO.setStudentFullName(rs.getString("StudentFullName"));
                thesisDTO.setMentorFullName(rs.getString("MentorFullName"));
                thesisDTO.setCycle(rs.getInt("Cycle"));
                thesisDTO.setStatus(rs.getString("Status"));
                thesis.add(thesisDTO);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return thesis;
    }

    public List<ThesisDTO> getAllThesisBySearch(String search){
        List<ThesisDTO> thesis = new ArrayList<>();
        String sql= """
                select T.Id,T.Title, CONCAT(S.FirstName,' ',S.LastName) as StudentFullName, CONCAT(A.FirstName,' ',A.LastName) AS MentorFullName, S.Cycle
                  FROM Thesis T
                  JOIN Student S on S.Id=T.StudentId
                  join AcademicStaff A on A.Id=T.MentorId
                  where (LOWER(T.Title) like ? or lower(CONCAT(S.FirstName,' ',S.LastName)) LIKE ? or lower(CONCAT(A.FirstName,' ',A.LastName)) LIKE ? or LOwer(S.FirstName) like ? or lower(S.LastName) LIKE ? or lower(A.FirstName) LIKE ? or lower(A.LastName) LIKE ?) and T.IsActive=1;
                """;

        try (Connection connection=CloudDatabaseConnection.Konekcija();
        PreparedStatement stmt=connection.prepareStatement(sql);)
        {
            stmt.setString(1, "%" + search.toLowerCase() + "%");
            stmt.setString(2, "%" + search.toLowerCase() + "%");
            stmt.setString(3, "%" + search.toLowerCase() + "%");
            stmt.setString(4, "%" + search.toLowerCase() + "%");
            stmt.setString(5, "%" + search.toLowerCase() + "%");
            stmt.setString(6, "%" + search.toLowerCase() + "%");
            stmt.setString(7, "%" + search.toLowerCase() + "%");

            ResultSet rs=stmt.executeQuery();
            while (rs.next()) {
                ThesisDTO thesisDTO = new ThesisDTO();
                thesisDTO.setId(rs.getInt("Id"));
                thesisDTO.setTitle(rs.getString("Title"));
                thesisDTO.setStudentFullName(rs.getString("StudentFullName"));
                thesisDTO.setMentorFullName(rs.getString("MentorFullName"));
                thesisDTO.setCycle(rs.getInt("Cycle"));
                thesis.add(thesisDTO);
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
        return thesis;
    }

    public void  insertThesis(Thesis thesis){
        String sql= """
                insert into Thesis(Title,ApplicationDate,DepartmentId,StudentId,MentorId,SecretaryId,SubjectId,StatusId)
                values(?,?,?,?,?,?,?,?);
                """;
        try (Connection connection=CloudDatabaseConnection.Konekcija();
             PreparedStatement stmt=connection.prepareStatement(sql);)
        {
            java.time.LocalDate applicationLocalDate = thesis.getApplicationDate();
            java.sql.Date sqlDate = java.sql.Date.valueOf(applicationLocalDate);

            stmt.setString(1, thesis.getTitle());
            stmt.setDate(2, sqlDate);
            stmt.setInt(3,thesis.getDepartmentId());
            stmt.setInt(4,thesis.getStudentId());
            stmt.setInt(5,thesis.getAcademicStaffId());
            stmt.setInt(6,thesis.getSecretaryId());
            stmt.setInt(7,thesis.getSubjectId());
            stmt.setInt(8,thesis.getStatusId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Error in inserting thesis");
            }


        }
        catch(SQLException e){
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
            ps.setBigDecimal(5, thesis.getGrade());
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
        // Soft delete - postavljamo IsActive na false
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

    // Metoda za dohvatanje jednog rada po ID-u (potrebna za edit)
    public Thesis getThesisById(int id) {
        String sql = """
        SELECT * FROM Thesis WHERE Id = ? AND IsActive = 1
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
                thesis.setGrade(rs.getBigDecimal("Grade"));
                thesis.setStudentId(rs.getInt("StudentId"));
                thesis.setAcademicStaffId(rs.getInt("MentorId"));
                thesis.setDepartmentId(rs.getInt("DepartmentId"));
                thesis.setSubjectId(rs.getInt("SubjectId"));
                thesis.setStatusId(rs.getInt("StatusId"));
                thesis.setSecretaryId(rs.getInt("SecretaryId"));
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



}
