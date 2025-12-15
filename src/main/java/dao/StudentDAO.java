package dao;

import model.Student;
import model.StudentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    private static final String BASE_QUERY = """
        SELECT s.*, st.Id AS status_id, st.Name AS status_name
        FROM Student s
        LEFT JOIN StudentStatus st ON s.StatusId = st.Id
        """;

    public List<Student> getAllStudents() {
        return fetchStudents(BASE_QUERY + " ORDER BY s.Id DESC");
    }

    public List<Student> searchStudents(String term) {
        String sql = BASE_QUERY + """
            WHERE LOWER(s.FirstName) LIKE ?
               OR LOWER(s.LastName) LIKE ?
               OR CAST(s.IndexNumber AS CHAR) LIKE ?
            """;

        return fetchStudents(sql, "%" + term.toLowerCase() + "%");
    }

    private List<Student> fetchStudents(String sql, String... params) {
        List<Student> list = new ArrayList<>();

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StudentStatus status = new StudentStatus(
                        rs.getInt("status_id"),
                        rs.getString("status_name")
                );

                list.add(new Student(
                        rs.getInt("Id"),
                        rs.getString("FirstName"),
                        rs.getString("LastName"),
                        rs.getString("FatherName"),
                        rs.getInt("IndexNumber"),
                        rs.getDate("BirthDate") != null
                                ? rs.getDate("BirthDate").toLocalDate()
                                : null,
                        rs.getString("BirthPlace"),
                        rs.getString("Municipality"),
                        rs.getString("Country"),
                        rs.getString("StudyProgram"),
                        rs.getInt("ECTS"),
                        rs.getInt("Cycle"),
                        rs.getInt("CycleDuration"),
                        status,
                        rs.getString("Email"),
                        rs.getTimestamp("CreatedAt") != null
                                ? rs.getTimestamp("CreatedAt").toLocalDateTime()
                                : null,
                        rs.getTimestamp("UpdatedAt") != null
                                ? rs.getTimestamp("UpdatedAt").toLocalDateTime()
                                : null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void insertStudent(Student s) {
        String sql = """
            INSERT INTO Student
            (FirstName, LastName, FatherName, IndexNumber, BirthDate,
             BirthPlace, Municipality, Country, StudyProgram,
             ECTS, Cycle, CycleDuration, StatusId, Email, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            fillStatement(ps, s);
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStudent(Student s) {
        String sql = """
            UPDATE Student SET
                FirstName=?, LastName=?, FatherName=?, IndexNumber=?, BirthDate=?,
                BirthPlace=?, Municipality=?, Country=?, StudyProgram=?,
                ECTS=?, Cycle=?, CycleDuration=?, StatusId=?, Email=?, UpdatedAt=?
            WHERE Id=?
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            fillStatement(ps, s);
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(16, s.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillStatement(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getFirstName());
        ps.setString(2, s.getLastName());
        ps.setString(3, s.getFatherName());
        ps.setInt(4, s.getIndexNumber());
        ps.setDate(5, s.getBirthDate() != null ? Date.valueOf(s.getBirthDate()) : null);
        ps.setString(6, s.getBirthPlace());
        ps.setString(7, s.getMunicipality());
        ps.setString(8, s.getCountry());
        ps.setString(9, s.getStudyProgram());
        ps.setInt(10, s.getECTS());
        ps.setInt(11, s.getCycle());
        ps.setInt(12, s.getCycleDuration());
        ps.setInt(13, s.getStatus() != null ? s.getStatus().getId() : 1);
        ps.setString(14, s.getEmail());
    }
}
