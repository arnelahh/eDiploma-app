package dao;

import model.AcademicStaff;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AcademicStaffDAO {

    public List<AcademicStaff> getAllActiveAcademicStaff() {
        List<AcademicStaff> staffList = new ArrayList<>();
        String sqlUpit = """
select * from AcademicStaff as A
where not EXISTS
  (
    Select 1
    from AppUser AP
    WHERE AP.AcademicStaffId=A.Id
  ) AND IsActive=1;
    
""";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlUpit)) {

            while (rs.next()) {
                AcademicStaff as = new AcademicStaff();
                as.setId(rs.getInt("Id"));
                as.setTitle(rs.getString("Title"));
                as.setFirstName(rs.getString("FirstName"));
                as.setLastName(rs.getString("LastName"));
                as.setEmail(rs.getString("Email"));
                as.setIsDean(rs.getBoolean("IsDean"));
                as.setIsActive(rs.getBoolean("IsActive"));

                if (rs.getTimestamp("CreatedAt") != null) {
                    as.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                }
                if (rs.getTimestamp("UpdatedAt") != null) {
                    as.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
                }

                staffList.add(as);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return staffList;
    }

}
