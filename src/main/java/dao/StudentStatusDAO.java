package dao;

import model.StudentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentStatusDAO {

    public List<StudentStatus> getAllStatuses() {
        List<StudentStatus> statuses = new ArrayList<>();
        String sql = "SELECT * FROM StudentStatus";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                StudentStatus status = new StudentStatus();
                status.setId(rs.getInt("Id"));
                status.setName(rs.getString("Name"));
                statuses.add(status);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return statuses;
    }
}
