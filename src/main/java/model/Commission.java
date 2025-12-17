package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Commission model - predstavlja komisiju za završni rad
 *
 * Struktura:
 * - Member1 = Predsjednik komisije (Chairman) - Role ID 1
 * - Member2 = Član komisije (Member) - Role ID 3
 * - Member3 = Zamjenski član (Substitute) - Role ID 4 (opciono)
 *
 * NAPOMENA: Mentor (Role ID 2) i Sekretar se NE čuvaju ovdje!
 * Oni se dohvataju iz Thesis tabele preko MentorId i SecretaryId
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Commission {
    private int ThesisId;

    // Member 1 = Predsjednik komisije (Chairman)
    private AcademicStaff Member1;
    private CommissionRole Member1Role;

    // Member 2 = Član komisije (Member)
    private AcademicStaff Member2;
    private CommissionRole Member2Role;

    // Member 3 = Zamjenski član (Substitute)
    private AcademicStaff Member3;
    private CommissionRole Member3Role;

    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
}