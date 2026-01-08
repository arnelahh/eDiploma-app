package dao;

import model.EmailLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmailLogDAO {

    /**
     * Loguje poslat email u bazu
     */
    public void logEmail(EmailLog emailLog) throws SQLException {
        String sql = """
            INSERT INTO EmailLog (SentBy, SentTo, Subject, Status, ErrorMessage, SentAt, DocumentId)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, emailLog.getSentBy());
            ps.setString(2, emailLog.getSentTo());
            ps.setString(3, emailLog.getSubject());
            ps.setString(4, emailLog.getStatus());
            ps.setString(5, emailLog.getErrorMessage());
            ps.setTimestamp(6, Timestamp.valueOf(emailLog.getSentAt()));
            
            if (emailLog.getDocumentId() != null) {
                ps.setInt(7, emailLog.getDocumentId());
            } else {
                ps.setNull(7, Types.INTEGER);
            }

            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        emailLog.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    /**
     * Vraća sve email logove za određenog korisnika
     */
    public List<EmailLog> getEmailLogsByUser(int userId) throws SQLException {
        List<EmailLog> logs = new ArrayList<>();
        String sql = """
            SELECT * FROM EmailLog
            WHERE SentBy = ?
            ORDER BY SentAt DESC
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToEmailLog(rs));
                }
            }
        }

        return logs;
    }

    /**
     * Vraća sve email logove za određeni dokument
     */
    public List<EmailLog> getEmailLogsByDocument(int documentId) throws SQLException {
        List<EmailLog> logs = new ArrayList<>();
        String sql = """
            SELECT * FROM EmailLog
            WHERE DocumentId = ?
            ORDER BY SentAt DESC
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, documentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapRowToEmailLog(rs));
                }
            }
        }

        return logs;
    }

    /**
     * Vraća sve failovane emailove
     */
    public List<EmailLog> getFailedEmails() throws SQLException {
        List<EmailLog> logs = new ArrayList<>();
        String sql = """
            SELECT * FROM EmailLog
            WHERE Status = 'FAILED'
            ORDER BY SentAt DESC
            """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapRowToEmailLog(rs));
            }
        }

        return logs;
    }

    private EmailLog mapRowToEmailLog(ResultSet rs) throws SQLException {
        EmailLog log = new EmailLog();
        log.setId(rs.getInt("Id"));
        log.setSentBy(rs.getInt("SentBy"));
        log.setSentTo(rs.getString("SentTo"));
        log.setSubject(rs.getString("Subject"));
        log.setStatus(rs.getString("Status"));
        log.setErrorMessage(rs.getString("ErrorMessage"));
        log.setSentAt(rs.getTimestamp("SentAt").toLocalDateTime());
        
        int docId = rs.getInt("DocumentId");
        if (!rs.wasNull()) {
            log.setDocumentId(docId);
        }
        
        return log;
    }
}
