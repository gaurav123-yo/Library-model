import com.formdev.flatlaf.FlatLightLaf;
import controllers.LoginController;
import database.MongoDBConnection;
import views.LoginView;
import views.MainDashboardView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Setup FlatLaf look and feel
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf theme. Reverting to default Swing.");
        }

        // Run UI initialization on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            // First connect/initialize database.
            // If connection fails, this will prompt the user with a dialog recursively
            // until a connection is made or the app exits.
            try {
                System.out.println("Initializing database connection...");
                MongoDBConnection.getDatabase();
            } catch (Exception e) {
                System.err.println("Fatal database initialization error: " + e.getMessage());
                JOptionPane.showMessageDialog(null, 
                        "Failed to initialize database. Please check logs and restart the app.", 
                        "Fatal Error", 
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // Create Views
            LoginView loginView = new LoginView();
            MainDashboardView dashboardView = new MainDashboardView();

            // Create Controller to link them
            new LoginController(loginView, dashboardView);

            // Show login screen
            loginView.setVisible(true);
            System.out.println("Smart Library Management System is running!");
        });
    }
}
