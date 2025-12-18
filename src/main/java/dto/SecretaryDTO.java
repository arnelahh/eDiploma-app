package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.AppUser;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecretaryDTO {
    private int id;
    private AppUser secretary;
    private int thesisCount;

    public SecretaryDTO(AppUser user) {
    }

    public String getDisplayName() {
        if (secretary == null || secretary.getAcademicStaff() == null) return "";
        String title = secretary.getAcademicStaff().getTitle();
        String name = secretary.getAcademicStaff().getFirstName() + " " + secretary.getAcademicStaff().getLastName();
        return (title != null && !title.isBlank()) ? title + " " + name : name;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }


}