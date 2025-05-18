package com.evaluation.evaluationsystem.ui; // Corrected package

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.User;
import com.evaluation.evaluationsystem.utils.JsonUtils; // Needed for saving users
import com.evaluation.evaluationsystem.utils.SessionManager; // Needed to get current user ID
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Optional;

/**
 * Dialog for Admins to change their own password.
 */
public class EditAdminProfileDialog extends JDialog {

    // --- UI Components ---
    private JLabel adminUsernameLabel; // Display the username being changed
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel passwordStatusLabel; // For password validation messages

    // Buttons
    private JButton saveButton;
    private JButton cancelButton;

    // --- State ---
    private boolean saved = false; // Flag if changes were successfully saved
    private final String adminUsername; // Store the username

    public EditAdminProfileDialog(Frame parent) {
        super(parent, "Change Admin Password", true); // Modal
        this.adminUsername = SessionManager.getCurrentUserId(); // Get logged-in admin's username

        if (this.adminUsername == null || this.adminUsername.isEmpty()) {
            // Should not happen if called correctly, but good practice to check
            showError("Error", "Could not identify logged-in admin user.");
            // Prevent dialog from showing properly if user is invalid
            SwingUtilities.invokeLater(this::dispose);
            return;
        }

        setSize(400, 300); // Smaller dialog is sufficient
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        // Display Username (Read-only)
        adminUsernameLabel = new JLabel(this.adminUsername);
        adminUsernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Password Fields
        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        passwordStatusLabel = new JLabel(" "); // Placeholder for messages
        passwordStatusLabel.setForeground(Color.RED);
        passwordStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        // Buttons
        saveButton = new JButton("Save New Password");
        cancelButton = new JButton("Cancel");
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Password Change Panel ---
        JPanel passwordPanel = new JPanel(new GridBagLayout());
        passwordPanel.setBorder(new TitledBorder("Change Password for User:")); // Title includes username
        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.insets = new Insets(5, 5, 5, 5); // Increased vertical spacing
        gbcPass.anchor = GridBagConstraints.WEST;

        // Row 0: Display Username
        gbcPass.gridx = 0; gbcPass.gridy = 0; passwordPanel.add(new JLabel("Admin User:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 0; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passwordPanel.add(adminUsernameLabel, gbcPass); // Display the username

        // Row 1: Current Password
        gbcPass.gridx = 0; gbcPass.gridy = 1; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0;
        passwordPanel.add(new JLabel("Current Password:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 1; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passwordPanel.add(currentPasswordField, gbcPass);

        // Row 2: New Password
        gbcPass.gridx = 0; gbcPass.gridy = 2; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0;
        passwordPanel.add(new JLabel("New Password:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 2; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passwordPanel.add(newPasswordField, gbcPass);

        // Row 3: Confirm Password
        gbcPass.gridx = 0; gbcPass.gridy = 3; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0;
        passwordPanel.add(new JLabel("Confirm New Password:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 3; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passwordPanel.add(confirmPasswordField, gbcPass);

        // Row 4: Status Label
        gbcPass.gridx = 1; gbcPass.gridy = 4; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passwordPanel.add(passwordStatusLabel, gbcPass);

        mainPanel.add(passwordPanel, BorderLayout.CENTER); // Add panel to center

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void addListeners() {
        saveButton.addActionListener(e -> performSave());
        cancelButton.addActionListener(e -> dispose());
    }

    private void performSave() {
        // --- 1. Get Passwords ---
        char[] currentPassword = currentPasswordField.getPassword();
        char[] newPassword = newPasswordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        // --- 2. Validate Input ---
        passwordStatusLabel.setText(" "); // Clear previous status
        if (currentPassword.length == 0 || newPassword.length == 0 || confirmPassword.length == 0) {
            showError("Validation Error", "All password fields are required.");
            return;
        }
        if (!Arrays.equals(newPassword, confirmPassword)) {
            showError("Validation Error", "New passwords do not match.");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            newPasswordField.requestFocus();
            return;
        }
        // Optional: Add password complexity rules here

        // --- 3. Verify Current Password ---
        Optional<User> userOpt = DataStorage.authenticateUser(this.adminUsername, new String(currentPassword), "Admin"); // Use "Admin" role
        if (userOpt.isEmpty()) {
            showError("Authentication Error", "Incorrect Current Password.");
            currentPasswordField.setText("");
            currentPasswordField.requestFocus();
            return;
        }

        // --- 4. Update Password in users.json ---
        try {
            updateUserPassword(this.adminUsername, new String(newPassword));
            saved = true;
            JOptionPane.showMessageDialog(this, "Admin password updated successfully!", "Password Changed", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close the dialog

        } catch (Exception ex) {
            showError("Password Update Error", "Could not update password: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            // Clear password arrays from memory for security
            Arrays.fill(currentPassword, ' ');
            Arrays.fill(newPassword, ' ');
            Arrays.fill(confirmPassword, ' ');
        }
    }

    // Helper method to update password in users.json (Identical to other dialogs)
    private void updateUserPassword(String username, String newPassword) {
        JSONObject usersData = JsonUtils.loadUsers();
        JSONArray usersArray = JsonUtils.getJSONArray(usersData, "users");
        boolean found = false;
        for (Object obj : usersArray) {
            JSONObject userJson = (JSONObject) obj;
            if (username.equals(JsonUtils.getString(userJson, "username", ""))) {
                userJson.put("password", newPassword); // Update the password
                found = true;
                break;
            }
        }
        if (found) {
            JsonUtils.saveUsers(usersData); // Save the updated users file
        } else {
            throw new RuntimeException("Admin user '" + username + "' not found in users file during password update.");
        }
    }

    // Helper to show error messages (primarily in the status label)
    private void showError(String title, String message) {
        passwordStatusLabel.setText(message);
        passwordStatusLabel.setForeground(Color.RED);
        // Optionally show a popup for non-validation errors
        if (!title.contains("Validation") && !title.contains("Authentication")) {
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    // Public method to check if save was successful
    public boolean isSaved() {
        return saved;
    }
}