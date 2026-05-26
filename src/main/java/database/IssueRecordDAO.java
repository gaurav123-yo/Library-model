package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import models.IssueRecord;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IssueRecordDAO {
    private final MongoCollection<Document> collection;

    public IssueRecordDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.collection = db.getCollection("issue_records");
    }

    public boolean addIssueRecord(IssueRecord record) {
        if (record == null) return false;
        try {
            collection.insertOne(toDocument(record));
            return true;
        } catch (Exception e) {
            System.err.println("Error adding issue record: " + e.getMessage());
            return false;
        }
    }

    public boolean updateIssueRecord(IssueRecord record) {
        if (record == null) return false;
        try {
            Bson filter = Filters.eq("issueId", record.getIssueId());
            Document updatedDoc = new Document("returnDate", record.getReturnDate())
                    .append("status", record.getStatus());
            collection.updateOne(filter, new Document("$set", updatedDoc));
            return true;
        } catch (Exception e) {
            System.err.println("Error updating issue record: " + e.getMessage());
            return false;
        }
    }

    public IssueRecord getIssueRecordById(String issueId) {
        if (issueId == null) return null;
        Document doc = collection.find(Filters.eq("issueId", issueId)).first();
        return toModel(doc);
    }

    public List<IssueRecord> getIssueRecordsByStudent(String studentId) {
        List<IssueRecord> records = new ArrayList<>();
        if (studentId == null) return records;
        for (Document doc : collection.find(Filters.eq("studentId", studentId))) {
            records.add(toModel(doc));
        }
        return records;
    }

    public List<IssueRecord> getActiveIssueRecordsByStudent(String studentId) {
        List<IssueRecord> records = new ArrayList<>();
        if (studentId == null) return records;
        Bson filter = Filters.and(Filters.eq("studentId", studentId), Filters.eq("status", "ISSUED"));
        for (Document doc : collection.find(filter)) {
            records.add(toModel(doc));
        }
        return records;
    }

    public IssueRecord getActiveRecordByStudentAndBook(String studentId, String bookIsbn) {
        if (studentId == null || bookIsbn == null) return null;
        Bson filter = Filters.and(
                Filters.eq("studentId", studentId),
                Filters.eq("bookIsbn", bookIsbn),
                Filters.eq("status", "ISSUED")
        );
        Document doc = collection.find(filter).first();
        return toModel(doc);
    }

    public List<IssueRecord> getAllIssueRecords() {
        List<IssueRecord> records = new ArrayList<>();
        for (Document doc : collection.find()) {
            records.add(toModel(doc));
        }
        return records;
    }

    // Dashboard Statistics Metrics
    public long getActiveIssuesCount() {
        return collection.countDocuments(Filters.eq("status", "ISSUED"));
    }

    public long getReturnedIssuesCount() {
        return collection.countDocuments(Filters.eq("status", "RETURNED"));
    }

    public long getOverdueIssuesCount() {
        Bson filter = Filters.and(
                Filters.eq("status", "ISSUED"),
                Filters.lt("dueDate", new Date())
        );
        return collection.countDocuments(filter);
    }

    public List<IssueRecord> getOverdueIssues() {
        List<IssueRecord> records = new ArrayList<>();
        Bson filter = Filters.and(
                Filters.eq("status", "ISSUED"),
                Filters.lt("dueDate", new Date())
        );
        for (Document doc : collection.find(filter)) {
            records.add(toModel(doc));
        }
        return records;
    }

    private IssueRecord toModel(Document doc) {
        if (doc == null) return null;
        IssueRecord record = new IssueRecord();
        record.setId(doc.getObjectId("_id"));
        record.setIssueId(doc.getString("issueId"));
        record.setStudentId(doc.getString("studentId"));
        record.setBookIsbn(doc.getString("bookIsbn"));
        record.setIssueDate(doc.getDate("issueDate"));
        record.setDueDate(doc.getDate("dueDate"));
        record.setReturnDate(doc.getDate("returnDate"));
        record.setStatus(doc.getString("status"));
        return record;
    }

    private Document toDocument(IssueRecord record) {
        if (record == null) return null;
        Document doc = new Document("issueId", record.getIssueId())
                .append("studentId", record.getStudentId())
                .append("bookIsbn", record.getBookIsbn())
                .append("issueDate", record.getIssueDate())
                .append("dueDate", record.getDueDate())
                .append("returnDate", record.getReturnDate())
                .append("status", record.getStatus());
        if (record.getId() != null) {
            doc.append("_id", record.getId());
        }
        return doc;
    }
}
