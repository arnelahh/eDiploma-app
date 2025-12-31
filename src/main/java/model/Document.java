package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {
    private int Id;

    private int ThesisId;
    private int TypeId;

    private String ContentBase64;
    private Integer UploadedByUserId;
    private String DocumentNumber;
    private DocumentStatus Status;

    private DocumentType DocumentType;

    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;

    public boolean IsActive;
}
