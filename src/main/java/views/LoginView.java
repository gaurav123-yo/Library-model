package views;

import com.formdev.flatlaf.FlatClientProperties;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel errorLabel;

    public LoginView() {
        setTitle("Smart Library Management System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main Layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new java.awt.Insets(10, 0, 10, 0);

        // Header Title
        JLabel titleLabel = new JLabel("SMART LIBRARY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(30, 144, 255)); // Dodger Blue
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        // Subtitle
        JLabel subtitleLabel = new JLabel("College Library Portal Management", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(0, 0, 30, 0);
        mainPanel.add(subtitleLabel, gbc);

        // Reset Insets for fields
        gbc.insets = new java.awt.Insets(8, 0, 8, 0);

        // Username Field
        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameField.setPreferredSize(new Dimension(300, 42));
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Username / Student ID");
        usernameField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
        gbc.gridy = 2;
        mainPanel.add(usernameField, gbc);

        // Password Field
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        passwordField.setPreferredSize(new Dimension(300, 42));
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Password");
        passwordField.putClientProperty(FlatClientProperties.STYLE, "showRevealButton: true");
        gbc.gridy = 3;
        mainPanel.add(passwordField, gbc);

        // Error Label
        errorLabel = new JLabel(" ", SwingConstants.CENTER);
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        errorLabel.setForeground(new Color(220, 20, 60)); // Crimson
        gbc.gridy = 4;
        mainPanel.add(errorLabel, gbc);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(300, 45));
        loginButton.setBackground(new Color(30, 144, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        gbc.gridy = 5;
        gbc.insets = new java.awt.Insets(20, 0, 10, 0);
        mainPanel.add(loginButton, gbc);

        // Footer copyright
        JLabel footerLabel = new JLabel("Powered by MongoDB & Java MVC", SwingConstants.CENTER);
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        footerLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 6;
        gbc.insets = new java.awt.Insets(30, 0, 0, 0);
        mainPanel.add(footerLabel, gbc);

        add(mainPanel);
    }

    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public void setErrorMessage(String message) {
        errorLabel.setText(message);
        if (message != null && !message.trim().isEmpty()) {
            usernameField.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
            passwordField.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
        } else {
            usernameField.putClientProperty(FlatClientProperties.OUTLINE, null);
            passwordField.putClientProperty(FlatClientProperties.OUTLINE, null);
        }
    }

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
        // Also fire on enter in fields
        usernameField.addActionListener(listener);
        passwordField.addActionListener(listener);
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        errorLabel.setText(" ");
        usernameField.putClientProperty(FlatClientProperties.OUTLINE, null);
        passwordField.putClientProperty(FlatClientProperties.OUTLINE, null);
    }
}
