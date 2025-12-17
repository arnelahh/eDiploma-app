package dao;

import dto.SecretaryDTO;
import model.AppUser;
import model.UserRole;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SecretaryDAO {

    private static final String BASE_QUERY = """
        SELECT u.*, r.Id AS role_id, r.Name AS role_name,
        (SELECT COUNT(*) FROM Thesis t WHERE t.SecretaryId = u.Id AND t.IsActive = 1) AS ThesisCount
        FROM AppUser u
        JOIN UserRole r ON u.RoleId = r.Id
        WHERE u.IsActive = 1
        """;

    public List<SecretaryDTO> getAllSecretaries() {
        return fetchSecretaries(BASE_QUERY + " ORDER BY u.Id DESC");
    }

    public List<SecretaryDTO> searchSecretaries(String term) {
        String sql = BASE_QUERY + """
            AND (LOWER(u.Username) LIKE ?
               OR LOWER(u.Email) LIKE ?)
            ORDER BY u.Id DESC
            """;

        return fetchSecretaries(sql,
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

                AppUser secretary = new AppUser();
                secretary.setId(rs.getInt("Id"));
                secretary.setUsername(rs.getString("Username"));
                secretary.setEmail(rs.getString("Email"));
                secretary.setPasswordHash(rs.getString("PasswordHash"));
                secretary.setActive(rs.getBoolean("IsActive"));
                secretary.setRole(role);

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
        String sql = """
            INSERT INTO AppUser 
            (Username, Email, PasswordHash, RoleId, IsActive, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, 1, 1, ?, ?)
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, secretary.getUsername());
            ps.setString(2, secretary.getEmail());
            // Default password hash - u produkciji bi se generisao pravi hash
            ps.setString(3, "$2a$10$defaultHashForNewSecretaries");
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateSecretary(AppUser secretary) {
        String sql = """
            UPDATE AppUser SET 
               Username=?, Email=?, UpdatedAt=?
            WHERE Id=?
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, secretary.getUsername());
            ps.setString(2, secretary.getEmail());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, secretary.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
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