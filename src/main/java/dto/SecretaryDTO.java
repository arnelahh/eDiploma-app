package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.AcademicStaff;
import model.AppUser;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecretaryDTO {
    private AcademicStaff secretary;
    private AppUser user;
}
