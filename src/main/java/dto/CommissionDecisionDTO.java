package dto;

public class CommissionDecisionDTO {
    private String decisionDate;
    private String studentFullName;
    private String thesisTitle;
    private String chairmanFullName;
    private String member1FullName;
    private String member2FullName; // Mentor
    private String secretaryFullName;

    // Constructors
    public CommissionDecisionDTO() {
    }

    // Getters and Setters
    public String getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(String decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getStudentFullName() {
        return studentFullName;
    }

    public void setStudentFullName(String studentFullName) {
        this.studentFullName = studentFullName;
    }

    public String getThesisTitle() {
        return thesisTitle;
    }

    public void setThesisTitle(String thesisTitle) {
        this.thesisTitle = thesisTitle;
    }

    public String getChairmanFullName() {
        return chairmanFullName;
    }

    public void setChairmanFullName(String chairmanFullName) {
        this.chairmanFullName = chairmanFullName;
    }

    public String getMember1FullName() {
        return member1FullName;
    }

    public void setMember1FullName(String member1FullName) {
        this.member1FullName = member1FullName;
    }

    public String getMember2FullName() {
        return member2FullName;
    }

    public void setMember2FullName(String member2FullName) {
        this.member2FullName = member2FullName;
    }

    public String getSecretaryFullName() {
        return secretaryFullName;
    }

    public void setSecretaryFullName(String secretaryFullName) {
        this.secretaryFullName = secretaryFullName;
    }
}