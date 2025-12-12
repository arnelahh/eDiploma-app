package dao;

import model.Subject;

import java.sql.*;
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

    public void AddSubject(Subject subject){
        if(subject.getName()==null || subject.getName().trim().isEmpty()
        ){
            throw new IllegalArgumentException("Ime predmeta ne smije biti prazno!");
        }

        String sqlCheck = "SELECT COUNT(*) FROM Subject WHERE Name = ?";
        String sqlUpit = "INSERT INTO Subject (Name) VALUES (?)";

        try(Connection conn = CloudDatabaseConnection.Konekcija();){
            try(PreparedStatement psCheck = conn.prepareStatement(sqlCheck)){
                psCheck.setString(1, subject.getName().trim());
                try(ResultSet rs = psCheck.executeQuery()){
                    if(rs.next() && rs.getInt(1)>0){
                        throw new IllegalArgumentException("Predmet s ovim imenom već postoji!");
                    }
                }
            }
            try(PreparedStatement psInsert = conn.prepareStatement(sqlUpit)){
                psInsert.setString(1, subject.getName().trim());
                psInsert.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void DeleteSubject(int id) throws SQLException {
        String sqlUpit = "DELETE FROM Subject WHERE Id = ?";
        try(Connection conn = CloudDatabaseConnection.Konekcija();
            PreparedStatement stmt = conn.prepareStatement(sqlUpit)){
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if(rowsAffected > 0){
                System.out.println("Predmet je uspješno obrisan.");
                
            } else {
                System.out.println("Predmet sa ID "+id+" ne postoji");
            }
        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
