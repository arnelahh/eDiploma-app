package model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ThesisStatusHistory {
    private int Id;
    private Thesis ThesisId;
    private Thesis OldStatusId;
    private Thesis NewStatusId;
    private int ChangedByUsreId;
    private LocalDateTime ChangedAt;
    private String Comment;
}
