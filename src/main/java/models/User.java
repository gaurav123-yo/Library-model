package models;

import org.bson.types.ObjectId;

public class User {
    private ObjectId id;
    private String username;
    private String password;
    private String role; // "LIBRARIAN" or "STUDENT"
    private String studentId; // links to Student model if role is "STUDENT"

    public User() {}

    public User(String username, String password, String role, String studentId) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.studentId = studentId;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
}
