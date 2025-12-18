package dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSecretaryDTO {
    private String title;
    private String firstName;
    private String lastName;
    private String email;

    private String username;
    private String rawPassword;
}
