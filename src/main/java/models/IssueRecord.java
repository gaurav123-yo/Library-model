package models;

import org.bson.types.ObjectId;
import java.util.Date;

public class IssueRecord {
    private ObjectId id;
    private String issueId;
    private String studentId;
    private String bookIsbn;
    private Date issueDate;
    private Date dueDate;
    private Date returnDate;
    private String status; // "ISSUED", "RETURNED"

    public IssueRecord() {}

    public IssueRecord(String issueId, String studentId, String bookIsbn, Date issueDate, Date dueDate, Date returnDate, String status) {
        this.issueId = issueId;
        this.studentId = studentId;
        this.bookIsbn = bookIsbn;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getIssueId() { return issueId; }
    public void setIssueId(String issueId) { this.issueId = issueId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }

    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
