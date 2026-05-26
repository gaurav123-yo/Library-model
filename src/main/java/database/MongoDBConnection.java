package database;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import utils.PasswordHasher;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

public class MongoDBConnection {
    private static final String CONFIG_FILE_PATH = "config.properties";
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static String connectionUri;
    private static String dbName;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        Properties properties = new Properties();
        File file = new File(CONFIG_FILE_PATH);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                properties.load(input);
                connectionUri = properties.getProperty("mongodb.uri", "mongodb://localhost:27017");
                dbName = properties.getProperty("mongodb.database", "smart_library");
            } catch (IOException ex) {
                System.err.println("Error reading config.properties. Using default settings.");
                connectionUri = "mongodb://localhost:27017";
                dbName = "smart_library";
            }
        } else {
            connectionUri = "mongodb://localhost:27017";
            dbName = "smart_library";
            saveConfig();
        }
    }

    private static void saveConfig() {
        Properties properties = new Properties();
        properties.setProperty("mongodb.uri", connectionUri);
        properties.setProperty("mongodb.database", dbName);
        try (OutputStream output = new FileOutputStream(CONFIG_FILE_PATH)) {
            properties.store(output, "Smart Library System Configuration");
        } catch (IOException io) {
            System.err.println("Error saving config.properties: " + io.getMessage());
        }
    }

    public static synchronized MongoDatabase getDatabase() {
        if (database != null) {
            return database;
        }

        while (true) {
            try {
                System.out.println("Attempting connection to MongoDB at: " + connectionUri);
                mongoClient = MongoClients.create(connectionUri);
                database = mongoClient.getDatabase(dbName);
                
                // Trigger connection test by executing a simple command
                database.runCommand(new Document("ping", 1));
                System.out.println("Connected to MongoDB successfully!");
                
                initializeDatabase();
                return database;
            } catch (Exception e) {
                System.err.println("MongoDB Connection Failed: " + e.getMessage());
                if (mongoClient != null) {
                    mongoClient.close();
                }
                database = null;

                // Connection failed. Prompt the user for a valid URI using a dialog
                String newUri = showConnectionErrorDialog(e.getMessage());
                if (newUri == null) {
                    // User cancelled the dialog, exit the application
                    JOptionPane.showMessageDialog(null, 
                            "Database connection is required to run the application. Exiting.", 
                            "Connection Failed", JOptionPane.ERROR_MESSAGE);
                    System.exit(1);
                } else {
                    connectionUri = newUri.trim();
                    saveConfig();
                }
            }
        }
    }

    private static String showConnectionErrorDialog(String originalError) {
        String msg = "Could not connect to MongoDB on local host (port 27017).\n" +
                "Error: " + originalError + "\n\n" +
                "Please make sure MongoDB is running locally, or enter a custom MongoDB Connection String (e.g. MongoDB Atlas URI):";
        
        return (String) JOptionPane.showInputDialog(
                null,
                msg,
                "MongoDB Connection Setup",
                JOptionPane.WARNING_MESSAGE,
                null,
                null,
                connectionUri
        );
    }

    private static void initializeDatabase() {
        try {
            // Create collections if they don't exist
            boolean userExists = false;
            for (String s : database.listCollectionNames()) {
                if (s.equals("users")) {
                    userExists = true;
                    break;
                }
            }

            // Create Collections explicitly (optional, but ensures they are initialized)
            if (!userExists) {
                database.createCollection("users");
                database.createCollection("books");
                database.createCollection("students");
                database.createCollection("issue_records");
                database.createCollection("fines");
                database.createCollection("book_likes");
                System.out.println("Created MongoDB collections.");
            }

            // Create Indexes for search optimization
            database.getCollection("users").createIndex(Indexes.ascending("username"), new IndexOptions().unique(true));
            database.getCollection("students").createIndex(Indexes.ascending("studentId"), new IndexOptions().unique(true));
            database.getCollection("books").createIndex(Indexes.ascending("isbn"), new IndexOptions().unique(true));
            
            // Search indexes for books (ISBN, Title, Author, Category)
            database.getCollection("books").createIndex(Indexes.ascending("title"));
            database.getCollection("books").createIndex(Indexes.ascending("author"));
            database.getCollection("books").createIndex(Indexes.ascending("category"));
            
            // Compound text index for general search
            database.getCollection("books").createIndex(
                Indexes.compoundIndex(
                    Indexes.text("title"),
                    Indexes.text("author"),
                    Indexes.text("isbn"),
                    Indexes.text("category")
                )
            );

            database.getCollection("issue_records").createIndex(Indexes.ascending("issueId"), new IndexOptions().unique(true));
            database.getCollection("issue_records").createIndex(Indexes.ascending("studentId"));
            database.getCollection("issue_records").createIndex(Indexes.ascending("bookIsbn"));
            database.getCollection("issue_records").createIndex(Indexes.ascending("status"));

            database.getCollection("fines").createIndex(Indexes.ascending("fineId"), new IndexOptions().unique(true));
            database.getCollection("fines").createIndex(Indexes.ascending("studentId"));
            database.getCollection("fines").createIndex(Indexes.ascending("issueId"));

            database.getCollection("book_likes").createIndex(
                Indexes.compoundIndex(Indexes.ascending("studentId"), Indexes.ascending("bookIsbn")),
                new IndexOptions().unique(true)
            );

            System.out.println("Created database indexes.");

            // Seed Sample Data if users collection is empty
            if (database.getCollection("users").countDocuments() == 0) {
                seedSampleData();
            }

        } catch (MongoException e) {
            System.err.println("Error initializing database indexes/collections: " + e.getMessage());
        }
    }

    private static void seedSampleData() {
        System.out.println("Seeding sample data...");

        // 1. Seed Users (1 Librarian/Admin and 2 Students)
        Document librarianUser = new Document("username", "admin")
                .append("password", PasswordHasher.hashPassword("admin123"))
                .append("role", "LIBRARIAN")
                .append("studentId", "");

        Document studentUser1 = new Document("username", "student1")
                .append("password", PasswordHasher.hashPassword("student123"))
                .append("role", "STUDENT")
                .append("studentId", "STU001");

        Document studentUser2 = new Document("username", "student2")
                .append("password", PasswordHasher.hashPassword("student123"))
                .append("role", "STUDENT")
                .append("studentId", "STU002");

        database.getCollection("users").insertMany(Arrays.asList(librarianUser, studentUser1, studentUser2));

        // 2. Seed Students profiles
        Document studentProfile1 = new Document("studentId", "STU001")
                .append("name", "Alice Johnson")
                .append("email", "alice.j@college.edu")
                .append("contact", "9876543210")
                .append("registrationDate", new Date())
                .append("categoryPreferences", Arrays.asList("Computer Science", "Fiction"));

        Document studentProfile2 = new Document("studentId", "STU002")
                .append("name", "Bob Smith")
                .append("email", "bob.s@college.edu")
                .append("contact", "8765432109")
                .append("registrationDate", new Date())
                .append("categoryPreferences", Arrays.asList("Mathematics", "Science Fiction"));

        database.getCollection("students").insertMany(Arrays.asList(studentProfile1, studentProfile2));

        // 3. Seed Books (a collection of interesting academic and fiction books)
        Document book1 = new Document("isbn", "9780132350884")
                .append("title", "Clean Code")
                .append("author", "Robert C. Martin")
                .append("category", "Computer Science")
                .append("quantity", 5)
                .append("availableQuantity", 4)
                .append("likeCount", 15);

        Document book2 = new Document("isbn", "9780134685991")
                .append("title", "Effective Java")
                .append("author", "Joshua Bloch")
                .append("category", "Computer Science")
                .append("quantity", 3)
                .append("availableQuantity", 3)
                .append("likeCount", 20);

        Document book3 = new Document("isbn", "9780061120084")
                .append("title", "To Kill a Mockingbird")
                .append("author", "Harper Lee")
                .append("category", "Fiction")
                .append("quantity", 4)
                .append("availableQuantity", 4)
                .append("likeCount", 35);

        Document book4 = new Document("isbn", "9780451524935")
                .append("title", "1984")
                .append("author", "George Orwell")
                .append("category", "Fiction")
                .append("quantity", 6)
                .append("availableQuantity", 5)
                .append("likeCount", 28);

        Document book5 = new Document("isbn", "9780590353427")
                .append("title", "Harry Potter and the Sorcerer's Stone")
                .append("author", "J.K. Rowling")
                .append("category", "Fantasy")
                .append("quantity", 8)
                .append("availableQuantity", 8)
                .append("likeCount", 50);

        Document book6 = new Document("isbn", "9780201896831")
                .append("title", "The Art of Computer Programming")
                .append("author", "Donald Knuth")
                .append("category", "Computer Science")
                .append("quantity", 2)
                .append("availableQuantity", 2)
                .append("likeCount", 8);

        Document book7 = new Document("isbn", "9780198520115")
                .append("title", "Introduction to Quantum Mechanics")
                .append("author", "David J. Griffiths")
                .append("category", "Physics")
                .append("quantity", 3)
                .append("availableQuantity", 2)
                .append("likeCount", 12);

        database.getCollection("books").insertMany(Arrays.asList(book1, book2, book3, book4, book5, book6, book7));

        // 4. Seed Issue Records
        // Alice has one active borrow (Clean Code)
        Date now = new Date();
        Date fourteenDaysAgo = new Date(now.getTime() - (14L * 24 * 60 * 60 * 1000));
        Date sevenDaysAgo = new Date(now.getTime() - (7L * 24 * 60 * 60 * 1000));
        Date sevenDaysLater = new Date(now.getTime() + (7L * 24 * 60 * 60 * 1000));

        Document activeIssue = new Document("issueId", UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .append("studentId", "STU001")
                .append("bookIsbn", "9780132350884") // Clean Code
                .append("issueDate", sevenDaysAgo)
                .append("dueDate", sevenDaysLater)
                .append("returnDate", null)
                .append("status", "ISSUED");

        // Bob had one returned book, but it was overdue, and has an unpaid fine
        Date twentyDaysAgo = new Date(now.getTime() - (20L * 24 * 60 * 60 * 1000));
        Date sixDaysAgo = new Date(now.getTime() - (6L * 24 * 60 * 60 * 1000)); // Due date
        Date threeDaysAgo = new Date(now.getTime() - (3L * 24 * 60 * 60 * 1000)); // Returned date (3 days late)

        String bobIssueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Document lateReturnedIssue = new Document("issueId", bobIssueId)
                .append("studentId", "STU002")
                .append("bookIsbn", "9780198520115") // Quantum Mechanics
                .append("issueDate", twentyDaysAgo)
                .append("dueDate", sixDaysAgo)
                .append("returnDate", threeDaysAgo)
                .append("status", "RETURNED");

        database.getCollection("issue_records").insertMany(Arrays.asList(activeIssue, lateReturnedIssue));

        // 5. Seed Fines
        // Bob has a fine of 3 days overdue * ₹10 = ₹30 (still unpaid)
        Document bobFine = new Document("fineId", UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .append("issueId", bobIssueId)
                .append("studentId", "STU002")
                .append("amount", 30.0)
                .append("paid", false);

        database.getCollection("fines").insertOne(bobFine);

        // 6. Seed Book Likes
        Document like1 = new Document("studentId", "STU001").append("bookIsbn", "9780132350884");
        Document like2 = new Document("studentId", "STU002").append("bookIsbn", "9780134685991");
        database.getCollection("book_likes").insertMany(Arrays.asList(like1, like2));

        System.out.println("Sample data seeded successfully!");
    }

    public static synchronized void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            System.out.println("MongoDB client closed.");
        }
    }
}
