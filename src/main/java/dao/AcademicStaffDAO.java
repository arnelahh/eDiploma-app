package dao;

import model.AcademicStaff;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AcademicStaffDAO {
    public List<AcademicStaff> getAllAcademicStaff() {
        List<AcademicStaff> staffList = new ArrayList<>();
        String sqlUpit = "SELECT * FROM AcademicStaff";
        try(Connection conn = CloudDatabaseConnection.Konekcija();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlUpit)) {
            while (rs.next()) {
                AcademicStaff as = new AcademicStaff();
                as.setId(rs.getInt("Id"));
                as.setTitle(rs.getString("Title"));
                as.setFirstName(rs.getString("FirstName"));
                as.setLastName(rs.getString("LastName"));
                as.setEmail(rs.getString("Email"));
                as.setDean(rs.getBoolean("IsDean"));
                as.setActive(rs.getBoolean("IsActive"));
                as.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                as.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
                staffList.add(as);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return staffList;
    }
}
