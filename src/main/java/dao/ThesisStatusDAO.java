package dao;

import model.ThesisStatus;
import model.UserRole;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;;

public class ThesisStatusDAO {
    public List<String> getAllStatuses(){
        List<String> thesisStatuses = new ArrayList<>();
        String sql = "select Name from ThesisStatus";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String thesisStatus = new String();
                thesisStatus = rs.getString("Name");
                thesisStatuses.add(thesisStatus);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return  thesisStatuses;
    }




}


