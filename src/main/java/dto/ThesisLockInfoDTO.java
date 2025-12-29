package dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThesisLockInfoDTO {
    private Integer LockedBy;
    private String lockedByUsername;
    private Timestamp lockedAt;
}
