package dao;

import model.StudentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentStatusDAO {

    public StudentStatus getStatusById(int id) {
        String sql = "SELECT * FROM StudentStatus WHERE Id = ?";
        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    StudentStatus status = new StudentStatus();
                    status.setId(rs.getInt("Id"));
                    status.setName(rs.getString("Name"));
                    return status;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

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
