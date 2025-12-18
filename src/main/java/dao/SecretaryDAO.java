package dao;

import dto.CreateSecretaryDTO;
import dto.CreateUserIdsDTO;
import dto.SecretaryDTO;
import model.AcademicStaff;
import model.AppUser;
import model.UserRole;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SecretaryDAO {

    public List<SecretaryDTO> getAllSecretaries() {
        String sql = """
            SELECT
                au.Id              AS appUserId,
                au.Username        AS username,
                au.Email           AS userEmail,
                au.PasswordHash    AS passwordHash,
                au.IsActive        AS userActive,
                au.CreatedAt       AS userCreatedAt,
                au.UpdatedAt       AS userUpdatedAt,

                ur.Id              AS roleId,
                ur.Name            AS roleName,

                s.Id               AS staffId,
                s.Title            AS staffTitle,
                s.FirstName        AS staffFirstName,
                s.LastName         AS staffLastName,
                s.Email            AS staffEmail,
                s.IsDean           AS staffIsDean,
                s.IsActive         AS staffIsActive,
                s.CreatedAt        AS staffCreatedAt,
                s.UpdatedAt        AS staffUpdatedAt

            FROM AppUser au
            JOIN UserRole ur ON ur.Id = au.RoleId
            JOIN AcademicStaff s ON s.Id = au.AcademicStaffId
            WHERE ur.Name = 'Secretary'
            ORDER BY s.LastName, s.FirstName
        """;

        List<SecretaryDTO> list = new ArrayList<>();

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                AcademicStaff staff = AcademicStaff.builder()
                        .Id(rs.getInt("staffId"))
                        .Title(rs.getString("staffTitle"))
                        .FirstName(rs.getString("staffFirstName"))
                        .LastName(rs.getString("staffLastName"))
                        .Email(rs.getString("staffEmail"))
                        .IsDean(rs.getBoolean("staffIsDean"))
                        .IsActive(rs.getBoolean("staffIsActive"))
                        .CreatedAt(toLdt(rs.getTimestamp("staffCreatedAt")))
                        .UpdatedAt(toLdt(rs.getTimestamp("staffUpdatedAt")))
                        .build();

                UserRole role = new UserRole(
                        rs.getInt("roleId"),
                        rs.getString("roleName")
                );

                AppUser user = new AppUser(
                        rs.getInt("appUserId"),
                        role,
                        rs.getString("username"),
                        rs.getString("userEmail"),
                        rs.getString("passwordHash"),
                        toLdt(rs.getTimestamp("userCreatedAt")),
                        toLdt(rs.getTimestamp("userUpdatedAt")),
                        rs.getBoolean("userActive"),
                        null, // AppPassword not used now
                        staff
                );

                list.add(SecretaryDTO.builder()
                        .secretary(staff)
                        .user(user)
                        .build());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private LocalDateTime toLdt(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    // Create both AcademicStaff + AppUser in one transaction
    public CreateUserIdsDTO createSecretary(CreateSecretaryDTO dto) throws SQLException {
        String insertStaffSql = """
            INSERT INTO AcademicStaff (Title, FirstName, LastName, Email, IsDean, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, 0, ?, ?)
        """;

        // Optimized: get RoleId with a subquery instead of separate SELECT roundtrip
        String insertUserSql = """
            INSERT INTO AppUser (RoleId, Username, Email, PasswordHash, CreatedAt, UpdatedAt, AppPassword, AcademicStaffId)
            VALUES ((SELECT Id FROM UserRole WHERE Name = 'Secretary'), ?, ?, ?, ?, ?, ?, ?)
        """;

        // Optional but recommended uniqueness pre-checks (fast + friendly errors)
        String existsUserSql = "SELECT 1 FROM AppUser WHERE Username = ? OR Email = ? LIMIT 1";
        String existsStaffSql = "SELECT 1 FROM AcademicStaff WHERE Email = ? LIMIT 1";

        try (Connection conn = CloudDatabaseConnection.Konekcija()) { // replace with your connection provider
            conn.setAutoCommit(false);

            try {
                // 1) quick uniqueness checks (optional but practical)
                try (PreparedStatement ps = conn.prepareStatement(existsStaffSql)) {
                    ps.setString(1, dto.getEmail());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("AcademicStaff email already exists.");
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(existsUserSql)) {
                    ps.setString(1, dto.getUsername());
                    ps.setString(2, dto.getEmail());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) throw new SQLException("AppUser username/email already exists.");
                    }
                }

                // 2) insert AcademicStaff
                int staffId;
                LocalDateTime now = LocalDateTime.now();

                try (PreparedStatement ps = conn.prepareStatement(insertStaffSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, dto.getTitle());
                    ps.setString(2, dto.getFirstName());
                    ps.setString(3, dto.getLastName());
                    ps.setString(4, dto.getEmail());
                    ps.setTimestamp(5, Timestamp.valueOf(now));
                    ps.setTimestamp(6, Timestamp.valueOf(now));

                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Failed to create AcademicStaff (no generated key).");
                        staffId = keys.getInt(1);
                    }
                }

                // 3) insert AppUser (role resolved in SQL)
                int userId;
                String passwordHash = BCrypt.hashpw(dto.getRawPassword(), BCrypt.gensalt(12)); // implement below

                try (PreparedStatement ps = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, dto.getUsername());
                    ps.setString(2, dto.getEmail());
                    ps.setString(3, passwordHash);
                    ps.setTimestamp(4, Timestamp.valueOf(now));
                    ps.setTimestamp(5, Timestamp.valueOf(now));
                    ps.setString(6, "");
                    ps.setInt(7, staffId);

                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Failed to create AppUser (no generated key).");
                        userId = keys.getInt(1);
                    }
                }

                conn.commit();
                return CreateUserIdsDTO.builder()
                        .academicStaffId(staffId)
                        .appUserId(userId)
                        .build();

            } catch (Exception ex) {
                conn.rollback();
                if (ex instanceof SQLException) throw (SQLException) ex;
                throw new SQLException("Create secretary failed: " + ex.getMessage(), ex);
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}