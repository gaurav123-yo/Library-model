package views;

import models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainDashboardView extends JFrame {
    private final JPanel contentPanel;
    private final JLabel userGreetingLabel;
    private final JButton logoutButton;

    public MainDashboardView() {
        setTitle("Smart Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // Layout
        setLayout(new BorderLayout());

        // Header Panel (Dodger Blue background, modern navbar look)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 144, 255));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 65));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Brand Logo / Title
        JLabel logoLabel = new JLabel("SMART LIBRARY SYSTEM");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoLabel.setForeground(Color.WHITE);
        headerPanel.add(logoLabel, BorderLayout.WEST);

        // Right side: Greetings and logout
        JPanel rightHeaderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        rightHeaderPanel.setOpaque(false);

        userGreetingLabel = new JLabel("Welcome, User (Role)");
        userGreetingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userGreetingLabel.setForeground(Color.WHITE);
        rightHeaderPanel.add(userGreetingLabel);

        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setForeground(new Color(30, 144, 255));
        logoutButton.setFocusPainted(false);
        rightHeaderPanel.add(logoutButton);

        headerPanel.add(rightHeaderPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content Area (Cards or specific role panels will swap in here)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(contentPanel, BorderLayout.CENTER);
    }

    public void setSessionUser(User user) {
        if (user != null) {
            String roleName = user.getRole().equalsIgnoreCase("LIBRARIAN") ? "Librarian" : "Student";
            userGreetingLabel.setText("Active Session: " + user.getUsername() + " (" + roleName + ")");
        }
    }

    public void setDashboardPanel(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }
}
