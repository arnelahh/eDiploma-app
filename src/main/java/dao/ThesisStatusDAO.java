package dao;

import model.ThesisStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThesisStatusDAO {

    public List<String> getAllStatuses() {
        List<String> thesisStatuses = new ArrayList<>();
        String sql = "SELECT Name FROM ThesisStatus ORDER BY Id";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                thesisStatuses.add(rs.getString("Name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Greška pri učitavanju statusa: " + e.getMessage(), e);
        }
        return thesisStatuses;
    }
}