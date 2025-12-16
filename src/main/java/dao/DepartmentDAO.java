package dao;

import model.Department;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM Department ORDER BY Name";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Department dept = new Department();
                dept.setId(rs.getInt("Id"));
                dept.setName(rs.getString("Name"));
                departments.add(dept);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri učitavanju odjela: " + e.getMessage(), e);
        }

        return departments;
    }

    public Department getDepartmentById(int id) {
        String sql = "SELECT * FROM Department WHERE Id = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Department dept = new Department();
                dept.setId(rs.getInt("Id"));
                dept.setName(rs.getString("Name"));
                return dept;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatanju odjela: " + e.getMessage(), e);
        }

        return null;
    }
}