package dao;

import model.CommissionRole;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CommissionRoleDAO {
    public List<CommissionRole> getCommissionRoles() {
        List<CommissionRole> roles = new ArrayList<>();
        String sql = "SELECT * FROM CommissionRole";
        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                CommissionRole role = new CommissionRole();
                role.setId(rs.getInt("Id"));
                role.setName(rs.getString("Name"));
                roles.add(role);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return roles;
    }
}
