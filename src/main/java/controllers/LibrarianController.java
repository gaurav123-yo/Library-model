package controllers;

import database.*;
import models.*;
import utils.PasswordHasher;
import utils.ValidationUtils;
import views.LibrarianDashboardPanel;
import views.MainDashboardView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class LibrarianController {
    private final LibrarianDashboardPanel panel;
    private final MainDashboardView parentFrame;

    private final BookDAO bookDAO;
    private final StudentDAO studentDAO;
    private final UserDAO userDAO;
    private final IssueRecordDAO issueDAO;
    private final FineDAO fineDAO;
    private final BookLikeDAO bookLikeDAO;

    private IssueRecord currentPendingReturnRecord = null;
    private double calculatedFineAmount = 0.0;

    public LibrarianController(LibrarianDashboardPanel panel, MainDashboardView parentFrame) {
        this.panel = panel;
        this.parentFrame = parentFrame;

        this.bookDAO = new BookDAO();
        this.studentDAO = new StudentDAO();
        this.userDAO = new UserDAO();
        this.issueDAO = new IssueRecordDAO();
        this.fineDAO = new FineDAO();
        this.bookLikeDAO = new BookLikeDAO();

        // Register Action Listeners
        registerBookListeners();
        registerStudentListeners();
        registerIssueReturnListeners();
        registerFineListeners();

        // Initial Data Load
        refreshDashboardData();
    }

    // Refresh KPIs and Tables
    private void refreshDashboardData() {
        // Fetch KPIs
        long totalBooks = bookDAO.getAllBooks().stream().mapToLong(Book::getQuantity).sum();
        long activeIssues = issueDAO.getActiveIssuesCount();
        long returnedIssues = issueDAO.getReturnedIssuesCount();
        long overdueIssues = issueDAO.getOverdueIssuesCount();
        long activeStudents = studentDAO.getAllStudents().size();

        panel.setKPIValues(totalBooks, activeIssues, returnedIssues, overdueIssues, activeStudents);

        // Populate Tables
        panel.populateTrendingBooks(bookDAO.getTrendingBooks(5));
        panel.populateBooksTable(bookDAO.getAllBooks());
        panel.populateStudentsTable(studentDAO.getAllStudents());
        panel.populateFinesTable(fineDAO.getAllFines());
    }

    // ==========================================
    // BOOK CONTROLLER LOGIC
    // ==========================================
    private void registerBookListeners() {
        panel.addBookSearchListener(e -> {
            String query = panel.getBookSearchQuery();
            panel.populateBooksTable(bookDAO.searchBooks(query));
        });

        panel.addBookResetListener(e -> {
            panel.clearBookFields();
            panel.populateBooksTable(bookDAO.getAllBooks());
        });

        panel.addBookActionsListener(
            new AddBookAction(),
            new UpdateBookAction(),
            new DeleteBookAction(),
            e -> panel.clearBookFields()
        );
    }

    private class AddBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String isbn = panel.getBookIsbn();
            String title = panel.getBookTitle();
            String author = panel.getBookAuthor();
            String category = panel.getBookCategory();
            int qty = panel.getBookQuantity();

            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all book fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidISBN(isbn)) {
                JOptionPane.showMessageDialog(panel, "Invalid ISBN format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Book book = new Book(isbn, title, author, category, qty, qty, 0);
            if (bookDAO.addBook(book)) {
                JOptionPane.showMessageDialog(panel, "Book added successfully!");
                panel.clearBookFields();
                refreshDashboardData();
            } else {
                JOptionPane.showMessageDialog(panel, "A book with this ISBN already exists.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class UpdateBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String isbn = panel.getSelectedBookIsbn();
            if (isbn == null) {
                JOptionPane.showMessageDialog(panel, "Please select a book from the table to update.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String title = panel.getBookTitle();
            String author = panel.getBookAuthor();
            String category = panel.getBookCategory();
            int qty = panel.getBookQuantity();

            if (title.isEmpty() || author.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all book fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Book oldBook = bookDAO.getBookByIsbn(isbn);
            if (oldBook == null) return;

            // Calculate new available quantity based on change in total quantity
            int qtyDiff = qty - oldBook.getQuantity();
            int newAvail = oldBook.getAvailableQuantity() + qtyDiff;

            if (newAvail < 0) {
                JOptionPane.showMessageDialog(panel, "Total quantity cannot be lower than currently issued copies.", "Quantity Conflict", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Book book = new Book(isbn, title, author, category, qty, newAvail, oldBook.getLikeCount());
            if (bookDAO.updateBook(book)) {
                JOptionPane.showMessageDialog(panel, "Book updated successfully!");
                panel.clearBookFields();
                refreshDashboardData();
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to update book.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class DeleteBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String isbn = panel.getSelectedBookIsbn();
            if (isbn == null) {
                JOptionPane.showMessageDialog(panel, "Please select a book from the table to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Book book = bookDAO.getBookByIsbn(isbn);
            if (book != null && book.getAvailableQuantity() < book.getQuantity()) {
                JOptionPane.showMessageDialog(panel, "Cannot delete a book while copies are currently issued to students.", "Delete Blocked", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete this book?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (bookDAO.deleteBook(isbn)) {
                    JOptionPane.showMessageDialog(panel, "Book deleted successfully!");
                    panel.clearBookFields();
                    refreshDashboardData();
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed to delete book.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // ==========================================
    // STUDENT CONTROLLER LOGIC
    // ==========================================
    private void registerStudentListeners() {
        panel.addStudentSearchListener(e -> {
            String query = panel.getStudentSearchQuery();
            panel.populateStudentsTable(studentDAO.searchStudents(query));
        });

        panel.addStudentResetListener(e -> {
            panel.clearStudentFields();
            panel.populateStudentsTable(studentDAO.getAllStudents());
        });

        panel.addStudentActionsListener(
            new AddStudentAction(),
            new UpdateStudentAction(),
            new DeleteStudentAction(),
            e -> panel.clearStudentFields(),
            new ViewBorrowHistoryAction()
        );
    }

    private class AddStudentAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = panel.getStudentId();
            String name = panel.getStudentName();
            String email = panel.getStudentEmail();
            String contact = panel.getStudentContact();
            String prefsStr = panel.getStudentPreferences();

            if (id.isEmpty() || name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all student profile fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                JOptionPane.showMessageDialog(panel, "Invalid email format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidPhone(contact)) {
                JOptionPane.showMessageDialog(panel, "Invalid contact number. Must be 10 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Parse preferences
            List<String> prefs = new ArrayList<>();
            if (!prefsStr.isEmpty()) {
                for (String p : prefsStr.split(",")) {
                    prefs.add(p.trim());
                }
            }

            Student student = new Student(id, name, email, contact, new Date(), prefs);
            if (studentDAO.addStudent(student)) {
                // Synchronize by creating user login account automatically
                // Password default: id of student or "student123"
                String defaultPass = "student123";
                User newUser = new User(id, PasswordHasher.hashPassword(defaultPass), "STUDENT", id);
                userDAO.createUser(newUser);

                JOptionPane.showMessageDialog(panel, "Student registered successfully!\nLogin account created automatically:\nUsername: " + id + "\nPassword: " + defaultPass);
                panel.clearStudentFields();
                refreshDashboardData();
            } else {
                JOptionPane.showMessageDialog(panel, "A student with this ID already exists.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class UpdateStudentAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = panel.getSelectedStudentId();
            if (id == null) {
                JOptionPane.showMessageDialog(panel, "Please select a student from the table to update.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String name = panel.getStudentName();
            String email = panel.getStudentEmail();
            String contact = panel.getStudentContact();
            String prefsStr = panel.getStudentPreferences();

            if (name.isEmpty() || email.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all student profile fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidEmail(email)) {
                JOptionPane.showMessageDialog(panel, "Invalid email format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!ValidationUtils.isValidPhone(contact)) {
                JOptionPane.showMessageDialog(panel, "Invalid contact number. Must be 10 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> prefs = new ArrayList<>();
            if (!prefsStr.isEmpty()) {
                for (String p : prefsStr.split(",")) {
                    prefs.add(p.trim());
                }
            }

            Student student = new Student(id, name, email, contact, null, prefs);
            if (studentDAO.updateStudent(student)) {
                JOptionPane.showMessageDialog(panel, "Student profile updated successfully!");
                panel.clearStudentFields();
                refreshDashboardData();
            } else {
                JOptionPane.showMessageDialog(panel, "Failed to update student.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class DeleteStudentAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = panel.getSelectedStudentId();
            if (id == null) {
                JOptionPane.showMessageDialog(panel, "Please select a student from the table to delete.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Verify if student has active borrows
            List<IssueRecord> active = issueDAO.getActiveIssueRecordsByStudent(id);
            if (!active.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Cannot delete student. Student has " + active.size() + " active book borrows.", "Delete Blocked", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to delete this student profile?", "Delete Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (studentDAO.deleteStudent(id)) {
                    // Synchronize and remove user account
                    userDAO.deleteUser(id);
                    JOptionPane.showMessageDialog(panel, "Student profile and user account deleted successfully!");
                    panel.clearStudentFields();
                    refreshDashboardData();
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed to delete student profile.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class ViewBorrowHistoryAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String id = panel.getSelectedStudentId();
            if (id == null) {
                JOptionPane.showMessageDialog(panel, "Please select a student from the table to view history.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Student s = studentDAO.getStudentById(id);
            if (s != null) {
                List<IssueRecord> recs = issueDAO.getIssueRecordsByStudent(id);
                List<Fine> fines = fineDAO.getFinesByStudent(id);
                panel.showBorrowHistoryDialog(s, recs, fines);
            }
        }
    }

    // ==========================================
    // ISSUE & RETURN CONTROLLER LOGIC
    // ==========================================
    private void registerIssueReturnListeners() {
        panel.addIssueReturnActionsListener(
            new IssueBookAction(),
            new RetrieveReturnDetailsAction(),
            new ProcessReturnAction()
        );
    }

    private class IssueBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String studentId = panel.getIssueStudentId();
            String isbn = panel.getIssueBookIsbn();
            int durationDays = panel.getIssueDurationDays();

            if (studentId.isEmpty() || isbn.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter Student ID and Book ISBN.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 1. Verify Student Exists
            Student student = studentDAO.getStudentById(studentId);
            if (student == null) {
                JOptionPane.showMessageDialog(panel, "Student profile does not exist.", "Invalid Student ID", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Verify Book Exists
            Book book = bookDAO.getBookByIsbn(isbn);
            if (book == null) {
                JOptionPane.showMessageDialog(panel, "Book does not exist in registry.", "Invalid ISBN", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 3. Verify Book Availability
            if (book.getAvailableQuantity() <= 0) {
                JOptionPane.showMessageDialog(panel, "No available copies of this book are left.", "Out of Stock", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 4. Verify Student doesn't already have an active loan of this book
            IssueRecord activeRecord = issueDAO.getActiveRecordByStudentAndBook(studentId, isbn);
            if (activeRecord != null) {
                JOptionPane.showMessageDialog(panel, "This book is already issued to this student.", "Double Issue Blocked", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Process Issue
            Date issueDate = new Date();
            Date dueDate = new Date(issueDate.getTime() + (durationDays * 24L * 60 * 60 * 1000));
            String issueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            IssueRecord record = new IssueRecord(issueId, studentId, isbn, issueDate, dueDate, null, "ISSUED");

            if (issueDAO.addIssueRecord(record)) {
                // Decrement book available count
                bookDAO.updateAvailability(isbn, -1);
                
                JOptionPane.showMessageDialog(panel, "Book issued successfully!\nIssue ID: " + issueId + "\nDue Date: " + dueDate);
                panel.clearIssueFields();
                refreshDashboardData();
            } else {
                JOptionPane.showMessageDialog(panel, "Database error issuing book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class RetrieveReturnDetailsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String studentId = panel.getReturnStudentId();
            String isbn = panel.getReturnBookIsbn();

            if (studentId.isEmpty() || isbn.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter Student ID and Book ISBN.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentPendingReturnRecord = issueDAO.getActiveRecordByStudentAndBook(studentId, isbn);
            if (currentPendingReturnRecord == null) {
                panel.setReturnDetailsText("No active issue record found for this Student and Book ISBN.", false);
                calculatedFineAmount = 0.0;
                return;
            }

            // Calculate fine
            Date now = new Date();
            Date due = currentPendingReturnRecord.getDueDate();
            
            calculatedFineAmount = 0.0;
            StringBuilder sb = new StringBuilder();
            sb.append("Loan Record Found!\n");
            sb.append("Issue ID: ").append(currentPendingReturnRecord.getIssueId()).append("\n");
            sb.append("Issue Date: ").append(currentPendingReturnRecord.getIssueDate()).append("\n");
            sb.append("Due Date: ").append(due).append("\n");

            if (now.after(due)) {
                long diffMs = now.getTime() - due.getTime();
                long overdueDays = diffMs / (1000 * 60 * 60 * 24);
                // In case it's a fractional day round up
                if (overdueDays == 0 && diffMs > 0) {
                    overdueDays = 1;
                }
                
                calculatedFineAmount = overdueDays * 10.0; // ₹10 per day overdue
                sb.append("Status: OVERDUE by ").append(overdueDays).append(" day(s)\n");
                sb.append("Overdue Fine: ₹").append(calculatedFineAmount).append(" (₹10/day)");
            } else {
                sb.append("Status: Active (No fines)");
            }

            panel.setReturnDetailsText(sb.toString(), true);
        }
    }

    private class ProcessReturnAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentPendingReturnRecord == null) return;

            String isbn = currentPendingReturnRecord.getBookIsbn();
            String studentId = currentPendingReturnRecord.getStudentId();
            String issueId = currentPendingReturnRecord.getIssueId();

            currentPendingReturnRecord.setReturnDate(new Date());
            currentPendingReturnRecord.setStatus("RETURNED");

            if (issueDAO.updateIssueRecord(currentPendingReturnRecord)) {
                // Restore Book availability
                bookDAO.updateAvailability(isbn, 1);

                // Check and post fine
                if (calculatedFineAmount > 0.0) {
                    String fineId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    Fine fine = new Fine(fineId, issueId, studentId, calculatedFineAmount, false);
                    fineDAO.addFine(fine);
                    JOptionPane.showMessageDialog(panel, 
                            "Book returned successfully!\nOverdue fine posted: ₹" + calculatedFineAmount, 
                            "Return Successful - Fine Incurred", 
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(panel, "Book returned successfully with no fines!");
                }

                // Add category back to student preferences to reinforce recommendation matching
                Book book = bookDAO.getBookByIsbn(isbn);
                Student student = studentDAO.getStudentById(studentId);
                if (book != null && student != null) {
                    String category = book.getCategory();
                    if (!student.getCategoryPreferences().contains(category)) {
                        student.getCategoryPreferences().add(category);
                        studentDAO.updateStudent(student);
                    }
                }

                currentPendingReturnRecord = null;
                calculatedFineAmount = 0.0;
                panel.clearReturnFields();
                refreshDashboardData();
            } else {
                JOptionPane.showMessageDialog(panel, "Database error returning book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // FINE REGISTRY CONTROLLER LOGIC
    // ==========================================
    private void registerFineListeners() {
        panel.addFineActionsListener(
            e -> {
                String query = panel.getFineFilterQuery();
                if (query.isEmpty()) {
                    panel.populateFinesTable(fineDAO.getAllFines());
                } else {
                    panel.populateFinesTable(fineDAO.getUnpaidFinesByStudent(query));
                }
            },
            new MarkPaidAction()
        );
    }

    private class MarkPaidAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String fineId = panel.getSelectedFineId();
            if (fineId == null) {
                JOptionPane.showMessageDialog(panel, "Please select an unpaid fine from the table to process.", "Selection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel, "Are you sure you want to mark this fine as paid?", "Payment Confirmation", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (fineDAO.updateFineStatus(fineId, true)) {
                    JOptionPane.showMessageDialog(panel, "Fine marked as paid successfully.");
                    refreshDashboardData();
                } else {
                    JOptionPane.showMessageDialog(panel, "Failed to update fine record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
