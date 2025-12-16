package dao;

import dto.ThesisDTO;
import model.AcademicStaff;
import model.AppUser;
import model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppUserDAO {

    private static final String FIND_BY_EMAIL_SQL = """
        SELECT
            u.Id AS user_id,
            u.Username,
            u.Email,
            u.PasswordHash,
            u.IsActive,
            u.AppPassword,
            u.CreatedAt,
            u.UpdatedAt,

            r.Id AS role_id,
            r.Name AS role_name,

            s.Id AS staff_id,
            s.FirstName AS staff_first_name,
            s.LastName AS staff_last_name,
            s.Title AS staff_title

        FROM AppUser u
        JOIN UserRole r ON u.RoleId = r.Id
        LEFT JOIN AcademicStaff s ON u.AcademicStaffId = s.Id
        WHERE u.Email = ?
        LIMIT 1
        """;

    public AppUser findByEmail(String email) {

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_EMAIL_SQL)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user by email", e);
        }
        return null;
    }

    private AppUser mapRowToUser(ResultSet rs) throws SQLException {
        UserRole role = new UserRole(
                rs.getInt("role_id"),
                rs.getString("role_name")
        );

        AcademicStaff staff = null;
        int staffId = rs.getInt("staff_id");
        if (!rs.wasNull()) {
            staff = AcademicStaff.builder()
                    .Id(staffId)
                    .FirstName(rs.getString("staff_first_name"))
                    .LastName(rs.getString("staff_last_name"))
                    .Title(rs.getString("staff_title"))
                    .build();
        }
        return new AppUser(
                rs.getInt("user_id"),
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
    public List<AppUser> getAllAppUsers() {
        List<AppUser> users = new ArrayList<>();
        String sql = """
            Select * from AppUser
        """;
        try(Connection conn=CloudDatabaseConnection.Konekcija();
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery(sql);)
        {
            while (rs.next()) {
                AppUser user = new AppUser();
                user.setId(rs.getInt("Id"));
                user.setUsername(rs.getString("Username"));
                user.setEmail(rs.getString("Email"));
                user.setPasswordHash(rs.getString("PasswordHash"));
                users.add(user);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }


}
