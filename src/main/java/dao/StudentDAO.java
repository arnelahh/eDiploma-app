package dao;

import model.Student;
import model.StudentStatus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {
    private final StudentStatusDAO studentStatusDAO = new StudentStatusDAO();

    public List<Student> getAllStudents(){
        List<Student> students = new ArrayList<>();
        String sqlUpit = "SELECT * FROM Student";
        try(Connection conn = CloudDatabaseConnection.Konekcija();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlUpit)) {
            while(rs.next()){
                StudentStatus status = studentStatusDAO.getStatusById(rs.getInt("Id"));
                Student s = new Student(
                rs.getInt("Id"),
                rs.getString("FirstName"),
                rs.getString("LastName"),
                rs.getString("FatherName"),
                rs.getInt("IndexNumber"),
                rs.getDate("BirthDate").toLocalDate(),
                rs.getString("BirthPlace"),
                rs.getString("Municipality"),
                rs.getString("Country"),
                rs.getString("StudyProgram"),
                rs.getInt("ECTS"),
                rs.getInt("Cycle"),
                rs.getInt("CycleDuration"),
                status,
                rs.getString("Email"),
                rs.getTimestamp("CreatedAt").toLocalDateTime(),
                rs.getTimestamp("UpdatedAt").toLocalDateTime()
            );
                students.add(s);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return students;
    }
}
