package com.evaluation.evaluationsystem.ui; // Adjust package if needed

import com.evaluation.evaluationsystem.utils.SessionManager;
import com.evaluation.evaluationsystem.ui.AdminStudentPanel;
import com.evaluation.evaluationsystem.ui.AdminTeacherPanel;
import com.evaluation.evaluationsystem.ui.AdminSubjectPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AdminDashboardFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private AdminStudentPanel studentManagementPanel;
    private JPanel teacherManagementPanel;
    private AdminSubjectPanel subjectManagementPanel;
    private JButton logoutButton;
    private JButton changePasswordButton;

    public AdminDashboardFrame() {
        // Check if user is logged in (basic security)
        if (!SessionManager.isLoggedIn() || !"Admin".equalsIgnoreCase(SessionManager.getCurrentUserRole())) {
            // If not logged in or not an admin, show error and close
            JOptionPane.showMessageDialog(null, "Access Denied. Please login as Admin.", "Error", JOptionPane.ERROR_MESSAGE);
            // Optionally redirect to login frame
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
            // Use dispose() in a separate event to avoid issues during constructor execution
            SwingUtilities.invokeLater(this::dispose);
            return; // Stop constructor execution
        }


        setTitle("Admin Dashboard - Welcome " + SessionManager.getCurrentUserId());
        setSize(900, 700); // Increased size for better viewing of tables
        setMinimumSize(new Dimension(700, 500)); // Set a minimum size
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Handle logout confirmation on close
        setLocationRelativeTo(null); // Center the window

        initComponents();
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        tabbedPane = new JTabbedPane();

        // --- Instantiate Panels ---
        // Student Panel (
        studentManagementPanel = new AdminStudentPanel();

        // Teacher Panel
        teacherManagementPanel = new AdminTeacherPanel();

        // Subject Panel
        subjectManagementPanel = new AdminSubjectPanel();

        //edit password
        changePasswordButton = new JButton("Edit Password");
        // Logout Button
        logoutButton = new JButton("Logout");
        logoutButton.setToolTipText("Logout and return to login screen"); // Add tooltip
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor on hover
    }

    private void layoutComponents() {
        // Set main layout
        setLayout(new BorderLayout(10, 10)); // Add gaps

        // --- Add Tabs ---
        // Set icons for tabs (optional, replace null with actual ImageIcons)
        // Example: tabbedPane.addTab("Students", new ImageIcon(getClass().getResource("/icons/student_icon.png")), studentManagementPanel, "Manage Students");
        tabbedPane.addTab("Students", null, studentManagementPanel, "Manage Students");
        tabbedPane.addTab("Teachers", null, teacherManagementPanel, "Manage Teachers");
        tabbedPane.addTab("Subjects", null, subjectManagementPanel, "Manage Subjects");

        // Add tabbed pane to the center
        add(tabbedPane, BorderLayout.CENTER);

        // --- Bottom Panel for Logout ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Align right
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Add padding
        bottomPanel.add(changePasswordButton); // Add change password button
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(logoutButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        // Logout Button Action
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogout();
            }
        });

        changePasswordButton.addActionListener(e -> handleChangePassword());

        // Window Closing Action (clicking the 'X')
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleLogout(); // Prompt before closing
            }
        });
    }

    private void handleLogout() {
        int confirmation = JOptionPane.showConfirmDialog(
                this, // Parent component
                "Are you sure you want to logout?", // Message
                "Logout Confirmation", // Title
                JOptionPane.YES_NO_OPTION, // Button options
                JOptionPane.QUESTION_MESSAGE // Icon type
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            SessionManager.logout(); // Clear the session
            dispose(); // Close the current admin dashboard window

            // Use SwingUtilities.invokeLater to ensure the LoginFrame is created on the EDT
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    LoginFrame loginFrame = new LoginFrame();
                    loginFrame.setVisible(true); // Show the login window again
                }
            });
        }
        // If NO_OPTION or dialog closed, do nothing and stay on the dashboard.
    }

    private void handleChangePassword()
    {
        EditAdminProfileDialog passwordDialog = new EditAdminProfileDialog(this);
        passwordDialog.setVisible(true);
    }

    // --- Main method for testing this frame directly (optional) ---
    /*
    public static void main(String[] args) {
        // For testing purposes only
        SessionManager.loginUser("testAdmin", "Admin"); // Simulate login
        SwingUtilities.invokeLater(() -> {
            AdminDashboardFrame frame = new AdminDashboardFrame();
            frame.setVisible(true);
        });
    }
    */
}