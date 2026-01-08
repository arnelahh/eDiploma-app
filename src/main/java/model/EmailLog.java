package model;

import java.time.LocalDateTime;

public class EmailLog {
    private int id;
    private int sentBy;  // AppUser.Id
    private String sentTo;
    private String subject;
    private String status;  // SUCCESS, FAILED
    private String errorMessage;
    private LocalDateTime sentAt;
    private Integer documentId;  // opciono - link na dokument

    // Konstruktori
    public EmailLog() {
    }

    public EmailLog(int sentBy, String sentTo, String subject, String status, String errorMessage, LocalDateTime sentAt, Integer documentId) {
        this.sentBy = sentBy;
        this.sentTo = sentTo;
        this.subject = subject;
        this.status = status;
        this.errorMessage = errorMessage;
        this.sentAt = sentAt;
        this.documentId = documentId;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSentBy() {
        return sentBy;
    }

    public void setSentBy(int sentBy) {
        this.sentBy = sentBy;
    }

    public String getSentTo() {
        return sentTo;
    }

    public void setSentTo(String sentTo) {
        this.sentTo = sentTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public Integer getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Integer documentId) {
        this.documentId = documentId;
    }

    @Override
    public String toString() {
        return "EmailLog{" +
                "id=" + id +
                ", sentBy=" + sentBy +
                ", sentTo='" + sentTo + '\'' +
                ", subject='" + subject + '\'' +
                ", status='" + status + '\'' +
                ", sentAt=" + sentAt +
                '}';
    }
}
