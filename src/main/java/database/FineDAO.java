package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import models.Fine;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class FineDAO {
    private final MongoCollection<Document> collection;

    public FineDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.collection = db.getCollection("fines");
    }

    public boolean addFine(Fine fine) {
        if (fine == null) return false;
        try {
            collection.insertOne(toDocument(fine));
            return true;
        } catch (Exception e) {
            System.err.println("Error adding fine: " + e.getMessage());
            return false;
        }
    }

    public boolean updateFineStatus(String fineId, boolean paid) {
        if (fineId == null) return false;
        try {
            collection.updateOne(Filters.eq("fineId", fineId), new Document("$set", new Document("paid", paid)));
            return true;
        } catch (Exception e) {
            System.err.println("Error updating fine status: " + e.getMessage());
            return false;
        }
    }

    public Fine getFineByIssueId(String issueId) {
        if (issueId == null) return null;
        Document doc = collection.find(Filters.eq("issueId", issueId)).first();
        return toModel(doc);
    }

    public List<Fine> getFinesByStudent(String studentId) {
        List<Fine> fines = new ArrayList<>();
        if (studentId == null) return fines;
        for (Document doc : collection.find(Filters.eq("studentId", studentId))) {
            fines.add(toModel(doc));
        }
        return fines;
    }

    public List<Fine> getUnpaidFinesByStudent(String studentId) {
        List<Fine> fines = new ArrayList<>();
        if (studentId == null) return fines;
        Bson filter = Filters.and(Filters.eq("studentId", studentId), Filters.eq("paid", false));
        for (Document doc : collection.find(filter)) {
            fines.add(toModel(doc));
        }
        return fines;
    }

    public double getPendingFinesTotal(String studentId) {
        if (studentId == null) return 0.0;
        double total = 0.0;
        Bson filter = Filters.and(Filters.eq("studentId", studentId), Filters.eq("paid", false));
        for (Document doc : collection.find(filter)) {
            total += doc.getDouble("amount");
        }
        return total;
    }

    public List<Fine> getAllFines() {
        List<Fine> fines = new ArrayList<>();
        for (Document doc : collection.find()) {
            fines.add(toModel(doc));
        }
        return fines;
    }

    private Fine toModel(Document doc) {
        if (doc == null) return null;
        Fine fine = new Fine();
        fine.setId(doc.getObjectId("_id"));
        fine.setFineId(doc.getString("fineId"));
        fine.setIssueId(doc.getString("issueId"));
        fine.setStudentId(doc.getString("studentId"));
        fine.setAmount(doc.getDouble("amount"));
        fine.setPaid(doc.getBoolean("paid", false));
        return fine;
    }

    private Document toDocument(Fine fine) {
        if (fine == null) return null;
        Document doc = new Document("fineId", fine.getFineId())
                .append("issueId", fine.getIssueId())
                .append("studentId", fine.getStudentId())
                .append("amount", fine.getAmount())
                .append("paid", fine.isPaid());
        if (fine.getId() != null) {
            doc.append("_id", fine.getId());
        }
        return doc;
    }
}
