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


    public void addDepartment(Department department) {
        String sql = "INSERT INTO Department (Name) VALUES (?)";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, department.getName());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Kreiranje katedre (odjela) nije uspjelo, nijedan red nije promijenjen.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    department.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Kreiranje katedre (odjela) nije uspjelo, ID nije dobijen.");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dodavanju odjela: " + e.getMessage(), e);
        }
    }
}