package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class AcademicStaff {
    private int Id;
    private String Title;
    private String FirstName;
    private String LastName;
    private String Email;
    private boolean IsDean;
    private boolean IsActive;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
    private boolean IsSecretary;

    public boolean isDean() {
        return false;
    }
}
