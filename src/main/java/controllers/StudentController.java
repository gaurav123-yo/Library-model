package controllers;

import database.*;
import models.*;
import views.StudentDashboardPanel;
import views.MainDashboardView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class StudentController {
    private final StudentDashboardPanel panel;
    private final MainDashboardView parentFrame;
    private final Student student;

    private final BookDAO bookDAO;
    private final IssueRecordDAO issueDAO;
    private final FineDAO fineDAO;
    private final BookLikeDAO bookLikeDAO;

    public StudentController(StudentDashboardPanel panel, Student student, MainDashboardView parentFrame) {
        this.panel = panel;
        this.student = student;
        this.parentFrame = parentFrame;

        this.bookDAO = new BookDAO();
        this.issueDAO = new IssueRecordDAO();
        this.fineDAO = new FineDAO();
        this.bookLikeDAO = new BookLikeDAO();

        // Wire up UI events
        registerCatalogListeners();
        
        // Initial Refresh
        refreshStudentData();
    }

    private void refreshStudentData() {
        String studentId = student.getStudentId();

        // 1. Fetch personal borrows and counts
        List<IssueRecord> allHistory = issueDAO.getIssueRecordsByStudent(studentId);
        List<IssueRecord> activeBorrows = issueDAO.getActiveIssueRecordsByStudent(studentId);
        double pendingFines = fineDAO.getPendingFinesTotal(studentId);
        List<String> likedIsbns = bookLikeDAO.getLikedBookIsbns(studentId);

        // Update Profile headers and dashboard cards
        panel.setProfileDetails(student, pendingFines, likedIsbns.size());
        panel.setProfileActiveBorrows(activeBorrows.size());

        // 2. Populate tables
        List<Book> allBooks = bookDAO.getAllBooks();
        panel.populateCurrentBorrows(activeBorrows, allBooks);
        panel.populateBorrowHistory(allHistory, allBooks, fineDAO.getFinesByStudent(studentId));
        panel.populateCatalogTable(bookDAO.getAllBooks());

        // 3. Generate recommendations
        generateSmartRecommendations(allHistory, likedIsbns);
    }

    private void generateSmartRecommendations(List<IssueRecord> history, List<String> likedIsbns) {
        // Collect preferred categories (explicit student preferences + categories of previously borrowed books)
        List<String> categories = new ArrayList<>(student.getCategoryPreferences());
        
        List<String> excludedIsbns = new ArrayList<>();
        for (IssueRecord rec : history) {
            excludedIsbns.add(rec.getBookIsbn());
            
            // Resolve categories of books in their borrow logs
            Book book = bookDAO.getBookByIsbn(rec.getBookIsbn());
            if (book != null && !categories.contains(book.getCategory())) {
                categories.add(book.getCategory());
            }
        }

        // If categories list is empty, default to standard trending fields
        if (categories.isEmpty()) {
            categories.add("Computer Science");
            categories.add("Fiction");
            categories.add("Fantasy");
        }

        // Retrieve suggestions matching preferences, excluding books they've already borrowed
        List<Book> recommended = bookDAO.getRecommendations(categories, excludedIsbns, 5);
        List<Book> trending = bookDAO.getTrendingBooks(5);

        panel.populateRecommendations(recommended, trending);
    }

    private void registerCatalogListeners() {
        // Selection in Catalog Table updates Like Button state
        panel.getCatalogTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String isbn = panel.getSelectedBookIsbn();
                    if (isbn != null) {
                        boolean hasLiked = bookLikeDAO.hasLiked(student.getStudentId(), isbn);
                        panel.setLikeButtonState(hasLiked);
                    }
                }
            }
        });

        // Search Catalog
        panel.addCatalogSearchListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = panel.getCatalogSearchQuery();
                panel.populateCatalogTable(bookDAO.searchBooks(query));
            }
        });

        // Reset search
        panel.addCatalogResetListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.populateCatalogTable(bookDAO.getAllBooks());
            }
        });

        // Like / Unlike action
        panel.addLikeToggleListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String isbn = panel.getSelectedBookIsbn();
                if (isbn == null) return;

                String studentId = student.getStudentId();
                boolean currentlyLiked = bookLikeDAO.hasLiked(studentId, isbn);

                if (currentlyLiked) {
                    bookLikeDAO.unlikeBook(studentId, isbn);
                    panel.setLikeButtonState(false);
                } else {
                    bookLikeDAO.likeBook(studentId, isbn);
                    panel.setLikeButtonState(true);
                }

                // Refresh details
                refreshStudentData();
            }
        });
    }
}
