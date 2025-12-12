package dao;

import model.AcademicStaff;
import model.AppUser;
import model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppUserDAO {
    private final UserRoleDAO roleDAO = new UserRoleDAO();
    private final AcademicStaffDAO staffDAO = new AcademicStaffDAO();

    public List<AppUser> getAllUsers() {
        List<AppUser> users = new ArrayList<>();
        String sql = "SELECT * FROM AppUser";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {

                UserRole role = roleDAO.getRoleById(rs.getInt("RoleId"));
                AcademicStaff staff = staffDAO.getStaffById(rs.getInt("AcademicStaffId"));
                AppUser user = new AppUser(
                        rs.getInt("Id"),
                        role,
                        rs.getString("Username"),
                        rs.getString("Email"),
                        rs.getString("PasswordHash"),
                        rs.getTimestamp("CreatedAt").toLocalDateTime(),
                        rs.getTimestamp("UpdatedAt").toLocalDateTime(),
                        rs.getBoolean("IsActive"),
                        rs.getString("AppPassword"),
                        staff
                );

                users.add(user);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return users;
    }
    public AppUser findByEmail(String email) {

        String sql = "SELECT * FROM AppUser WHERE Email = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    UserRole role = roleDAO.getRoleById(rs.getInt("RoleId"));
                    AcademicStaff staff = staffDAO.getStaffById(rs.getInt("AcademicStaffId"));

                    return new AppUser(
                            rs.getInt("Id"),
                            role,
                            rs.getString("Username"),
                            rs.getString("Email"),
                            rs.getString("PasswordHash"),
                            rs.getTimestamp("CreatedAt").toLocalDateTime(),
                            rs.getTimestamp("UpdatedAt").toLocalDateTime(),
                            rs.getBoolean("IsActive"),
                            rs.getString("AppPassword"),
                            staff
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null; // korisnik ne postoji
    }
}
