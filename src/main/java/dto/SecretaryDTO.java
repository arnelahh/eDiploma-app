package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.AppUser;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecretaryDTO {
    private AppUser secretary;
    private int thesisCount;
}