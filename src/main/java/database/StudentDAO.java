package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import models.Student;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class StudentDAO {
    private final MongoCollection<Document> collection;

    public StudentDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.collection = db.getCollection("students");
    }

    public boolean addStudent(Student student) {
        if (student == null || getStudentById(student.getStudentId()) != null) {
            return false;
        }
        try {
            collection.insertOne(toDocument(student));
            return true;
        } catch (Exception e) {
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStudent(Student student) {
        if (student == null) return false;
        try {
            Bson filter = Filters.eq("studentId", student.getStudentId());
            Document updatedDoc = new Document("name", student.getName())
                    .append("email", student.getEmail())
                    .append("contact", student.getContact())
                    .append("categoryPreferences", student.getCategoryPreferences());
            
            collection.updateOne(filter, new Document("$set", updatedDoc));
            return true;
        } catch (Exception e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStudent(String studentId) {
        if (studentId == null) return false;
        try {
            collection.deleteOne(Filters.eq("studentId", studentId));
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }

    public Student getStudentById(String studentId) {
        if (studentId == null) return null;
        Document doc = collection.find(Filters.eq("studentId", studentId)).first();
        return toModel(doc);
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        for (Document doc : collection.find()) {
            students.add(toModel(doc));
        }
        return students;
    }

    public List<Student> searchStudents(String query) {
        List<Student> students = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllStudents();
        }

        String regexPattern = ".*" + PatternQuote(query.trim()) + ".*";
        Bson filter = Filters.or(
                Filters.regex("studentId", regexPattern, "i"),
                Filters.regex("name", regexPattern, "i"),
                Filters.regex("email", regexPattern, "i"),
                Filters.regex("contact", regexPattern, "i")
        );

        for (Document doc : collection.find(filter)) {
            students.add(toModel(doc));
        }
        return students;
    }

    // Helper to safely escape regex characters
    private String PatternQuote(String s) {
        int slashEIndex = s.indexOf("\\E");
        if (slashEIndex == -1) {
            return "\\Q" + s + "\\E";
        }
        StringBuilder sb = new StringBuilder(s.length() * 2);
        sb.append("\\Q");
        int current = 0;
        while ((slashEIndex = s.indexOf("\\E", current)) != -1) {
            sb.append(s, current, slashEIndex);
            current = slashEIndex + 2;
            sb.append("\\E\\\\E\\Q");
        }
        sb.append(s.substring(current));
        sb.append("\\E");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private Student toModel(Document doc) {
        if (doc == null) return null;
        Student student = new Student();
        student.setId(doc.getObjectId("_id"));
        student.setStudentId(doc.getString("studentId"));
        student.setName(doc.getString("name"));
        student.setEmail(doc.getString("email"));
        student.setContact(doc.getString("contact"));
        student.setRegistrationDate(doc.getDate("registrationDate"));
        
        List<String> prefs = (List<String>) doc.get("categoryPreferences");
        student.setCategoryPreferences(prefs != null ? prefs : new ArrayList<>());
        
        return student;
    }

    private Document toDocument(Student student) {
        if (student == null) return null;
        Document doc = new Document("studentId", student.getStudentId())
                .append("name", student.getName())
                .append("email", student.getEmail())
                .append("contact", student.getContact())
                .append("registrationDate", student.getRegistrationDate() != null ? student.getRegistrationDate() : new Date())
                .append("categoryPreferences", student.getCategoryPreferences());
        if (student.getId() != null) {
            doc.append("_id", student.getId());
        }
        return doc;
    }
}
