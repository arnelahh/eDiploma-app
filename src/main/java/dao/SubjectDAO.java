package dao;

import model.Subject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {
    public List<Subject> getAllSubjects(){
        List<Subject> predmeti = new ArrayList<>();
        String sqlUpit = "SELECT * FROM Subject";
        try(Connection conn = CloudDatabaseConnection.Konekcija();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqlUpit)){
            while(rs.next()){
                Subject s = new Subject();
                s.setId(rs.getInt("Id"));
                s.setName(rs.getString("Name"));
                predmeti.add(s);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return predmeti;
    }

}
