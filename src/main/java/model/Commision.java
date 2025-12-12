package model;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class Commision {
    private int ThesisId;
    private AcademicStaff Member1;
    private CommissionRole Member1Role;
    private AcademicStaff Member2;
    private CommissionRole Member2Role;
    private AcademicStaff Member3;
    private CommissionRole Member3Role;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
}
