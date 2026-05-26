package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import models.User;
import org.bson.Document;
import org.bson.conversions.Bson;

public class UserDAO {
    private final MongoCollection<Document> collection;

    public UserDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.collection = db.getCollection("users");
    }

    public User getUserByUsername(String username) {
        if (username == null) return null;
        Document doc = collection.find(Filters.eq("username", username)).first();
        return toModel(doc);
    }

    public boolean createUser(User user) {
        if (user == null || getUserByUsername(user.getUsername()) != null) {
            return false;
        }
        try {
            collection.insertOne(toDocument(user));
            return true;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(String username) {
        if (username == null) return false;
        try {
            collection.deleteOne(Filters.eq("username", username));
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserPassword(String username, String newHashedPassword) {
        if (username == null || newHashedPassword == null) return false;
        try {
            collection.updateOne(Filters.eq("username", username), 
                    new Document("$set", new Document("password", newHashedPassword)));
            return true;
        } catch (Exception e) {
            System.err.println("Error updating password: " + e.getMessage());
            return false;
        }
    }

    private User toModel(Document doc) {
        if (doc == null) return null;
        User user = new User();
        user.setId(doc.getObjectId("_id"));
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        user.setRole(doc.getString("role"));
        user.setStudentId(doc.getString("studentId"));
        return user;
    }

    private Document toDocument(User user) {
        if (user == null) return null;
        Document doc = new Document("username", user.getUsername())
                .append("password", user.getPassword())
                .append("role", user.getRole())
                .append("studentId", user.getStudentId());
        if (user.getId() != null) {
            doc.append("_id", user.getId());
        }
        return doc;
    }
}
