package dao;

import model.StudentStatus;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentStatusDAO {
    public StudentStatus getStatusById(int id){
        String sql = "SELECT * FROM StudentStatus WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                StudentStatus status = new StudentStatus();
                status.setId(rs.getInt("Id"));
                status.setName(rs.getString("Name"));
                return status;
            }
    } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
