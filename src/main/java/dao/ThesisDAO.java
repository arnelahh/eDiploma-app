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
                " join ThesisStatus TS on TS.Id=T.StatusId";

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
                  where LOWER(T.Title) like ? or lower(CONCAT(S.FirstName,' ',S.LastName)) LIKE ? or lower(CONCAT(A.FirstName,' ',A.LastName)) LIKE ? or LOwer(S.FirstName) like ? or lower(S.LastName) LIKE ? or lower(A.FirstName) LIKE ? or lower(A.LastName) LIKE ?
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



}
