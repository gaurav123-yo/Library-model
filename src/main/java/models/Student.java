package models;

import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Student {
    private ObjectId id;
    private String studentId;
    private String name;
    private String email;
    private String contact;
    private Date registrationDate;
    private List<String> categoryPreferences = new ArrayList<>();

    public Student() {}

    public Student(String studentId, String name, String email, String contact, Date registrationDate, List<String> categoryPreferences) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.registrationDate = registrationDate;
        this.categoryPreferences = categoryPreferences != null ? categoryPreferences : new ArrayList<>();
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public Date getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(Date registrationDate) { this.registrationDate = registrationDate; }

    public List<String> getCategoryPreferences() { return categoryPreferences; }
    public void setCategoryPreferences(List<String> categoryPreferences) { this.categoryPreferences = categoryPreferences; }
}
