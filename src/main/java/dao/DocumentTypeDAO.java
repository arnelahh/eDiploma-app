package dao;

import model.DocumentType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentTypeDAO {

    public List<DocumentType> getAllOrdered() {
        String sql = """
            SELECT Id, Name, RequiresNumber, NumberPrefix, SortOrder
            FROM DocumentType
            ORDER BY SortOrder
        """;

        List<DocumentType> list = new ArrayList<>();

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(DocumentType.builder()
                        .Id(rs.getInt("Id"))
                        .Name(rs.getString("Name"))
                        .RequiresNumber(rs.getBoolean("RequiresNumber"))
                        .NumberPrefix(rs.getString("NumberPrefix"))
                        .SortOrder((Integer) rs.getObject("SortOrder"))
                        .build());
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri učitavanju DocumentType.", e);
        }
    }

    public DocumentType getByName(String name) {
        String sql = """
            SELECT Id, Name, RequiresNumber, NumberPrefix, SortOrder
            FROM DocumentType
            WHERE Name = ?
            LIMIT 1
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return DocumentType.builder()
                        .Id(rs.getInt("Id"))
                        .Name(rs.getString("Name"))
                        .RequiresNumber(rs.getBoolean("RequiresNumber"))
                        .NumberPrefix(rs.getString("NumberPrefix"))
                        .SortOrder((Integer) rs.getObject("SortOrder"))
                        .build();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatu DocumentType po nazivu.", e);
        }
    }
}
