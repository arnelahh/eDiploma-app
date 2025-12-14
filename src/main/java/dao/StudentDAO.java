package dao;

import model.Student;
import model.StudentStatus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private final StudentStatusDAO studentStatusDAO = new StudentStatusDAO();

    public List<Student> getAllStudents(){
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student ORDER BY Id DESC";
        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                StudentStatus status = studentStatusDAO.getStatusById(rs.getInt("StatusId"));
                Student student = new Student(rs.getInt("Id"), rs.getString("FirstName"),                 rs.getString("LastName"),
                        rs.getString("FatherName"),
                        rs.getInt("IndexNumber"),
                        rs.getDate("BirthDate") != null ? rs.getDate("BirthDate").toLocalDate() : null,
                        rs.getString("BirthPlace"), rs.getString("Municipality"),
                        rs.getString("Country"), rs.getString("StudyProgram"),
                        rs.getInt("ECTS"),
                        rs.getInt("Cycle"),
                        rs.getInt("CycleDuration"),
                        status,
                        rs.getString("Email"),
                        rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null,
                        rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null);
                students.add(student);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return students;
    }

    public List<Student> searchStudents(String searchTerm) {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM Student WHERE LOWER(FirstName) LIKE ? OR LOWER(LastName) LIKE ? OR CAST(IndexNumber AS CHAR) LIKE ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentStatus status = studentStatusDAO.getStatusById(rs.getInt("StatusId"));
                    Student student = new Student(rs.getInt("Id"), rs.getString("FirstName"), rs.getString("LastName"), rs.getString("FatherName"), rs.getInt("IndexNumber"), rs.getDate("BirthDate") != null ? rs.getDate("BirthDate").toLocalDate() : null, rs.getString("BirthPlace"), rs.getString("Municipality"), rs.getString("Country"), rs.getString("StudyProgram"), rs.getInt("ECTS"), rs.getInt("Cycle"), rs.getInt("CycleDuration"), status, rs.getString("Email"), rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null, rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null
                    );
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return students;
    }

    public void insertStudent(Student student) {
        String sql = "INSERT INTO Student (FirstName, LastName, FatherName, IndexNumber, BirthDate, " +
                "BirthPlace, Municipality, Country, StudyProgram, ECTS, Cycle, CycleDuration, " +
                "StatusId, Email, CreatedAt, UpdatedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getLastName());
            ps.setString(3, student.getFatherName());
            ps.setInt(4, student.getIndexNumber());
            ps.setDate(5, student.getBirthDate() != null ? Date.valueOf(student.getBirthDate()) : null);
            ps.setString(6, student.getBirthPlace());
            ps.setString(7, student.getMunicipality());
            ps.setString(8, student.getCountry());
            ps.setString(9, student.getStudyProgram());
            ps.setInt(10, student.getECTS());
            ps.setInt(11, student.getCycle());
            ps.setInt(12, student.getCycleDuration());
            ps.setInt(13, student.getStatus() != null ? student.getStatus().getId() : 1);
            ps.setString(14, student.getEmail());
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStudent(Student student) {
        String sql = "UPDATE Student SET FirstName = ?, LastName = ?, FatherName = ?, IndexNumber = ?, " +
                "BirthDate = ?, BirthPlace = ?, Municipality = ?, Country = ?, StudyProgram = ?, " +
                "ECTS = ?, Cycle = ?, CycleDuration = ?, StatusId = ?, Email = ?, UpdatedAt = ? WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getLastName());
            ps.setString(3, student.getFatherName());
            ps.setInt(4, student.getIndexNumber());
            ps.setDate(5, student.getBirthDate() != null ? Date.valueOf(student.getBirthDate()) : null);
            ps.setString(6, student.getBirthPlace());
            ps.setString(7, student.getMunicipality());
            ps.setString(8, student.getCountry());
            ps.setString(9, student.getStudyProgram());
            ps.setInt(10, student.getECTS());
            ps.setInt(11, student.getCycle());
            ps.setInt(12, student.getCycleDuration());
            ps.setInt(13, student.getStatus() != null ? student.getStatus().getId() : 1);
            ps.setString(14, student.getEmail());
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(16, student.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Student getStudentById(int id) {
        String sql = "SELECT * FROM Student WHERE Id = ?";
        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudentStatus status = studentStatusDAO.getStatusById(rs.getInt("StatusId"));
                    return new Student(rs.getInt("Id"), rs.getString("FirstName"), rs.getString("LastName"), rs.getString("FatherName"), rs.getInt("IndexNumber"), rs.getDate("BirthDate") != null ? rs.getDate("BirthDate").toLocalDate() : null, rs.getString("BirthPlace"), rs.getString("Municipality"), rs.getString("Country"), rs.getString("StudyProgram"), rs.getInt("ECTS"), rs.getInt("Cycle"), rs.getInt("CycleDuration"), status, rs.getString("Email"), rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null, rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
