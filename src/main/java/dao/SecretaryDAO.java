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

    private static final String BASE_QUERY = """
        SELECT u.*, r.Id AS role_id, r.Name AS role_name,
               a.Id AS staff_id, a.FirstName AS staff_first_name, 
               a.LastName AS staff_last_name, a.Title AS staff_title, a.Email AS staff_email,
        (SELECT COUNT(*) FROM Thesis t WHERE t.SecretaryId = u.Id AND t.IsActive = 1) AS ThesisCount
        FROM AppUser u
        JOIN UserRole r ON u.RoleId = r.Id
        LEFT JOIN AcademicStaff a ON u.AcademicStaffId = a.Id
        WHERE u.IsActive = 1
        """;

    public List<SecretaryDTO> getAllSecretaries() {
        return fetchSecretaries(BASE_QUERY + " ORDER BY u.Id DESC");
    }

    public List<SecretaryDTO> searchSecretaries(String term) {
        String sql = BASE_QUERY + """
            AND (LOWER(u.Username) LIKE ?
               OR LOWER(u.Email) LIKE ?
               OR LOWER(a.FirstName) LIKE ?
               OR LOWER(a.LastName) LIKE ?)
            ORDER BY u.Id DESC
            """;

        return fetchSecretaries(sql,
                "%" + term.toLowerCase() + "%",
                "%" + term.toLowerCase() + "%",
                "%" + term.toLowerCase() + "%",
                "%" + term.toLowerCase() + "%"
        );
    }

    private List<SecretaryDTO> fetchSecretaries(String sql, String... params) {
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

                // Kreiraj AcademicStaff ako postoji
                AcademicStaff staff = null;
                if (rs.getObject("staff_id") != null) {
                    staff = new AcademicStaff();
                    staff.setId(rs.getInt("staff_id"));
                    staff.setFirstName(rs.getString("staff_first_name"));
                    staff.setLastName(rs.getString("staff_last_name"));
                    staff.setTitle(rs.getString("staff_title"));
                    staff.setEmail(rs.getString("staff_email"));
                }

                AppUser secretary = new AppUser();
                secretary.setId(rs.getInt("Id"));
                secretary.setUsername(rs.getString("Username"));
                secretary.setEmail(rs.getString("Email"));
                secretary.setPasswordHash(rs.getString("PasswordHash"));
                secretary.setActive(rs.getBoolean("IsActive"));
                secretary.setRole(role);
                secretary.setAcademicStaff(staff);

                if (rs.getTimestamp("CreatedAt") != null) {
                    secretary.setCreatedAt(rs.getTimestamp("CreatedAt").toLocalDateTime());
                }
                if (rs.getTimestamp("UpdatedAt") != null) {
                    secretary.setUpdatedAt(rs.getTimestamp("UpdatedAt").toLocalDateTime());
                }

                int thesisCount = rs.getInt("ThesisCount");
                list.add(new SecretaryDTO(secretary, thesisCount));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void insertSecretary(AppUser secretary) {
        Connection conn = null;
        try {
            conn = CloudDatabaseConnection.Konekcija();
            conn.setAutoCommit(false);

            Integer academicStaffId = null;

            // Prvo insertuj AcademicStaff ako postoji
            if (secretary.getAcademicStaff() != null) {
                String staffSql = """
                    INSERT INTO AcademicStaff 
                    (Title, FirstName, LastName, Email, IsDean, IsActive, CreatedAt, UpdatedAt)
                    VALUES (?, ?, ?, ?, 0, 1, ?, ?)
                    """;

                try (PreparedStatement staffPs = conn.prepareStatement(staffSql,
                        Statement.RETURN_GENERATED_KEYS)) {
                    AcademicStaff staff = secretary.getAcademicStaff();
                    staffPs.setString(1, staff.getTitle());
                    staffPs.setString(2, staff.getFirstName());
                    staffPs.setString(3, staff.getLastName());
                    staffPs.setString(4, staff.getEmail());
                    staffPs.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                    staffPs.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                    staffPs.executeUpdate();

                    ResultSet generatedKeys = staffPs.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        academicStaffId = generatedKeys.getInt(1);
                    }
                }
            }

            // Zatim insertuj AppUser
            String userSql = """
                INSERT INTO AppUser 
                (Username, Email, PasswordHash, RoleId, AcademicStaffId, IsActive, CreatedAt, UpdatedAt)
                VALUES (?, ?, ?, 1, ?, 1, ?, ?)
                """;

            try (PreparedStatement userPs = conn.prepareStatement(userSql)) {
                userPs.setString(1, secretary.getUsername());
                userPs.setString(2, secretary.getEmail());
                userPs.setString(3, "$2a$10$defaultHashForNewSecretaries");

                if (academicStaffId != null) {
                    userPs.setInt(4, academicStaffId);
                } else {
                    userPs.setNull(4, Types.INTEGER);
                }

                userPs.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                userPs.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                userPs.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Rollback failed", ex);
                }
            }
            throw new RuntimeException("Insert failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void updateSecretary(AppUser secretary) {
        Connection conn = null;
        try {
            conn = CloudDatabaseConnection.Konekcija();
            conn.setAutoCommit(false);

            // Ažuriraj ili kreiraj AcademicStaff
            if (secretary.getAcademicStaff() != null) {
                AcademicStaff staff = secretary.getAcademicStaff();

                if (staff.getId() > 0) {
                    // Update existing
                    String staffUpdateSql = """
                        UPDATE AcademicStaff SET 
                           Title=?, FirstName=?, LastName=?, Email=?, UpdatedAt=?
                        WHERE Id=?
                        """;

                    try (PreparedStatement ps = conn.prepareStatement(staffUpdateSql)) {
                        ps.setString(1, staff.getTitle());
                        ps.setString(2, staff.getFirstName());
                        ps.setString(3, staff.getLastName());
                        ps.setString(4, staff.getEmail());
                        ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                        ps.setInt(6, staff.getId());
                        ps.executeUpdate();
                    }
                } else {
                    // Insert new
                    String staffInsertSql = """
                        INSERT INTO AcademicStaff 
                        (Title, FirstName, LastName, Email, IsDean, IsActive, CreatedAt, UpdatedAt)
                        VALUES (?, ?, ?, ?, 0, 1, ?, ?)
                        """;

                    try (PreparedStatement ps = conn.prepareStatement(staffInsertSql,
                            Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, staff.getTitle());
                        ps.setString(2, staff.getFirstName());
                        ps.setString(3, staff.getLastName());
                        ps.setString(4, staff.getEmail());
                        ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                        ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                        ps.executeUpdate();

                        ResultSet keys = ps.getGeneratedKeys();
                        if (keys.next()) {
                            int newStaffId = keys.getInt(1);

                            // Update AppUser to link to this new AcademicStaff
                            String linkSql = "UPDATE AppUser SET AcademicStaffId = ? WHERE Id = ?";
                            try (PreparedStatement linkPs = conn.prepareStatement(linkSql)) {
                                linkPs.setInt(1, newStaffId);
                                linkPs.setInt(2, secretary.getId());
                                linkPs.executeUpdate();
                            }
                        }
                    }
                }
            }

            // Ažuriraj AppUser
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
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Rollback failed", ex);
                }
            }
            throw new RuntimeException("Update failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void deleteSecretary(int id) {
        String sql = "UPDATE AppUser SET IsActive = 0, UpdatedAt = ? WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}