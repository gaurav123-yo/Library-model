package database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import models.Book;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    private final MongoCollection<Document> collection;

    public BookDAO() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.collection = db.getCollection("books");
    }

    public boolean addBook(Book book) {
        if (book == null || getBookByIsbn(book.getIsbn()) != null) {
            return false;
        }
        try {
            collection.insertOne(toDocument(book));
            return true;
        } catch (Exception e) {
            System.err.println("Error adding book: " + e.getMessage());
            return false;
        }
    }

    public boolean updateBook(Book book) {
        if (book == null) return false;
        try {
            Bson filter = Filters.eq("isbn", book.getIsbn());
            Document updatedDoc = new Document("title", book.getTitle())
                    .append("author", book.getAuthor())
                    .append("category", book.getCategory())
                    .append("quantity", book.getQuantity())
                    .append("availableQuantity", book.getAvailableQuantity())
                    .append("likeCount", book.getLikeCount());
            
            collection.updateOne(filter, new Document("$set", updatedDoc));
            return true;
        } catch (Exception e) {
            System.err.println("Error updating book: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBook(String isbn) {
        if (isbn == null) return false;
        try {
            collection.deleteOne(Filters.eq("isbn", isbn));
            return true;
        } catch (Exception e) {
            System.err.println("Error deleting book: " + e.getMessage());
            return false;
        }
    }

    public Book getBookByIsbn(String isbn) {
        if (isbn == null) return null;
        Document doc = collection.find(Filters.eq("isbn", isbn)).first();
        return toModel(doc);
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find()) {
            books.add(toModel(doc));
        }
        return books;
    }

    // Optimized search by Title, Author, ISBN, or Category using indexes and regex
    public List<Book> searchBooks(String query) {
        List<Book> books = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAllBooks();
        }
        
        String regexPattern = ".*" + PatternQuote(query.trim()) + ".*";
        Bson filter = Filters.or(
                Filters.regex("isbn", regexPattern, "i"),
                Filters.regex("title", regexPattern, "i"),
                Filters.regex("author", regexPattern, "i"),
                Filters.regex("category", regexPattern, "i")
        );

        for (Document doc : collection.find(filter)) {
            books.add(toModel(doc));
        }
        return books;
    }

    // Increments/Decrements book quantity and updates availability
    public boolean changeQuantity(String isbn, int change) {
        try {
            Book book = getBookByIsbn(isbn);
            if (book == null) return false;
            
            int newQty = book.getQuantity() + change;
            int newAvail = book.getAvailableQuantity() + change;
            
            if (newQty < 0 || newAvail < 0) return false; // Prevent negative stock

            collection.updateOne(Filters.eq("isbn", isbn), 
                    new Document("$set", new Document("quantity", newQty)
                            .append("availableQuantity", newAvail)));
            return true;
        } catch (Exception e) {
            System.err.println("Error changing book quantity: " + e.getMessage());
            return false;
        }
    }

    // Adjust availability specifically (on issue/return)
    public boolean updateAvailability(String isbn, int change) {
        try {
            Book book = getBookByIsbn(isbn);
            if (book == null) return false;
            
            int newAvail = book.getAvailableQuantity() + change;
            if (newAvail < 0 || newAvail > book.getQuantity()) {
                return false; // Out of bounds
            }

            collection.updateOne(Filters.eq("isbn", isbn), new Document("$set", new Document("availableQuantity", newAvail)));
            return true;
        } catch (Exception e) {
            System.err.println("Error updating book availability: " + e.getMessage());
            return false;
        }
    }

    // Increments the like count when a book is liked
    public void incrementLikeCount(String isbn, int increment) {
        try {
            collection.updateOne(Filters.eq("isbn", isbn), new Document("$inc", new Document("likeCount", increment)));
        } catch (Exception e) {
            System.err.println("Error updating like count: " + e.getMessage());
        }
    }

    // Get top liked (Trending) books
    public List<Book> getTrendingBooks(int limit) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find().sort(Sorts.descending("likeCount")).limit(limit)) {
            books.add(toModel(doc));
        }
        return books;
    }

    // Get Recommendations based on categories and liked parameters, excluding current borrows
    public List<Book> getRecommendations(List<String> categories, List<String> excludedIsbns, int limit) {
        List<Book> recommendations = new ArrayList<>();
        
        Bson categoryFilter = Filters.in("category", categories);
        Bson excludeFilter = Filters.not(Filters.in("isbn", excludedIsbns));
        Bson combinedFilter = excludedIsbns.isEmpty() ? categoryFilter : Filters.and(categoryFilter, excludeFilter);

        for (Document doc : collection.find(combinedFilter).sort(Sorts.descending("likeCount")).limit(limit)) {
            recommendations.add(toModel(doc));
        }
        
        // Fill up remaining recommendations with trending books if list is too small
        if (recommendations.size() < limit) {
            int needed = limit - recommendations.size();
            List<Book> trending = getTrendingBooks(limit * 2);
            for (Book b : trending) {
                if (recommendations.size() >= limit) break;
                // Exclude if already in recommendations, in excluded list, or category matches (already evaluated)
                boolean alreadyIn = false;
                for (Book r : recommendations) {
                    if (r.getIsbn().equals(b.getIsbn())) {
                        alreadyIn = true;
                        break;
                    }
                }
                if (!alreadyIn && !excludedIsbns.contains(b.getIsbn())) {
                    recommendations.add(b);
                }
            }
        }

        return recommendations;
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

    private Book toModel(Document doc) {
        if (doc == null) return null;
        Book book = new Book();
        book.setId(doc.getObjectId("_id"));
        book.setIsbn(doc.getString("isbn"));
        book.setTitle(doc.getString("title"));
        book.setAuthor(doc.getString("author"));
        book.setCategory(doc.getString("category"));
        book.setQuantity(doc.getInteger("quantity", 0));
        book.setAvailableQuantity(doc.getInteger("availableQuantity", 0));
        book.setLikeCount(doc.getInteger("likeCount", 0));
        return book;
    }

    private Document toDocument(Book book) {
        if (book == null) return null;
        Document doc = new Document("isbn", book.getIsbn())
                .append("title", book.getTitle())
                .append("author", book.getAuthor())
                .append("category", book.getCategory())
                .append("quantity", book.getQuantity())
                .append("availableQuantity", book.getAvailableQuantity())
                .append("likeCount", book.getLikeCount());
        if (book.getId() != null) {
            doc.append("_id", book.getId());
        }
        return doc;
    }
}
