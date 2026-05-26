package models;

import org.bson.types.ObjectId;

public class BookLike {
    private ObjectId id;
    private String studentId;
    private String bookIsbn;

    public BookLike() {}

    public BookLike(String studentId, String bookIsbn) {
        this.studentId = studentId;
        this.bookIsbn = bookIsbn;
    }

    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getBookIsbn() { return bookIsbn; }
    public void setBookIsbn(String bookIsbn) { this.bookIsbn = bookIsbn; }
}
