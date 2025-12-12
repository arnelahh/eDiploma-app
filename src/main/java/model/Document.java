package model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Document {
    private int Id;
    private Thesis ThesisID;
    private DocumentType TypeId;
    private String ContentBase64;
    private int UploadedByUserId;
    private String DocumentNumber;
    private LocalDateTime CreatedAt;
    private LocalDateTime UpdatedAt;
    private int IsActive;
}
