package dao;

import dto.SecretaryDTO;
import model.AcademicStaff;
import model.AppUser;
import model.UserRole;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SecretaryDAO {

    private static final int SECRETARY_ROLE_ID = 2;

    private static final String BASE_QUERY = """
        SELECT 
            u.Id                AS user_id,
            u.Username,
            u.Email             AS user_email,
            u.PasswordHash,
            u.IsActive           AS user_active,
            u.CreatedAt          AS user_created,
            u.UpdatedAt          AS user_updated,

            r.Id                 AS role_id,
            r.Name               AS role_name,

            a.Id                 AS staff_id,
            a.FirstName,
            a.LastName,
            a.Title,
            a.Email              AS staff_email,
            a.IsSecretary,
            a.IsDean,
            a.IsActive           AS staff_active,

            (SELECT COUNT(*) 
             FROM Thesis t 
             WHERE t.SecretaryId = u.Id AND t.IsActive = 1) AS ThesisCount

        FROM AppUser u
        JOIN UserRole r ON u.RoleId = r.Id
        JOIN AcademicStaff a ON u.AcademicStaffId = a.Id
        WHERE u.IsActive = 1
          AND r.Id = 2
          AND a.IsSecretary = 1
        """;

    /* ===================== READ ===================== */

    public List<SecretaryDTO> getAllSecretaries() {
        return fetch(BASE_QUERY + " ORDER BY u.Id DESC");
    }

    public List<SecretaryDTO> searchSecretaries(String term) {
        String sql = BASE_QUERY + """
            AND (
                LOWER(u.Username) LIKE ?
                OR LOWER(u.Email) LIKE ?
                OR LOWER(a.FirstName) LIKE ?
                OR LOWER(a.LastName) LIKE ?
            )
            ORDER BY u.Id DESC
            """;

        String like = "%" + term.toLowerCase() + "%";
        return fetch(sql, like, like, like, like);
    }

    private List<SecretaryDTO> fetch(String sql, String... params) {
        List<SecretaryDTO> list = new ArrayList<>();

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                UserRole role = new UserRole();
                role.setId(rs.getInt("role_id"));
                role.setName(rs.getString("role_name"));

                AcademicStaff staff = new AcademicStaff();
                staff.setId(rs.getInt("staff_id"));
                staff.setFirstName(rs.getString("FirstName"));
                staff.setLastName(rs.getString("LastName"));
                staff.setTitle(rs.getString("Title"));
                staff.setEmail(rs.getString("staff_email"));
                staff.setIsSecretary(rs.getBoolean("IsSecretary"));
                staff.setIsDean(rs.getBoolean("IsDean"));
                staff.setIsActive(rs.getBoolean("staff_active"));

                AppUser user = new AppUser();
                user.setId(rs.getInt("user_id"));
                user.setUsername(rs.getString("Username"));
                user.setEmail(rs.getString("user_email"));
                user.setPasswordHash(rs.getString("PasswordHash"));
                user.setActive(rs.getBoolean("user_active"));
                user.setRole(role);
                user.setAcademicStaff(staff);

                if (rs.getTimestamp("user_created") != null) {
                    user.setCreatedAt(rs.getTimestamp("user_created").toLocalDateTime());
                }
                if (rs.getTimestamp("user_updated") != null) {
                    user.setUpdatedAt(rs.getTimestamp("user_updated").toLocalDateTime());
                }

                list.add(new SecretaryDTO(user.getId(),user, rs.getInt("ThesisCount")));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /* ===================== INSERT ===================== */

    public void insertSecretary(AppUser secretary) {
        Connection conn = null;

        try {
            conn = CloudDatabaseConnection.Konekcija();
            conn.setAutoCommit(false);

            AcademicStaff staff = secretary.getAcademicStaff();
            Integer staffId;

            String staffSql = """
                INSERT INTO AcademicStaff
                (FirstName, LastName, Title, Email, IsSecretary, IsDean, IsActive, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, 1, 0, 1, ?, ?)
                """;

            try (PreparedStatement ps = conn.prepareStatement(
                    staffSql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, staff.getFirstName());
                ps.setString(2, staff.getLastName());
                ps.setString(3, staff.getTitle());
                ps.setString(4, staff.getEmail());
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                keys.next();
                staffId = keys.getInt(1);
            }

            String userSql = """
                INSERT INTO AppUser
                (Username, Email, PasswordHash, RoleId, AcademicStaffId, IsActive, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, ?, ?, 1, ?, ?)
                """;

            try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                ps.setString(1, secretary.getUsername());
                ps.setString(2, secretary.getEmail());
                ps.setString(3, "$2a$10$defaultHash");
                ps.setInt(4, SECRETARY_ROLE_ID);
                ps.setInt(5, staffId);
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                ps.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }
    }

    /* ===================== UPDATE ===================== */

    public void updateSecretary(AppUser secretary) {
        Connection conn = null;

        try {
            conn = CloudDatabaseConnection.Konekcija();
            conn.setAutoCommit(false);

            AcademicStaff s = secretary.getAcademicStaff();

            String staffSql = """
                UPDATE AcademicStaff SET
                    FirstName=?, LastName=?, Title=?, Email=?, UpdatedAt=?
                WHERE Id=?
                """;

            try (PreparedStatement ps = conn.prepareStatement(staffSql)) {
                ps.setString(1, s.getFirstName());
                ps.setString(2, s.getLastName());
                ps.setString(3, s.getTitle());
                ps.setString(4, s.getEmail());
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(6, s.getId());
                ps.executeUpdate();
            }

            String userSql = """
                UPDATE AppUser SET
                    Username=?, Email=?, UpdatedAt=?
                WHERE Id=?
                """;

            try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                ps.setString(1, secretary.getUsername());
                ps.setString(2, secretary.getEmail());
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(4, secretary.getId());
                ps.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException(e);
        } finally {
            close(conn);
        }
    }

    /* ===================== DELETE ===================== */

    public void deleteSecretary(int userId) {
        String sql = """
            UPDATE AppUser SET IsActive=0, UpdatedAt=?
            WHERE Id=?
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, userId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* ===================== HELPERS ===================== */

    private void rollback(Connection conn) {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException ignored) {}
    }

    private void close(Connection conn) {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException ignored) {}
    }
}
