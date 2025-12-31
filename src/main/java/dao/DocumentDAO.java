package dao;

import model.Document;
import model.DocumentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    public List<Document> getByThesisId(int thesisId) {
        String sql = """
            SELECT Id, ThesisId, TypeId, UploadedByUserId, DocumentNumber, Status, CreatedAt, UpdatedAt, IsActive
            FROM Document
            WHERE ThesisId = ? AND IsActive = 1
        """;

        List<Document> list = new ArrayList<>();

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, thesisId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String statusStr = rs.getString("Status");
                    DocumentStatus status = (statusStr != null) ? DocumentStatus.valueOf(statusStr) : null;

                    list.add(Document.builder()
                            .Id(rs.getInt("Id"))
                            .ThesisId(rs.getInt("ThesisId"))
                            .TypeId(rs.getInt("TypeId"))
                            .UploadedByUserId((Integer) rs.getObject("UploadedByUserId"))
                            .DocumentNumber(rs.getString("DocumentNumber"))
                            .Status(status)
                            .CreatedAt(rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null)
                            .UpdatedAt(rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null)
                            .IsActive(rs.getBoolean("IsActive"))
                            .build());
                }
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri učitavanju dokumenata po tezi.", e);
        }
    }

    public Document getByThesisAndType(int thesisId, int typeId) {
        String sql = """
            SELECT Id, ThesisId, TypeId, ContentBase64, UploadedByUserId, DocumentNumber, Status, CreatedAt, UpdatedAt, IsActive
            FROM Document
            WHERE ThesisId = ? AND TypeId = ? AND IsActive = 1
            LIMIT 1
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, thesisId);
            ps.setInt(2, typeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                String statusStr = rs.getString("Status");
                DocumentStatus status = (statusStr != null) ? DocumentStatus.valueOf(statusStr) : null;

                return Document.builder()
                        .Id(rs.getInt("Id"))
                        .ThesisId(rs.getInt("ThesisId"))
                        .TypeId(rs.getInt("TypeId"))
                        .ContentBase64(rs.getString("ContentBase64"))
                        .UploadedByUserId((Integer) rs.getObject("UploadedByUserId"))
                        .DocumentNumber(rs.getString("DocumentNumber"))
                        .Status(status)
                        .CreatedAt(rs.getTimestamp("CreatedAt") != null ? rs.getTimestamp("CreatedAt").toLocalDateTime() : null)
                        .UpdatedAt(rs.getTimestamp("UpdatedAt") != null ? rs.getTimestamp("UpdatedAt").toLocalDateTime() : null)
                        .IsActive(rs.getBoolean("IsActive"))
                        .build();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatu dokumenta.", e);
        }
    }

    public String getContentBase64(int documentId) {
        String sql = "SELECT ContentBase64 FROM Document WHERE Id = ? AND IsActive = 1";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, documentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return rs.getString("ContentBase64");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri dohvatu ContentBase64.", e);
        }
    }

    /**
     * UPSERT (kreira ako ne postoji, inače update):
     * - snimi ContentBase64
     * - snimi DocumentNumber
     * - snimi Status
     * - snimi UploadedByUserId
     */
    public void upsert(int thesisId, int typeId, String contentBase64, Integer uploadedByUserId,
                       String documentNumber, DocumentStatus status) {

        String checkSql = "SELECT Id FROM Document WHERE ThesisId = ? AND TypeId = ? AND IsActive = 1 LIMIT 1";
        String insertSql = """
            INSERT INTO Document(ThesisId, TypeId, ContentBase64, UploadedByUserId, DocumentNumber, Status, CreatedAt, UpdatedAt, IsActive)
            VALUES(?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1)
        """;
        String updateSql = """
            UPDATE Document
            SET ContentBase64 = ?,
                UploadedByUserId = ?,
                DocumentNumber = ?,
                Status = ?,
                UpdatedAt = CURRENT_TIMESTAMP
            WHERE Id = ?
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija()) {

            Integer existingId = null;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, thesisId);
                ps.setInt(2, typeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) existingId = rs.getInt("Id");
                }
            }

            if (existingId == null) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, thesisId);
                    ps.setInt(2, typeId);
                    ps.setString(3, contentBase64);
                    if (uploadedByUserId != null) ps.setInt(4, uploadedByUserId); else ps.setNull(4, Types.INTEGER);
                    ps.setString(5, documentNumber);
                    ps.setString(6, status != null ? status.name() : null);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, contentBase64);
                    if (uploadedByUserId != null) ps.setInt(2, uploadedByUserId); else ps.setNull(2, Types.INTEGER);
                    ps.setString(3, documentNumber);
                    ps.setString(4, status != null ? status.name() : null);
                    ps.setInt(5, existingId);
                    ps.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Greška pri snimanju dokumenta (upsert).", e);
        }
    }
}