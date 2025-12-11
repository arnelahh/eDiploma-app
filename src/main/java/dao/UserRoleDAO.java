package dao;

import model.UserRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRoleDAO {
    public UserRole getRoleById(int id) {
        String sql = "SELECT * FROM UserRole WHERE Id = ?";
        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new UserRole(
                        rs.getInt("Id"),
                        rs.getString("Name")
                );
            }

        }  catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<UserRole> getAllRoles() {
        List<UserRole> roles = new ArrayList<>();
        String sql = "SELECT * FROM UserRole";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                roles.add(new UserRole(
                        rs.getInt("Id"),
                        rs.getString("Name")
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return roles;
    }
}
