package com.evaluation.evaluationsystem.ui;

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.User;
import com.evaluation.evaluationsystem.utils.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;


public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JRadioButton adminRadio, teacherRadio, studentRadio;
    private ButtonGroup roleGroup;
    private JButton loginButton;
    private JLabel statusLabel; // To show login errors

    public LoginFrame() {
        setTitle("Student Evaluation System - Login");
        setSize(400, 300); // Adjust size as needed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
        setLayout(new GridBagLayout()); // Use GridBagLayout for flexibility

        initComponents();
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);

        adminRadio = new JRadioButton("Admin");
        teacherRadio = new JRadioButton("Teacher");
        studentRadio = new JRadioButton("Student");
        studentRadio.setSelected(true); // Default selection

        roleGroup = new ButtonGroup();
        roleGroup.add(adminRadio);
        roleGroup.add(teacherRadio);
        roleGroup.add(studentRadio);

        loginButton = new JButton("Login");
        statusLabel = new JLabel(" ", SwingConstants.CENTER); // Start with empty space
        statusLabel.setForeground(Color.RED);
    }

    private void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding around components
        gbc.anchor = GridBagConstraints.WEST; // Align labels to the left

        // Username Row
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Allow field to expand horizontally
        add(usernameField, gbc);

        // Password Row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE; // Reset fill
        gbc.weightx = 0; // Reset weight
        add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(passwordField, gbc);

        // Role Selection Row
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Span across two columns
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; // Center radio buttons
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        rolePanel.add(adminRadio);
        rolePanel.add(teacherRadio);
        rolePanel.add(studentRadio);
        add(rolePanel, gbc);

        // Login Button Row
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        // Status Label Row
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        add(statusLabel, gbc);
    }


    private void addListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        // Add action listener for pressing Enter in password field
        passwordField.addActionListener(e -> handleLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus()); // Move focus on Enter
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String role = getSelectedRole();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            statusLabel.setText("Please fill in all fields and select a role.");
            return;
        }

        Optional<User> authenticatedUser = DataStorage.authenticateUser(username, password, role);

        if (authenticatedUser.isPresent()) {
            statusLabel.setText("Login successful!");
            statusLabel.setForeground(Color.GREEN);
            SessionManager.loginUser(authenticatedUser.get().getUsername(), authenticatedUser.get().getRole());
            // Open the corresponding dashboard
            openDashboard(authenticatedUser.get().getRole());
            dispose(); // Close the login window
        } else {
            statusLabel.setText("Invalid username, password, or role.");
            statusLabel.setForeground(Color.RED);
        }
    }

    private String getSelectedRole() {
        if (adminRadio.isSelected()) return "Admin";
        if (teacherRadio.isSelected()) return "Teacher";
        if (studentRadio.isSelected()) return "Student";
        return null;
    }

    private void openDashboard(String role) {
        // Use SwingUtilities.invokeLater to ensure GUI updates happen on the EDT
        SwingUtilities.invokeLater(() -> {
            JFrame dashboardFrame = null;
            switch (role.toLowerCase()) {
                case "admin":
                    dashboardFrame = new AdminDashboardFrame(); // Create this class
                    break;
                case "teacher":
                    dashboardFrame = new TeacherDashboardFrame(); // Create this class
                    break;
                case "student":
                    dashboardFrame = new StudentDashboardFrame(); // Create this class
                    break;
                default:
                    System.err.println("Unknown role: " + role);
                    statusLabel.setText("Login succeeded but dashboard not found.");
                    return;
            }
            dashboardFrame.setVisible(true);
        });
    }
}