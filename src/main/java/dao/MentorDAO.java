package dao;

import dto.MentorDTO;
import model.AcademicStaff;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MentorDAO {

    private static final String BASE_QUERY = """
        SELECT a.*, 
        (SELECT COUNT(*) FROM Thesis t WHERE t.MentorId = a.Id) AS StudentCount,
        (SELECT COUNT(*) FROM Thesis t INNER JOIN ThesisStatus ts ON t.StatusId = ts.Id WHERE t.MentorId = a.Id AND
        ts.Name NOT IN ('Defended','Rejected')) AS OngoingThesisCount
        FROM AcademicStaff a
        WHERE NOT EXISTS(
        SELECT 1
        FROM AppUser AP
        where AP.AcademicStaffId = a.Id
        )
         AND a.IsActive = 1
        """;

    public List<MentorDTO> getAllMentors() {
        return fetchMentors(BASE_QUERY + " ORDER BY a.Id DESC");
    }

    public List<MentorDTO> searchMentors(String term) {
        String sql = BASE_QUERY + """
            AND (LOWER(a.FirstName) LIKE ?
               OR LOWER(a.LastName) LIKE ?
               OR LOWER(a.Email) LIKE ?
               OR LOWER(a.Title) LIKE ?)
            ORDER BY a.Id DESC
            """;

        return fetchMentors(sql,
                "%" + term.toLowerCase() + "%",
                "%" + term.toLowerCase() + "%",
                "%" + term.toLowerCase() + "%",
                "%" + term.toLowerCase() + "%"
        );
    }

    private List<MentorDTO> fetchMentors(String sql, String... params) {
        List<MentorDTO> list = new ArrayList<>();

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AcademicStaff mentor = new AcademicStaff();
                mentor.setId(rs.getInt("Id"));
                mentor.setTitle(rs.getString("Title"));
                mentor.setFirstName(rs.getString("FirstName"));
                mentor.setLastName(rs.getString("LastName"));
                mentor.setEmail(rs.getString("Email"));
                mentor.setIsDean(rs.getBoolean("IsDean"));
                mentor.setIsActive(rs.getBoolean("IsActive"));

                if (rs.getTimestamp("CreatedAt") != null) {
                    mentor.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                }
                if (rs.getTimestamp("UpdatedAt") != null) {
                    mentor.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
                }

                int studentCount = rs.getInt("StudentCount");
                int OngoingThesisCount = rs.getInt("OngoingThesisCount");
                list.add(new MentorDTO(mentor, studentCount, OngoingThesisCount));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void insertMentor(AcademicStaff mentor) {
        String sql = """
            INSERT INTO AcademicStaff 
            (Title, FirstName, LastName, Email,  CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mentor.getTitle());
            ps.setString(2, mentor.getFirstName());
            ps.setString(3, mentor.getLastName());
            ps.setString(4, mentor.getEmail());

            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMentor(AcademicStaff mentor) {
        String sql = """
            UPDATE AcademicStaff SET 
               Title=?, FirstName=?, LastName=?, Email=?, UpdatedAt=?
            WHERE Id=?
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, mentor.getTitle());
            ps.setString(2, mentor.getFirstName());
            ps.setString(3, mentor.getLastName());
            ps.setString(4, mentor.getEmail());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(6, mentor.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMentor(int id) {
        String sql = "UPDATE AcademicStaff SET IsActive = 0, UpdatedAt = ? WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    public boolean isEmailTaken(String email, int mentorIdToIgnore) {
        String sql = "SELECT COUNT(*) FROM AcademicStaff WHERE Email = ? AND Id != ?";
        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, mentorIdToIgnore);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
