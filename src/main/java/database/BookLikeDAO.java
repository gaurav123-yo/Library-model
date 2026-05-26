package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import models.BookLike;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class BookLikeDAO {
    private final MongoCollection<Document> collection;
    private final BookDAO bookDAO;

    public BookLikeDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.collection = db.getCollection("book_likes");
        this.bookDAO = new BookDAO();
    }

    public boolean likeBook(String studentId, String bookIsbn) {
        if (studentId == null || bookIsbn == null) return false;
        if (hasLiked(studentId, bookIsbn)) {
            return false; // Already liked
        }
        try {
            Document doc = new Document("studentId", studentId).append("bookIsbn", bookIsbn);
            collection.insertOne(doc);
            
            // Increment book's aggregate like count
            bookDAO.incrementLikeCount(bookIsbn, 1);
            return true;
        } catch (Exception e) {
            System.err.println("Error liking book: " + e.getMessage());
            return false;
        }
    }

    public boolean unlikeBook(String studentId, String bookIsbn) {
        if (studentId == null || bookIsbn == null) return false;
        if (!hasLiked(studentId, bookIsbn)) {
            return false; // Not liked yet
        }
        try {
            Bson filter = Filters.and(Filters.eq("studentId", studentId), Filters.eq("bookIsbn", bookIsbn));
            collection.deleteOne(filter);
            
            // Decrement book's aggregate like count
            bookDAO.incrementLikeCount(bookIsbn, -1);
            return true;
        } catch (Exception e) {
            System.err.println("Error unliking book: " + e.getMessage());
            return false;
        }
    }

    public boolean hasLiked(String studentId, String bookIsbn) {
        if (studentId == null || bookIsbn == null) return false;
        Bson filter = Filters.and(Filters.eq("studentId", studentId), Filters.eq("bookIsbn", bookIsbn));
        return collection.countDocuments(filter) > 0;
    }

    public List<String> getLikedBookIsbns(String studentId) {
        List<String> isbns = new ArrayList<>();
        if (studentId == null) return isbns;
        for (Document doc : collection.find(Filters.eq("studentId", studentId))) {
            isbns.add(doc.getString("bookIsbn"));
        }
        return isbns;
    }
}
