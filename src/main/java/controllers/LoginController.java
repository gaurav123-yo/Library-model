package controllers;

import database.UserDAO;
import database.StudentDAO;
import models.User;
import models.Student;
import utils.PasswordHasher;
import views.LoginView;
import views.MainDashboardView;
import views.LibrarianDashboardPanel;
import views.StudentDashboardPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginController {
    private final LoginView loginView;
    private final MainDashboardView dashboardView;
    private final UserDAO userDAO;
    private final StudentDAO studentDAO;

    public LoginController(LoginView loginView, MainDashboardView dashboardView) {
        this.loginView = loginView;
        this.dashboardView = dashboardView;
        this.userDAO = new UserDAO();
        this.studentDAO = new StudentDAO();

        this.loginView.addLoginListener(new LoginActionListener());
        this.dashboardView.addLogoutListener(new LogoutActionListener());
    }

    public void showLogin() {
        loginView.clearFields();
        loginView.setVisible(true);
        dashboardView.setVisible(false);
    }

    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = loginView.getUsername();
            String password = loginView.getPassword();

            if (username.isEmpty() || password.isEmpty()) {
                loginView.setErrorMessage("Please enter both username and password.");
                return;
            }

            // Fetch user from DB
            User user = userDAO.getUserByUsername(username);
            if (user == null) {
                // If not found in users, check if it matches a studentId directly (allow studentId as username fallback)
                Student student = studentDAO.getStudentById(username);
                if (student != null) {
                    user = userDAO.getUserByUsername(username); // retry username
                }
            }

            if (user != null && PasswordHasher.verifyPassword(password, user.getPassword())) {
                loginView.setErrorMessage("");
                launchDashboard(user);
            } else {
                loginView.setErrorMessage("Invalid username or password.");
            }
        }
    }

    private class LogoutActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showConfirmDialog(
                    dashboardView, 
                    "Are you sure you want to log out?", 
                    "Logout Confirmation", 
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                showLogin();
            }
        }
    }

    private void launchDashboard(User user) {
        dashboardView.setSessionUser(user);

        if (user.getRole().equalsIgnoreCase("LIBRARIAN")) {
            // Instantiate Librarian Panel and Controller
            LibrarianDashboardPanel librarianPanel = new LibrarianDashboardPanel();
            new LibrarianController(librarianPanel, dashboardView);
            dashboardView.setDashboardPanel(librarianPanel);
        } else if (user.getRole().equalsIgnoreCase("STUDENT")) {
            // Locate Student Profile
            String studentId = user.getStudentId();
            Student student = studentDAO.getStudentById(studentId);
            
            if (student == null) {
                JOptionPane.showMessageDialog(loginView, 
                        "Student profile not found for this user account.", 
                        "Data Integrity Error", 
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Instantiate Student Panel and Controller
            StudentDashboardPanel studentPanel = new StudentDashboardPanel();
            new StudentController(studentPanel, student, dashboardView);
            dashboardView.setDashboardPanel(studentPanel);
        } else {
            loginView.setErrorMessage("Invalid user role detected.");
            return;
        }

        loginView.setVisible(false);
        dashboardView.setVisible(true);
    }
}
