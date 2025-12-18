package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSecretaryDTO {
    private int appUserId;
    private int academicStaffId;

    private String title;
    private String firstName;
    private String lastName;
    private String email;

    private String username;
    private String rawPassword;
}
