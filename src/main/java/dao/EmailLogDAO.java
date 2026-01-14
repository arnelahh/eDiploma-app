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

}
