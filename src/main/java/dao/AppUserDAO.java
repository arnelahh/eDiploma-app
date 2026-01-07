package dao;

import model.AppUser;
import model.UserRole;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class AppUserDAO {

    public AppUser findByUsername(String username) throws SQLException {
        String sql = "SELECT Id, RoleId, Username, Email, PasswordHash, AppPassword FROM AppUser WHERE Username = ? AND IsActive = 1";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserRole role = getUserRole(rs.getInt("RoleId"));
                    return new AppUser(
                            rs.getInt("Id"),
                            role,
                            rs.getString("Username"),
                            rs.getString("Email"),
                            rs.getString("PasswordHash"),
                            null,
                            null,
                            true,
                            rs.getString("AppPassword"),
                            null
                    );
                }
            }
        }
        return null;
    }

    private UserRole getUserRole(int roleId) throws SQLException {
        String sql = "SELECT Id, Name FROM UserRole WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserRole(rs.getInt("Id"), rs.getString("Name"));
                }
            }
        }
        return null;
    }

    public void updatePassword(int userId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE AppUser SET PasswordHash = ?, UpdatedAt = ? WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPasswordHash);
            ps.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(3, userId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to update password. User not found.");
            }
        }
    }

    public void updateAppPassword(int userId, String hashedAppPassword) throws SQLException {
        String sql = "UPDATE AppUser SET AppPassword = ?, UpdatedAt = ? WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashedAppPassword);
            ps.setTimestamp(2, Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setInt(3, userId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Failed to update app password. User not found.");
            }
        }
    }
}
