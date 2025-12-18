package dao;

import model.AcademicStaff;

import java.sql.*;
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
                as.setIsDean(rs.getBoolean("IsDean"));
                as.setIsActive(rs.getBoolean("IsActive"));
                as.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                as.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
                as.setIsSecretary(rs.getBoolean("IsSecretary"));
                staffList.add(as);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return staffList;
    }
    public AcademicStaff getStaffById(int id) {
        String sql = "SELECT * FROM AcademicStaff WHERE Id = ?";
        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
                    as.setIsSecretary(rs.getBoolean("IsSecretary"));
                    return as;
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AcademicStaff> getAllActiveAcademicStaff() {
        List<AcademicStaff> staffList = new ArrayList<>();
        String sqlUpit = "SELECT * FROM AcademicStaff WHERE IsActive = 1";

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
                as.setIsSecretary(rs.getBoolean("IsSecretary"));
                staffList.add(as);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return staffList;
    }

    public List<AcademicStaff> getAllActiveProfessors() {
        List<AcademicStaff> staffList = new ArrayList<>();
        String sqlUpit = "SELECT * FROM AcademicStaff WHERE IsActive = 1 AND IsSecretary = 0";

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
                as.setIsSecretary(rs.getBoolean("IsSecretary"));
                staffList.add(as);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return staffList;
    }
}
