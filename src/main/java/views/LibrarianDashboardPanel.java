package views;

import com.formdev.flatlaf.FlatClientProperties;
import models.Book;
import models.Student;
import models.IssueRecord;
import models.Fine;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class LibrarianDashboardPanel extends JPanel {
    private JTabbedPane tabbedPane;

    // --- Overview Tab ---
    private JLabel totalBooksVal;
    private JLabel activeIssuesVal;
    private JLabel returnedBooksVal;
    private JLabel overdueBooksVal;
    private JLabel activeUsersVal;
    private JTable trendingBooksTable;
    private DefaultTableModel trendingBooksModel;

    // --- Book Management Tab ---
    private JTextField bookSearchField;
    private JButton bookSearchBtn;
    private JButton bookResetBtn;
    private JTable bookTable;
    private DefaultTableModel bookTableModel;
    private JTextField isbnField, titleField, authorField;
    private JComboBox<String> categoryCombo;
    private JSpinner quantitySpinner;
    private JButton addBookBtn, updateBookBtn, deleteBookBtn, clearBookFieldsBtn;

    // --- Student Management Tab ---
    private JTextField studentSearchField;
    private JButton studentSearchBtn;
    private JButton studentResetBtn;
    private JTable studentTable;
    private DefaultTableModel studentTableModel;
    private JTextField studentIdField, nameField, emailField, contactField, preferencesField;
    private JButton addStudentBtn, updateStudentBtn, deleteStudentBtn, viewBorrowHistoryBtn, clearStudentFieldsBtn;

    // --- Issue & Return Tab ---
    // Issue Box
    private JTextField issueStudentIdField, issueBookIsbnField;
    private JSpinner issueDaysSpinner;
    private JButton issueBookBtn;
    // Return Box
    private JTextField returnStudentIdField, returnBookIsbnField;
    private JButton checkReturnDetailsBtn;
    private JLabel returnStatusDetailsLabel;
    private JButton returnBookBtn;

    // --- Fine Management Tab ---
    private JTable fineTable;
    private DefaultTableModel fineTableModel;
    private JTextField fineSearchField;
    private JButton fineSearchBtn;
    private JButton markPaidBtn;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public LibrarianDashboardPanel() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Create Tabs
        createOverviewTab();
        createBookTab();
        createStudentTab();
        createIssueReturnTab();
        createFineTab();

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // OVERVIEW TAB
    // ==========================================
    private void createOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // KPI Cards Grid (5 columns)
        JPanel kpiPanel = new JPanel(new GridLayout(1, 5, 15, 15));

        JPanel card1 = createKPICard("TOTAL BOOKS", "0", new Color(240, 248, 255), new Color(30, 144, 255)); // Alice Blue, Dodger Blue
        totalBooksVal = (JLabel) card1.getClientProperty("valLbl");
        kpiPanel.add(card1);

        JPanel card2 = createKPICard("BOOKS ISSUED", "0", new Color(255, 240, 245), new Color(219, 112, 147)); // Lavender Blush, Pale Violet Red
        activeIssuesVal = (JLabel) card2.getClientProperty("valLbl");
        kpiPanel.add(card2);

        JPanel card3 = createKPICard("BOOKS RETURNED", "0", new Color(240, 255, 240), new Color(46, 139, 87)); // Honeydew, Sea Green
        returnedBooksVal = (JLabel) card3.getClientProperty("valLbl");
        kpiPanel.add(card3);

        JPanel card4 = createKPICard("OVERDUE ISSUES", "0", new Color(255, 250, 240), new Color(205, 92, 92)); // Floral White, Indian Red
        overdueBooksVal = (JLabel) card4.getClientProperty("valLbl");
        kpiPanel.add(card4);

        JPanel card5 = createKPICard("ACTIVE STUDENTS", "0", new Color(245, 245, 245), new Color(112, 128, 144)); // White Smoke, Slate Gray
        activeUsersVal = (JLabel) card5.getClientProperty("valLbl");
        kpiPanel.add(card5);

        panel.add(kpiPanel, BorderLayout.NORTH);

        // Center Panel: Trending Section
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Trending Books (Popular by Likes)"));
        
        trendingBooksModel = new DefaultTableModel(new String[]{"ISBN", "Title", "Author", "Category", "Likes", "Availability"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        trendingBooksTable = new JTable(trendingBooksModel);
        trendingBooksTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        trendingBooksTable.setRowHeight(24);
        
        JScrollPane scrollPane = new JScrollPane(trendingBooksTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        panel.add(centerPanel, BorderLayout.CENTER);
        tabbedPane.addTab("Dashboard Overview", panel);
    }

    private JPanel createKPICard(String title, String value, Color bgColor, Color textColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 12");

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(textColor.darker());

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLbl.setForeground(textColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.putClientProperty("valLbl", valLbl); // save reference
        return card;
    }

    // ==========================================
    // BOOK MANAGEMENT TAB
    // ==========================================
    private void createBookTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        bookSearchField = new JTextField(25);
        bookSearchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search by title, author, ISBN, category...");
        bookSearchField.setPreferredSize(new Dimension(300, 32));
        
        bookSearchBtn = new JButton("Search");
        bookSearchBtn.setPreferredSize(new Dimension(80, 32));
        
        bookResetBtn = new JButton("Reset");
        bookResetBtn.setPreferredSize(new Dimension(80, 32));
        
        searchPanel.add(bookSearchField);
        searchPanel.add(bookSearchBtn);
        searchPanel.add(bookResetBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Center Table
        bookTableModel = new DefaultTableModel(new String[]{"ISBN", "Title", "Author", "Category", "Quantity", "Available", "Likes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        bookTable = new JTable(bookTableModel);
        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookTable.setRowHeight(25);
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow != -1) {
                isbnField.setText(bookTableModel.getValueAt(selectedRow, 0).toString());
                titleField.setText(bookTableModel.getValueAt(selectedRow, 1).toString());
                authorField.setText(bookTableModel.getValueAt(selectedRow, 2).toString());
                categoryCombo.setSelectedItem(bookTableModel.getValueAt(selectedRow, 3).toString());
                quantitySpinner.setValue(Integer.parseInt(bookTableModel.getValueAt(selectedRow, 4).toString()));
                isbnField.setEditable(false); // Do not change ISBN on update
            }
        });
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        // East Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Manage Book Details"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        formPanel.setPreferredSize(new Dimension(350, getTemplateHeight()));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;

        // Form Fields
        gbc.gridy = 0;
        formPanel.add(new JLabel("ISBN:"), gbc);
        isbnField = new JTextField();
        isbnField.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 1;
        formPanel.add(isbnField, gbc);

        gbc.gridy = 2;
        formPanel.add(new JLabel("Title:"), gbc);
        titleField = new JTextField();
        titleField.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 3;
        formPanel.add(titleField, gbc);

        gbc.gridy = 4;
        formPanel.add(new JLabel("Author:"), gbc);
        authorField = new JTextField();
        authorField.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 5;
        formPanel.add(authorField, gbc);

        gbc.gridy = 6;
        formPanel.add(new JLabel("Category:"), gbc);
        categoryCombo = new JComboBox<>(new String[]{"Computer Science", "Fiction", "Mathematics", "Science Fiction", "Physics", "Chemistry", "Fantasy", "Biography", "Other"});
        categoryCombo.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 7;
        formPanel.add(categoryCombo, gbc);

        gbc.gridy = 8;
        formPanel.add(new JLabel("Quantity:"), gbc);
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100, 1));
        quantitySpinner.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 9;
        formPanel.add(quantitySpinner, gbc);

        // Buttons Panel inside Form
        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        addBookBtn = new JButton("Add Book");
        updateBookBtn = new JButton("Update");
        deleteBookBtn = new JButton("Delete");
        clearBookFieldsBtn = new JButton("Clear");
        btnPanel.add(addBookBtn);
        btnPanel.add(updateBookBtn);
        btnPanel.add(deleteBookBtn);
        btnPanel.add(clearBookFieldsBtn);

        gbc.gridy = 10;
        gbc.insets = new Insets(20, 8, 8, 8);
        formPanel.add(btnPanel, gbc);

        panel.add(formPanel, BorderLayout.EAST);
        tabbedPane.addTab("Book Management", panel);
    }

    // ==========================================
    // STUDENT MANAGEMENT TAB
    // ==========================================
    private void createStudentTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        studentSearchField = new JTextField(25);
        studentSearchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search by Student ID, name, email...");
        studentSearchField.setPreferredSize(new Dimension(300, 32));

        studentSearchBtn = new JButton("Search");
        studentSearchBtn.setPreferredSize(new Dimension(80, 32));
        
        studentResetBtn = new JButton("Reset");
        studentResetBtn.setPreferredSize(new Dimension(80, 32));
        
        searchPanel.add(studentSearchField);
        searchPanel.add(studentSearchBtn);
        searchPanel.add(studentResetBtn);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Center Table
        studentTableModel = new DefaultTableModel(new String[]{"Student ID", "Name", "Email", "Contact", "Preferences"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        studentTable = new JTable(studentTableModel);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        studentTable.setRowHeight(25);
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow != -1) {
                studentIdField.setText(studentTableModel.getValueAt(selectedRow, 0).toString());
                nameField.setText(studentTableModel.getValueAt(selectedRow, 1).toString());
                emailField.setText(studentTableModel.getValueAt(selectedRow, 2).toString());
                contactField.setText(studentTableModel.getValueAt(selectedRow, 3).toString());
                preferencesField.setText(studentTableModel.getValueAt(selectedRow, 4).toString());
                studentIdField.setEditable(false); // Do not change ID on update
            }
        });
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        // East Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Manage Student Profiles"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        formPanel.setPreferredSize(new Dimension(350, getTemplateHeight()));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridx = 0;

        // Form Fields
        gbc.gridy = 0;
        formPanel.add(new JLabel("Student ID:"), gbc);
        studentIdField = new JTextField();
        studentIdField.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 1;
        formPanel.add(studentIdField, gbc);

        gbc.gridy = 2;
        formPanel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField();
        nameField.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 3;
        formPanel.add(nameField, gbc);

        gbc.gridy = 4;
        formPanel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField();
        emailField.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 5;
        formPanel.add(emailField, gbc);

        gbc.gridy = 6;
        formPanel.add(new JLabel("Contact:"), gbc);
        contactField = new JTextField();
        contactField.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 7;
        formPanel.add(contactField, gbc);

        gbc.gridy = 8;
        formPanel.add(new JLabel("Category Preferences (comma separated):"), gbc);
        preferencesField = new JTextField();
        preferencesField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "e.g. Fiction, Computer Science");
        preferencesField.setPreferredSize(new Dimension(200, 32));
        gbc.gridy = 9;
        formPanel.add(preferencesField, gbc);

        // Buttons Panel inside Form
        JPanel btnPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        addStudentBtn = new JButton("Add Student");
        updateStudentBtn = new JButton("Update");
        deleteStudentBtn = new JButton("Delete");
        clearStudentFieldsBtn = new JButton("Clear");
        viewBorrowHistoryBtn = new JButton("View History");
        viewBorrowHistoryBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewBorrowHistoryBtn.setBackground(new Color(255, 240, 245));
        
        btnPanel.add(addStudentBtn);
        btnPanel.add(updateStudentBtn);
        btnPanel.add(deleteStudentBtn);
        btnPanel.add(clearStudentFieldsBtn);
        btnPanel.add(viewBorrowHistoryBtn);
        
        gbc.gridy = 10;
        gbc.insets = new Insets(15, 8, 8, 8);
        formPanel.add(btnPanel, gbc);

        panel.add(formPanel, BorderLayout.EAST);
        tabbedPane.addTab("Student Management", panel);
    }

    // ==========================================
    // ISSUE & RETURN TAB
    // ==========================================
    private void createIssueReturnTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // LEFT: Issue Book Panel
        JPanel issuePanel = new JPanel(new GridBagLayout());
        issuePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Issue Book Section"),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        gbc.gridy = 0;
        issuePanel.add(new JLabel("Student ID:"), gbc);
        issueStudentIdField = new JTextField();
        issueStudentIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        issueStudentIdField.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 1;
        issuePanel.add(issueStudentIdField, gbc);

        gbc.gridy = 2;
        issuePanel.add(new JLabel("Book ISBN:"), gbc);
        issueBookIsbnField = new JTextField();
        issueBookIsbnField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        issueBookIsbnField.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 3;
        issuePanel.add(issueBookIsbnField, gbc);

        gbc.gridy = 4;
        issuePanel.add(new JLabel("Borrow Duration (Days):"), gbc);
        issueDaysSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 60, 1));
        issueDaysSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        issueDaysSpinner.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 5;
        issuePanel.add(issueDaysSpinner, gbc);

        issueBookBtn = new JButton("Issue Book");
        issueBookBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        issueBookBtn.setBackground(new Color(30, 144, 255));
        issueBookBtn.setForeground(Color.WHITE);
        issueBookBtn.setPreferredSize(new Dimension(250, 42));
        gbc.gridy = 6;
        gbc.insets = new Insets(25, 10, 10, 10);
        issuePanel.add(issueBookBtn, gbc);

        panel.add(issuePanel);

        // RIGHT: Return Book Panel
        JPanel returnPanel = new JPanel(new GridBagLayout());
        returnPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Return Book Section"),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;

        gbc.gridy = 0;
        returnPanel.add(new JLabel("Student ID:"), gbc);
        returnStudentIdField = new JTextField();
        returnStudentIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        returnStudentIdField.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 1;
        returnPanel.add(returnStudentIdField, gbc);

        gbc.gridy = 2;
        returnPanel.add(new JLabel("Book ISBN:"), gbc);
        returnBookIsbnField = new JTextField();
        returnBookIsbnField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        returnBookIsbnField.setPreferredSize(new Dimension(250, 36));
        gbc.gridy = 3;
        returnPanel.add(returnBookIsbnField, gbc);

        // Check Return Details Button
        checkReturnDetailsBtn = new JButton("Retrieve Return Details");
        checkReturnDetailsBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 4;
        gbc.insets = new Insets(10, 10, 15, 10);
        returnPanel.add(checkReturnDetailsBtn, gbc);

        // Details display label (shows issue status, due date, fine preview)
        returnStatusDetailsLabel = new JLabel("<html><body style='text-align: center; color: gray;'>Enter details and click retrieve above to calculate overdue fines.</body></html>", SwingConstants.CENTER);
        returnStatusDetailsLabel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1));
        returnStatusDetailsLabel.setPreferredSize(new Dimension(250, 80));
        gbc.gridy = 5;
        gbc.insets = new Insets(5, 10, 20, 10);
        returnPanel.add(returnStatusDetailsLabel, gbc);

        returnBookBtn = new JButton("Process Return");
        returnBookBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        returnBookBtn.setBackground(new Color(46, 139, 87));
        returnBookBtn.setForeground(Color.WHITE);
        returnBookBtn.setPreferredSize(new Dimension(250, 42));
        returnBookBtn.setEnabled(false); // Enable only after details retrieved
        gbc.gridy = 6;
        gbc.insets = new Insets(5, 10, 10, 10);
        returnPanel.add(returnBookBtn, gbc);

        panel.add(returnPanel);

        tabbedPane.addTab("Issue & Return System", panel);
    }

    // ==========================================
    // FINE MANAGEMENT TAB
    // ==========================================
    private void createFineTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top panel with search and action
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fineSearchField = new JTextField(20);
        fineSearchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Filter by Student ID...");
        fineSearchField.setPreferredSize(new Dimension(250, 32));
        
        fineSearchBtn = new JButton("Filter");
        fineSearchBtn.setPreferredSize(new Dimension(80, 32));
        
        searchPanel.add(fineSearchField);
        searchPanel.add(fineSearchBtn);
        topPanel.add(searchPanel, BorderLayout.WEST);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        markPaidBtn = new JButton("Mark selected fine as PAID");
        markPaidBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        markPaidBtn.setBackground(new Color(46, 139, 87));
        markPaidBtn.setForeground(Color.WHITE);
        markPaidBtn.setPreferredSize(new Dimension(220, 32));
        actionPanel.add(markPaidBtn);
        topPanel.add(actionPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        // Table
        fineTableModel = new DefaultTableModel(new String[]{"Fine ID", "Student ID", "Issue ID", "Amount (₹)", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        fineTable = new JTable(fineTableModel);
        fineTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        fineTable.setRowHeight(25);
        panel.add(new JScrollPane(fineTable), BorderLayout.CENTER);

        tabbedPane.addTab("Fine Registry", panel);
    }

    // Utility getters for Heights
    private int getTemplateHeight() {
        return 400;
    }

    // ==========================================
    // DATA SETTERS / POPULATION
    // ==========================================
    public void setKPIValues(long totalBooks, long activeIssues, long returnedBooks, long overdueBooks, long activeStudents) {
        totalBooksVal.setText(String.valueOf(totalBooks));
        activeIssuesVal.setText(String.valueOf(activeIssues));
        returnedBooksVal.setText(String.valueOf(returnedBooks));
        overdueBooksVal.setText(String.valueOf(overdueBooks));
        activeUsersVal.setText(String.valueOf(activeStudents));
    }

    public void populateTrendingBooks(List<Book> books) {
        trendingBooksModel.setRowCount(0);
        for (Book b : books) {
            trendingBooksModel.addRow(new Object[]{
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategory(),
                    b.getLikeCount(),
                    b.getAvailableQuantity() + " / " + b.getQuantity()
            });
        }
    }

    public void populateBooksTable(List<Book> books) {
        bookTableModel.setRowCount(0);
        for (Book b : books) {
            bookTableModel.addRow(new Object[]{
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategory(),
                    b.getQuantity(),
                    b.getAvailableQuantity(),
                    b.getLikeCount()
            });
        }
    }

    public void populateStudentsTable(List<Student> students) {
        studentTableModel.setRowCount(0);
        for (Student s : students) {
            String prefs = String.join(", ", s.getCategoryPreferences());
            studentTableModel.addRow(new Object[]{
                    s.getStudentId(),
                    s.getName(),
                    s.getEmail(),
                    s.getContact(),
                    prefs
            });
        }
    }

    public void populateFinesTable(List<Fine> fines) {
        fineTableModel.setRowCount(0);
        for (Fine f : fines) {
            fineTableModel.addRow(new Object[]{
                    f.getFineId(),
                    f.getStudentId(),
                    f.getIssueId(),
                    f.getAmount(),
                    f.isPaid() ? "PAID" : "UNPAID"
            });
        }
    }

    // ==========================================
    // GETTERS & ACTION LISTENERS
    // ==========================================

    // Book fields getters
    public String getBookIsbn() { return isbnField.getText().trim(); }
    public String getBookTitle() { return titleField.getText().trim(); }
    public String getBookAuthor() { return authorField.getText().trim(); }
    public String getBookCategory() { return categoryCombo.getSelectedItem().toString(); }
    public int getBookQuantity() { return (Integer) quantitySpinner.getValue(); }
    public String getBookSearchQuery() { return bookSearchField.getText().trim(); }

    public void setBookFields(String isbn, String title, String author, String category, int qty) {
        isbnField.setText(isbn);
        titleField.setText(title);
        authorField.setText(author);
        categoryCombo.setSelectedItem(category);
        quantitySpinner.setValue(qty);
    }

    public void clearBookFields() {
        isbnField.setText("");
        titleField.setText("");
        authorField.setText("");
        categoryCombo.setSelectedIndex(0);
        quantitySpinner.setValue(1);
        isbnField.setEditable(true);
        bookTable.clearSelection();
    }

    // Student fields getters
    public String getStudentId() { return studentIdField.getText().trim(); }
    public String getStudentName() { return nameField.getText().trim(); }
    public String getStudentEmail() { return emailField.getText().trim(); }
    public String getStudentContact() { return contactField.getText().trim(); }
    public String getStudentPreferences() { return preferencesField.getText().trim(); }
    public String getStudentSearchQuery() { return studentSearchField.getText().trim(); }

    public void setStudentFields(String id, String name, String email, String contact, String prefs) {
        studentIdField.setText(id);
        nameField.setText(name);
        emailField.setText(email);
        contactField.setText(contact);
        preferencesField.setText(prefs);
    }

    public void clearStudentFields() {
        studentIdField.setText("");
        nameField.setText("");
        emailField.setText("");
        contactField.setText("");
        preferencesField.setText("");
        studentIdField.setEditable(true);
        studentTable.clearSelection();
    }

    // Selection helper getters
    public String getSelectedBookIsbn() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) return null;
        return bookTableModel.getValueAt(selectedRow, 0).toString();
    }

    public String getSelectedStudentId() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) return null;
        return studentTableModel.getValueAt(selectedRow, 0).toString();
    }

    public String getSelectedFineId() {
        int selectedRow = fineTable.getSelectedRow();
        if (selectedRow == -1) return null;
        return fineTableModel.getValueAt(selectedRow, 0).toString();
    }

    public String getFineFilterQuery() {
        return fineSearchField.getText().trim();
    }

    // Issue/Return fields getters
    public String getIssueStudentId() { return issueStudentIdField.getText().trim(); }
    public String getIssueBookIsbn() { return issueBookIsbnField.getText().trim(); }
    public int getIssueDurationDays() { return (Integer) issueDaysSpinner.getValue(); }

    public String getReturnStudentId() { return returnStudentIdField.getText().trim(); }
    public String getReturnBookIsbn() { return returnBookIsbnField.getText().trim(); }

    public void clearIssueFields() {
        issueStudentIdField.setText("");
        issueBookIsbnField.setText("");
        issueDaysSpinner.setValue(14);
    }

    public void clearReturnFields() {
        returnStudentIdField.setText("");
        returnBookIsbnField.setText("");
        setReturnDetailsText("Enter details and click retrieve above to calculate overdue fines.", false);
        returnBookBtn.setEnabled(false);
    }

    public void setReturnDetailsText(String text, boolean enableReturnBtn) {
        returnStatusDetailsLabel.setText("<html><body style='text-align: center; color: black; font-size:12px;'>" + text.replace("\n", "<br>") + "</body></html>");
        returnBookBtn.setEnabled(enableReturnBtn);
    }

    // Button action hooks
    public void addBookSearchListener(ActionListener l) { bookSearchBtn.addActionListener(l); }
    public void addBookResetListener(ActionListener l) { bookResetBtn.addActionListener(l); }
    public void addBookActionsListener(ActionListener add, ActionListener update, ActionListener delete, ActionListener clear) {
        addBookBtn.addActionListener(add);
        updateBookBtn.addActionListener(update);
        deleteBookBtn.addActionListener(delete);
        clearBookFieldsBtn.addActionListener(clear);
    }

    public void addStudentSearchListener(ActionListener l) { studentSearchBtn.addActionListener(l); }
    public void addStudentResetListener(ActionListener l) { studentResetBtn.addActionListener(l); }
    public void addStudentActionsListener(ActionListener add, ActionListener update, ActionListener delete, ActionListener clear, ActionListener history) {
        addStudentBtn.addActionListener(add);
        updateStudentBtn.addActionListener(update);
        deleteStudentBtn.addActionListener(delete);
        clearStudentFieldsBtn.addActionListener(clear);
        viewBorrowHistoryBtn.addActionListener(history);
    }

    public void addIssueReturnActionsListener(ActionListener issue, ActionListener checkReturn, ActionListener processReturn) {
        issueBookBtn.addActionListener(issue);
        checkReturnDetailsBtn.addActionListener(checkReturn);
        returnBookBtn.addActionListener(processReturn);
    }

    public void addFineActionsListener(ActionListener filter, ActionListener markPaid) {
        fineSearchBtn.addActionListener(filter);
        markPaidBtn.addActionListener(markPaid);
    }

    // Dialog helper to display student borrow history details
    public void showBorrowHistoryDialog(Student student, List<IssueRecord> historyList, List<Fine> studentFines) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Borrowing History - " + student.getName(), true);
        dialog.setSize(750, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 15));
        JLabel nameLbl = new JLabel("Student: " + student.getName() + " (" + student.getStudentId() + ")");
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        JLabel summaryLbl = new JLabel("Total Borrows: " + historyList.size() + " | Contact: " + student.getContact());
        summaryLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summaryLbl.setForeground(Color.GRAY);
        header.add(nameLbl);
        header.add(summaryLbl);
        dialog.add(header, BorderLayout.NORTH);

        // History Table
        DefaultTableModel model = new DefaultTableModel(new String[]{"Issue ID", "Book ISBN", "Issue Date", "Due Date", "Returned Date", "Status", "Fine"}, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(22);
        
        for (IssueRecord rec : historyList) {
            String retDate = rec.getReturnDate() != null ? dateFormat.format(rec.getReturnDate()) : "N/A";
            double fineAmt = 0.0;
            for (Fine f : studentFines) {
                if (f.getIssueId().equals(rec.getIssueId())) {
                    fineAmt = f.getAmount();
                    break;
                }
            }
            String fineStr = fineAmt > 0 ? "₹" + fineAmt : "None";
            model.addRow(new Object[]{
                    rec.getIssueId(),
                    rec.getBookIsbn(),
                    dateFormat.format(rec.getIssueDate()),
                    dateFormat.format(rec.getDueDate()),
                    retDate,
                    rec.getStatus(),
                    fineStr
            });
        }
        
        dialog.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 10));
        JButton close = new JButton("Close");
        close.addActionListener(e -> dialog.dispose());
        footer.add(close);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
