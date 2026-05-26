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

public class StudentDashboardPanel extends JPanel {
    private JTabbedPane tabbedPane;

    // --- My Dashboard Tab ---
    private JLabel profileNameVal;
    private JLabel profileIdVal;
    private JLabel profileEmailVal;
    private JLabel profilePrefsVal;
    
    private JLabel activeBorrowsCardVal;
    private JLabel unpaidFinesCardVal;
    private JLabel likedBooksCardVal;
    
    private JTable currentBorrowsTable;
    private DefaultTableModel currentBorrowsModel;

    // --- Catalog Tab ---
    private JTextField catalogSearchField;
    private JButton catalogSearchBtn;
    private JButton catalogResetBtn;
    private JTable catalogTable;
    private DefaultTableModel catalogTableModel;
    private JButton likeToggleBtn;

    // --- Recommendations Tab ---
    private JTable recsTable;
    private DefaultTableModel recsTableModel;
    private JTable trendingTable;
    private DefaultTableModel trendingTableModel;

    // --- Borrowing History Tab ---
    private JTable historyTable;
    private DefaultTableModel historyTableModel;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public StudentDashboardPanel() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        createDashboardOverviewTab();
        createCatalogTab();
        createRecommendationsTab();
        createHistoryTab();

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==========================================
    // MY DASHBOARD TAB
    // ==========================================
    private void createDashboardOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Panel: Left Profile Info, Right Stats Cards
        JPanel topSplitPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 15);

        // Profile Panel (Dodger Blue Outline Card)
        JPanel profilePanel = new JPanel(new GridLayout(4, 1, 5, 5));
        profilePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(30, 144, 255), 1, true), "My Profile Details"),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        profilePanel.setPreferredSize(new Dimension(380, 130));

        profileNameVal = new JLabel("Name: Loading...");
        profileNameVal.setFont(new Font("Segoe UI", Font.BOLD, 14));
        profileIdVal = new JLabel("Student ID: Loading...");
        profileEmailVal = new JLabel("Email: Loading...");
        profilePrefsVal = new JLabel("Category Preferences: Loading...");
        profilePrefsVal.setForeground(Color.GRAY);

        profilePanel.add(profileNameVal);
        profilePanel.add(profileIdVal);
        profilePanel.add(profileEmailVal);
        profilePanel.add(profilePrefsVal);

        gbc.gridx = 0;
        gbc.weightx = 0.4;
        topSplitPanel.add(profilePanel, gbc);

        // Stats Panel (3 KPI Cards)
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        
        JPanel activeBorrowsCard = createKPICard("CURRENT ISSUES", "0", new Color(240, 248, 255), new Color(30, 144, 255));
        activeBorrowsCardVal = (JLabel) activeBorrowsCard.getClientProperty("valLbl");
        statsPanel.add(activeBorrowsCard);

        JPanel unpaidFinesCard = createKPICard("UNPAID FINES", "₹0.0", new Color(255, 250, 240), new Color(220, 20, 60)); // Crimson
        unpaidFinesCardVal = (JLabel) unpaidFinesCard.getClientProperty("valLbl");
        statsPanel.add(unpaidFinesCard);

        JPanel likedBooksCard = createKPICard("MY FAVORITES", "0", new Color(240, 255, 240), new Color(46, 139, 87));
        likedBooksCardVal = (JLabel) likedBooksCard.getClientProperty("valLbl");
        statsPanel.add(likedBooksCard);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        gbc.insets = new Insets(0, 0, 0, 0);
        topSplitPanel.add(statsPanel, gbc);

        panel.add(topSplitPanel, BorderLayout.NORTH);

        // Center Panel: Current Borrowed Books Table
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Currently Issued Books"));

        currentBorrowsModel = new DefaultTableModel(new String[]{"ISBN", "Title", "Author", "Category", "Issue Date", "Due Date", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        currentBorrowsTable = new JTable(currentBorrowsModel);
        currentBorrowsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        currentBorrowsTable.setRowHeight(24);
        
        centerPanel.add(new JScrollPane(currentBorrowsTable), BorderLayout.CENTER);
        panel.add(centerPanel, BorderLayout.CENTER);

        tabbedPane.addTab("My Dashboard", panel);
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
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(textColor);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        card.putClientProperty("valLbl", valLbl); // save reference
        return card;
    }

    // ==========================================
    // CATALOG TAB
    // ==========================================
    private void createCatalogTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Search Panel + Like Toggle Button
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        catalogSearchField = new JTextField(25);
        catalogSearchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search catalog by title, author, ISBN, category...");
        catalogSearchField.setPreferredSize(new Dimension(300, 32));

        catalogSearchBtn = new JButton("Search Catalog");
        catalogSearchBtn.setPreferredSize(new Dimension(130, 32));
        
        catalogResetBtn = new JButton("Reset");
        catalogResetBtn.setPreferredSize(new Dimension(80, 32));

        searchPanel.add(catalogSearchField);
        searchPanel.add(catalogSearchBtn);
        searchPanel.add(catalogResetBtn);
        topPanel.add(searchPanel, BorderLayout.WEST);

        // Like Button
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        likeToggleBtn = new JButton("Like/Favorite Book");
        likeToggleBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        likeToggleBtn.setEnabled(false); // Enable on selection
        likeToggleBtn.setPreferredSize(new Dimension(180, 32));
        actionPanel.add(likeToggleBtn);
        topPanel.add(actionPanel, BorderLayout.EAST);

        panel.add(topPanel, BorderLayout.NORTH);

        // Center Table
        catalogTableModel = new DefaultTableModel(new String[]{"ISBN", "Title", "Author", "Category", "Likes", "Availability"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        catalogTable = new JTable(catalogTableModel);
        catalogTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        catalogTable.setRowHeight(25);
        
        panel.add(new JScrollPane(catalogTable), BorderLayout.CENTER);
        tabbedPane.addTab("Search Library Catalog", panel);
    }

    // ==========================================
    // RECOMMENDATIONS TAB
    // ==========================================
    private void createRecommendationsTab() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // LEFT: Recommended Panel
        JPanel recsPanel = new JPanel(new BorderLayout(10, 10));
        recsPanel.setBorder(BorderFactory.createTitledBorder("Recommended Books For You"));
        
        recsTableModel = new DefaultTableModel(new String[]{"Title", "Author", "Category", "Likes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        recsTable = new JTable(recsTableModel);
        recsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recsTable.setRowHeight(22);
        recsPanel.add(new JScrollPane(recsTable), BorderLayout.CENTER);
        
        panel.add(recsPanel);

        // RIGHT: Trending Panel
        JPanel trendingPanel = new JPanel(new BorderLayout(10, 10));
        trendingPanel.setBorder(BorderFactory.createTitledBorder("Trending Books in Library"));
        
        trendingTableModel = new DefaultTableModel(new String[]{"Title", "Author", "Category", "Likes"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        trendingTable = new JTable(trendingTableModel);
        trendingTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        trendingTable.setRowHeight(22);
        trendingPanel.add(new JScrollPane(trendingTable), BorderLayout.CENTER);
        
        panel.add(trendingPanel);

        tabbedPane.addTab("Smart Recommendations", panel);
    }

    // ==========================================
    // HISTORY TAB
    // ==========================================
    private void createHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        historyTableModel = new DefaultTableModel(new String[]{"Issue ID", "ISBN", "Book Title", "Issue Date", "Due Date", "Return Date", "Status", "Fine Incurred"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        historyTable.setRowHeight(24);

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        tabbedPane.addTab("My Borrow History", panel);
    }

    // ==========================================
    // PROFILE SETTER
    // ==========================================
    public void setProfileDetails(Student student, double pendingFines, int likesCount) {
        if (student != null) {
            profileNameVal.setText("Name: " + student.getName());
            profileIdVal.setText("Student ID: " + student.getStudentId());
            profileEmailVal.setText("Email: " + student.getEmail());
            profilePrefsVal.setText("Category Preferences: " + String.join(", ", student.getCategoryPreferences()));
            
            unpaidFinesCardVal.setText("₹" + String.format("%.2f", pendingFines));
            likedBooksCardVal.setText(String.valueOf(likesCount));
        }
    }

    public void setProfileActiveBorrows(int activeBorrows) {
        activeBorrowsCardVal.setText(String.valueOf(activeBorrows));
    }

    // ==========================================
    // TABLE POPULATORS
    // ==========================================
    public void populateCurrentBorrows(List<IssueRecord> records, List<Book> books) {
        currentBorrowsModel.setRowCount(0);
        for (IssueRecord rec : records) {
            String title = "Unknown";
            String author = "Unknown";
            String cat = "Unknown";
            for (Book b : books) {
                if (b.getIsbn().equals(rec.getBookIsbn())) {
                    title = b.getTitle();
                    author = b.getAuthor();
                    cat = b.getCategory();
                    break;
                }
            }
            currentBorrowsModel.addRow(new Object[]{
                    rec.getBookIsbn(),
                    title,
                    author,
                    cat,
                    dateFormat.format(rec.getIssueDate()),
                    dateFormat.format(rec.getDueDate()),
                    rec.getStatus()
            });
        }
    }

    public void populateCatalogTable(List<Book> books) {
        catalogTableModel.setRowCount(0);
        for (Book b : books) {
            catalogTableModel.addRow(new Object[]{
                    b.getIsbn(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getCategory(),
                    b.getLikeCount(),
                    b.getAvailableQuantity() + " / " + b.getQuantity()
            });
        }
    }

    public void populateRecommendations(List<Book> recommended, List<Book> trending) {
        recsTableModel.setRowCount(0);
        for (Book b : recommended) {
            recsTableModel.addRow(new Object[]{b.getTitle(), b.getAuthor(), b.getCategory(), b.getLikeCount()});
        }

        trendingTableModel.setRowCount(0);
        for (Book b : trending) {
            trendingTableModel.addRow(new Object[]{b.getTitle(), b.getAuthor(), b.getCategory(), b.getLikeCount()});
        }
    }

    public void populateBorrowHistory(List<IssueRecord> records, List<Book> books, List<Fine> fines) {
        historyTableModel.setRowCount(0);
        for (IssueRecord rec : records) {
            String title = "Unknown";
            for (Book b : books) {
                if (b.getIsbn().equals(rec.getBookIsbn())) {
                    title = b.getTitle();
                    break;
                }
            }
            double fineAmt = 0.0;
            boolean isPaid = false;
            for (Fine f : fines) {
                if (f.getIssueId().equals(rec.getIssueId())) {
                    fineAmt = f.getAmount();
                    isPaid = f.isPaid();
                    break;
                }
            }
            String fineStr = "None";
            if (fineAmt > 0) {
                fineStr = "₹" + fineAmt + " (" + (isPaid ? "PAID" : "UNPAID") + ")";
            }

            String retDate = rec.getReturnDate() != null ? dateFormat.format(rec.getReturnDate()) : "N/A";
            historyTableModel.addRow(new Object[]{
                    rec.getIssueId(),
                    rec.getBookIsbn(),
                    title,
                    dateFormat.format(rec.getIssueDate()),
                    dateFormat.format(rec.getDueDate()),
                    retDate,
                    rec.getStatus(),
                    fineStr
            });
        }
    }

    // ==========================================
    // GETTERS & SELECTION LISTENERS
    // ==========================================
    public String getCatalogSearchQuery() { return catalogSearchField.getText().trim(); }

    public String getSelectedBookIsbn() {
        int selectedRow = catalogTable.getSelectedRow();
        if (selectedRow == -1) return null;
        return catalogTableModel.getValueAt(selectedRow, 0).toString();
    }

    public void setLikeButtonState(boolean hasLiked) {
        likeToggleBtn.setEnabled(true);
        if (hasLiked) {
            likeToggleBtn.setText("♥ Remove from Likes");
            likeToggleBtn.setBackground(new Color(255, 192, 203)); // Pinkish
            likeToggleBtn.setForeground(Color.BLACK);
        } else {
            likeToggleBtn.setText("♡ Add to Likes");
            likeToggleBtn.setBackground(new Color(30, 144, 255)); // Dodger Blue
            likeToggleBtn.setForeground(Color.WHITE);
        }
    }

    public JTable getCatalogTable() {
        return catalogTable;
    }

    public void addCatalogSearchListener(ActionListener l) { catalogSearchBtn.addActionListener(l); }
    public void addCatalogResetListener(ActionListener l) { catalogResetBtn.addActionListener(l); }
    public void addLikeToggleListener(ActionListener l) { likeToggleBtn.addActionListener(l); }
}
