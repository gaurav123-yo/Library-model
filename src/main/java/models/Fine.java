package models;

import org.bson.types.ObjectId;

public class Fine {
    private ObjectId id;
    private String fineId;
    private String issueId;
    private String studentId;
    private double amount;
    private boolean paid;

    public Fine() {}

    public Fine(String fineId, String issueId, String studentId, double amount, boolean paid) {
        this.fineId = fineId;
        this.issueId = issueId;
        this.studentId = studentId;
        this.amount = amount;
        this.paid = paid;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getFineId() { return fineId; }
    public void setFineId(String fineId) { this.fineId = fineId; }

    public String getIssueId() { return issueId; }
    public void setIssueId(String issueId) { this.issueId = issueId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}
