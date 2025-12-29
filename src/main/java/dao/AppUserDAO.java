package dao;

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
    public List<AcademicStaff> getAllSecretariesAsStaff() {
        List<AcademicStaff> secretaries = new ArrayList<>();

        String sql = """
        SELECT 
            s.Id, s.Title, s.FirstName, s.LastName, s.Email,
            s.IsDean, s.IsActive, s.CreatedAt, s.UpdatedAt
        FROM AcademicStaff s
        JOIN AppUser u ON u.AcademicStaffId = s.Id
        JOIN UserRole r ON r.Id = u.RoleId
        WHERE r.Name = 'Secretary' AND u.IsActive = 1
        ORDER BY s.LastName, s.FirstName
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AcademicStaff staff = AcademicStaff.builder()
                        .Id(rs.getInt("Id"))
                        .Title(rs.getString("Title"))
                        .FirstName(rs.getString("FirstName"))
                        .LastName(rs.getString("LastName"))
                        .Email(rs.getString("Email"))
                        .IsDean(rs.getBoolean("IsDean"))
                        .IsActive(rs.getBoolean("IsActive"))
                        .CreatedAt(rs.getTimestamp("CreatedAt") != null ?
                                rs.getTimestamp("CreatedAt").toLocalDateTime() : null)
                        .UpdatedAt(rs.getTimestamp("UpdatedAt") != null ?
                                rs.getTimestamp("UpdatedAt").toLocalDateTime() : null)
                        .build();

                secretaries.add(staff);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load secretaries as staff", e);
        }

        return secretaries;
    }
    public int getAppUserIdByAcademicStaffId(int academicStaffId) {
        String sql = """
        SELECT Id 
        FROM AppUser 
        WHERE AcademicStaffId = ? AND IsActive = 1
        LIMIT 1
    """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, academicStaffId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Id");
                } else {
                    throw new RuntimeException("AppUser not found for AcademicStaff ID: " + academicStaffId);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find AppUser ID for AcademicStaff", e);
        }
    }

    public String getUserNameById(int userId) {
        String sql = "SELECT FirstName, LastName FROM AppUser WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("FirstName") + " " + rs.getString("LastName");
            }
            return "Nepoznat korisnik";

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju korisnika", e);
        }
    }
}
