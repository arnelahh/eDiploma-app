package dao;

import model.AcademicStaff;
import model.Commission;
import model.CommissionRole;

import java.sql.*;
import java.time.LocalDateTime;

public class CommissionDAO {

    public void insertCommission(Commission commission) {
        // Prvo provjeri da li već postoji komisija za ovaj thesis
        String checkSql = "SELECT COUNT(*) FROM Commission WHERE ThesisId = ?";

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setInt(1, commission.getThesisId());
            ResultSet rs = checkPs.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Commission already exists for this thesis. Updating instead...");
                updateCommission(commission);
                return;
            }

        } catch (SQLException e) {
            System.err.println("Error checking existing commission: " + e.getMessage());
        }

        String sql = """
            INSERT INTO Commission 
            (ThesisId, Member1Id, Member1RoleId, Member2Id, Member2RoleId, 
             Member3Id, Member3RoleId, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            System.out.println("=== INSERTING COMMISSION ===");
            System.out.println("ThesisId: " + commission.getThesisId());

            ps.setInt(1, commission.getThesisId());

            // Member 1 - Chairman
            if (commission.getMember1() != null) {
                ps.setInt(2, commission.getMember1().getId());
                ps.setInt(3, commission.getMember1Role() != null ? commission.getMember1Role().getId() : 1);
                System.out.println("Member1 (Chairman) ID: " + commission.getMember1().getId() + ", RoleId: " +
                        (commission.getMember1Role() != null ? commission.getMember1Role().getId() : 1));
            } else {
                ps.setNull(2, Types.INTEGER);
                ps.setNull(3, Types.INTEGER);
                System.out.println("Member1 is NULL");
            }

            // Member 2 - Member
            if (commission.getMember2() != null) {
                ps.setInt(4, commission.getMember2().getId());
                ps.setInt(5, commission.getMember2Role() != null ? commission.getMember2Role().getId() : 3);
                System.out.println("Member2 (Member) ID: " + commission.getMember2().getId() + ", RoleId: " +
                        (commission.getMember2Role() != null ? commission.getMember2Role().getId() : 3));
            } else {
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.INTEGER);
                System.out.println("Member2 is NULL");
            }

            // Member 3 - Substitute (opciono)
            if (commission.getMember3() != null) {
                ps.setInt(6, commission.getMember3().getId());
                ps.setInt(7, commission.getMember3Role() != null ? commission.getMember3Role().getId() : 4);
                System.out.println("Member3 (Substitute) ID: " + commission.getMember3().getId() + ", RoleId: " +
                        (commission.getMember3Role() != null ? commission.getMember3Role().getId() : 4));
            } else {
                ps.setNull(6, Types.INTEGER);
                ps.setNull(7, Types.INTEGER);
                System.out.println("Member3 is NULL (optional)");
            }

            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

            int rows = ps.executeUpdate();
            System.out.println("=== COMMISSION INSERTED SUCCESSFULLY! Rows: " + rows + " ===");

        } catch (SQLException e) {
            System.err.println("=== SQL ERROR IN INSERT ===");
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Greška pri dodavanju komisije: " + e.getMessage(), e);
        }
    }

    public void updateCommission(Commission commission) {
        String sql = """
            UPDATE Commission SET
                Member1Id = ?, Member1RoleId = ?,
                Member2Id = ?, Member2RoleId = ?,
                Member3Id = ?, Member3RoleId = ?,
                UpdatedAt = ?
            WHERE ThesisId = ?
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            System.out.println("=== UPDATING COMMISSION ===");

            if (commission.getMember1() != null) {
                ps.setInt(1, commission.getMember1().getId());
                ps.setInt(2, commission.getMember1Role() != null ? commission.getMember1Role().getId() : 1);
            } else {
                ps.setNull(1, Types.INTEGER);
                ps.setNull(2, Types.INTEGER);
            }

            if (commission.getMember2() != null) {
                ps.setInt(3, commission.getMember2().getId());
                ps.setInt(4, commission.getMember2Role() != null ? commission.getMember2Role().getId() : 3);
            } else {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            }

            if (commission.getMember3() != null) {
                ps.setInt(5, commission.getMember3().getId());
                ps.setInt(6, commission.getMember3Role() != null ? commission.getMember3Role().getId() : 4);
            } else {
                ps.setNull(5, Types.INTEGER);
                ps.setNull(6, Types.INTEGER);
            }

            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(8, commission.getThesisId());

            int rows = ps.executeUpdate();
            System.out.println("=== COMMISSION UPDATED SUCCESSFULLY! Rows: " + rows + " ===");

        } catch (SQLException e) {
            System.err.println("=== SQL ERROR IN UPDATE ===");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Greška pri ažuriranju komisije: " + e.getMessage(), e);
        }
    }

    public Commission getCommissionByThesisId(int thesisId) {
        String sql = """
            SELECT c.*,
                   a1.Id AS m1_id, a1.Title AS m1_title, a1.FirstName AS m1_fname, 
                   a1.LastName AS m1_lname, a1.Email AS m1_email,
                   a2.Id AS m2_id, a2.Title AS m2_title, a2.FirstName AS m2_fname, 
                   a2.LastName AS m2_lname, a2.Email AS m2_email,
                   a3.Id AS m3_id, a3.Title AS m3_title, a3.FirstName AS m3_fname, 
                   a3.LastName AS m3_lname, a3.Email AS m3_email,
                   r1.Id AS r1_id, r1.Name AS r1_name,
                   r2.Id AS r2_id, r2.Name AS r2_name,
                   r3.Id AS r3_id, r3.Name AS r3_name
            FROM Commission c
            LEFT JOIN AcademicStaff a1 ON c.Member1Id = a1.Id
            LEFT JOIN AcademicStaff a2 ON c.Member2Id = a2.Id
            LEFT JOIN AcademicStaff a3 ON c.Member3Id = a3.Id
            LEFT JOIN CommissionRole r1 ON c.Member1RoleId = r1.Id
            LEFT JOIN CommissionRole r2 ON c.Member2RoleId = r2.Id
            LEFT JOIN CommissionRole r3 ON c.Member3RoleId = r3.Id
            WHERE c.ThesisId = ?
        """;

        try (Connection conn = CloudDatabaseConnection.Konekcija();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, thesisId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Commission comm = new Commission();
                comm.setThesisId(rs.getInt("ThesisId"));

                // Member 1
                if (rs.getObject("m1_id") != null) {
                    AcademicStaff m1 = new AcademicStaff();
                    m1.setId(rs.getInt("m1_id"));
                    m1.setTitle(rs.getString("m1_title"));
                    m1.setFirstName(rs.getString("m1_fname"));
                    m1.setLastName(rs.getString("m1_lname"));
                    m1.setEmail(rs.getString("m1_email"));
                    comm.setMember1(m1);

                    if (rs.getObject("r1_id") != null) {
                        CommissionRole r1 = new CommissionRole();
                        r1.setId(rs.getInt("r1_id"));
                        r1.setName(rs.getString("r1_name"));
                        comm.setMember1Role(r1);
                    }
                }

                // Member 2
                if (rs.getObject("m2_id") != null) {
                    AcademicStaff m2 = new AcademicStaff();
                    m2.setId(rs.getInt("m2_id"));
                    m2.setTitle(rs.getString("m2_title"));
                    m2.setFirstName(rs.getString("m2_fname"));
                    m2.setLastName(rs.getString("m2_lname"));
                    m2.setEmail(rs.getString("m2_email"));
                    comm.setMember2(m2);

                    if (rs.getObject("r2_id") != null) {
                        CommissionRole r2 = new CommissionRole();
                        r2.setId(rs.getInt("r2_id"));
                        r2.setName(rs.getString("r2_name"));
                        comm.setMember2Role(r2);
                    }
                }

                // Member 3
                if (rs.getObject("m3_id") != null) {
                    AcademicStaff m3 = new AcademicStaff();
                    m3.setId(rs.getInt("m3_id"));
                    m3.setTitle(rs.getString("m3_title"));
                    m3.setFirstName(rs.getString("m3_fname"));
                    m3.setLastName(rs.getString("m3_lname"));
                    m3.setEmail(rs.getString("m3_email"));
                    comm.setMember3(m3);

                    if (rs.getObject("r3_id") != null) {
                        CommissionRole r3 = new CommissionRole();
                        r3.setId(rs.getInt("r3_id"));
                        r3.setName(rs.getString("r3_name"));
                        comm.setMember3Role(r3);
                    }
                }

                return comm;
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Error loading commission: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Greška pri dohvatanju komisije: " + e.getMessage(), e);
        }
    }
}