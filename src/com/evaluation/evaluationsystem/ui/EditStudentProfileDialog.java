package com.evaluation.evaluationsystem.ui; // Corrected package

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.Student;
import com.evaluation.evaluationsystem.models.User;
import com.evaluation.evaluationsystem.utils.JsonUtils; // Needed for saving users
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays; // For comparing password char arrays
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Dialog for students to edit their profile information (Email)
 * and change their password.
 */
public class EditStudentProfileDialog extends JDialog {

    // --- Data ---
    private final Student currentStudent; // Student object being edited

    // --- UI Components ---
    // Profile Info
    private JLabel studentIdLabel;
    private JLabel studentNameLabel;
    private JTextField emailField;

    // Password Change
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel passwordStatusLabel; // For password validation messages

    // Buttons
    private JButton saveButton;
    private JButton cancelButton;

    // --- State ---
    private boolean saved = false; // Flag if changes were successfully saved

    // Validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public EditStudentProfileDialog(Frame parent, Student student) {
        super(parent, "Edit Profile & Password", true); // Modal
        this.currentStudent = student;

        setSize(450, 400); // Adjusted size
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        layoutComponents();
        addListeners();

        loadProfileData(); // Load current data into fields
    }

    private void initComponents() {
        // Info Labels (Read-only)
        studentIdLabel = new JLabel(currentStudent.getId());
        studentNameLabel = new JLabel(currentStudent.getFirstName() + " " + currentStudent.getLastName());
        studentIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        studentNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));


        // Editable Fields
        emailField = new JTextField(25);

        // Password Fields
        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        passwordStatusLabel = new JLabel(" "); // Placeholder for messages
        passwordStatusLabel.setForeground(Color.RED);
        passwordStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));


        // Buttons
        saveButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");
    }

    private void loadProfileData() {
        // Load current email
        emailField.setText(currentStudent.getEmail());
        // Clear password fields for security
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 15)); // Gaps
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Padding

        // --- Profile Info Panel ---
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(new TitledBorder("Profile Information"));
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.insets = new Insets(4, 4, 4, 4);
        gbcInfo.anchor = GridBagConstraints.WEST;

        // Row 0: ID
        gbcInfo.gridx = 0; gbcInfo.gridy = 0; infoPanel.add(new JLabel("Student ID:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.gridy = 0; infoPanel.add(studentIdLabel, gbcInfo);

        // Row 1: Name
        gbcInfo.gridx = 0; gbcInfo.gridy = 1; infoPanel.add(new JLabel("Name:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.gridy = 1; infoPanel.add(studentNameLabel, gbcInfo);

        // Row 2: Email (Editable)
        gbcInfo.gridx = 0; gbcInfo.gridy = 2; infoPanel.add(new JLabel("Email:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.gridy = 2; gbcInfo.fill = GridBagConstraints.HORIZONTAL; gbcInfo.weightx = 1.0;
        infoPanel.add(emailField, gbcInfo);

        // --- Password Change Panel ---
        JPanel passwordPanel = new JPanel(new GridBagLayout());
        passwordPanel.setBorder(new TitledBorder("Change Password (Optional)"));
        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.insets = new Insets(4, 4, 4, 4);
        gbcPass.anchor = GridBagConstraints.WEST;

        // Row 0: Current Password
        gbcPass.gridx = 0; gbcPass.gridy = 0; passwordPanel.add(new JLabel("Current Password:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 0; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passwordPanel.add(currentPasswordField, gbcPass);

        // Row 1: New Password
        gbcPass.gridx = 0; gbcPass.gridy = 1; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0;
        passwordPanel.add(new JLabel("New Password:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 1; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passwordPanel.add(newPasswordField, gbcPass);

        // Row 2: Confirm Password
        gbcPass.gridx = 0; gbcPass.gridy = 2; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0;
        passwordPanel.add(new JLabel("Confirm New Password:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 2; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passwordPanel.add(confirmPasswordField, gbcPass);

        // Row 3: Status Label
        gbcPass.gridx = 1; gbcPass.gridy = 3; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0;
        passwordPanel.add(passwordStatusLabel, gbcPass);


        // --- Combine Panels ---
        // Use BoxLayout to stack info and password panels vertically
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(infoPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacer
        centerPanel.add(passwordPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

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
        // --- 1. Get Data ---
        String newEmail = emailField.getText().trim();
        char[] currentPassword = currentPasswordField.getPassword();
        char[] newPassword = newPasswordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        // --- 2. Validate Email ---
        if (newEmail.isEmpty()) {
            showError("Validation Error", "Email address cannot be empty.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
            showError("Validation Error", "Invalid email address format.");
            return;
        }

        // --- 3. Handle Password Change (if attempted) ---
        boolean passwordChangeAttempted = currentPassword.length > 0 || newPassword.length > 0 || confirmPassword.length > 0;
        boolean passwordChanged = false;

        if (passwordChangeAttempted) {
            passwordStatusLabel.setText(" "); // Clear previous status

            // 3a. Check if current password field is filled
            if (currentPassword.length == 0) {
                passwordStatusLabel.setText("Current Password required to change password.");
                return;
            }

            // 3b. Verify current password
            Optional<User> userOpt = DataStorage.authenticateUser(currentStudent.getId(), new String(currentPassword), "Student");
            if (userOpt.isEmpty()) {
                passwordStatusLabel.setText("Incorrect Current Password.");
                // Clear fields for retry? Maybe just current pass field.
                currentPasswordField.setText("");
                currentPasswordField.requestFocus();
                return;
            }

            // 3c. Check if new password fields are filled
            if (newPassword.length == 0 || confirmPassword.length == 0) {
                passwordStatusLabel.setText("New Password and Confirmation are required.");
                return;
            }

            // 3d. Check if new passwords match
            if (!Arrays.equals(newPassword, confirmPassword)) {
                passwordStatusLabel.setText("New passwords do not match.");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                newPasswordField.requestFocus();
                return;
            }

            // 3e. Optional: Add password complexity rules here if desired

            // 3f. If all checks pass, update password in users.json
            try {
                updateUserPassword(currentStudent.getId(), new String(newPassword));
                passwordChanged = true;
                passwordStatusLabel.setText("Password updated successfully."); // Temporary success message
                passwordStatusLabel.setForeground(Color.GREEN.darker());
            } catch (Exception ex) {
                showError("Password Update Error", "Could not update password: " + ex.getMessage());
                ex.printStackTrace();
                return; // Stop if password update failed
            }

        } else {
            passwordStatusLabel.setText(" "); // Clear status if no change attempted
        }

        // --- 4. Update Student Email (if changed) ---
        boolean emailChanged = !newEmail.equals(currentStudent.getEmail());
        if (emailChanged) {
            currentStudent.setEmail(newEmail);
            // Note: saveStudent will be called below, saving both email and potentially other fields if added
        }

        // --- 5. Save Student Data (if email changed) and Finalize ---
        if (emailChanged || passwordChanged) { // Only save if something actually changed
            try {
                DataStorage.saveStudent(currentStudent); // Save updated student object (updates email in students.json)
                saved = true; // Mark as saved

                // Show final success message
                String successMessage = "Profile updated successfully.";
                if (passwordChanged) {
                    successMessage += " Password was changed.";
                }
                JOptionPane.showMessageDialog(this, successMessage, "Update Successful", JOptionPane.INFORMATION_MESSAGE);

                dispose(); // Close the dialog

            } catch (Exception ex) {
                showError("Profile Save Error", "Could not save student profile changes: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            // If nothing changed (only email was same and no password attempt)
            JOptionPane.showMessageDialog(this, "No changes were detected.", "No Changes", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Still close the dialog
        }

        // Clear password fields from memory for security
        Arrays.fill(currentPassword, ' ');
        Arrays.fill(newPassword, ' ');
        Arrays.fill(confirmPassword, ' ');
    }

    // Helper method to update password in users.json
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
            // This should ideally not happen if the current password was verified
            throw new RuntimeException("User '" + username + "' not found in users file during password update.");
        }
    }


    // Helper to show error messages
    private void showError(String title, String message) {
        passwordStatusLabel.setText(message); // Show password errors in dedicated label
        passwordStatusLabel.setForeground(Color.RED);
        // Also show general errors in a popup
        if (!title.equals("Validation Error") && !title.equals("Password Update Error")) {
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    // Public method to check if save was successful
    public boolean isSaved() {
        return saved;
    }
}