package com.evaluation.evaluationsystem.ui; // Corrected package

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.Teacher;
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
 * Dialog for teachers to edit their profile information (Email, Dept, Pos)
 * and change their password.
 */
public class EditTeacherProfileDialog extends JDialog {

    // --- Data ---
    private final Teacher currentTeacher; // Teacher object being edited

    // --- UI Components ---
    // Profile Info
    private JLabel teacherIdLabel;
    private JLabel teacherNameLabel;
    private JTextField emailField;
    private JTextField departmentField; // Editable
    private JTextField positionField;   // Editable

    // Password Change
    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel passwordStatusLabel;

    // Buttons
    private JButton saveButton;
    private JButton cancelButton;

    // --- State ---
    private boolean saved = false; // Flag if changes were successfully saved

    // Validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    public EditTeacherProfileDialog(Frame parent, Teacher teacher) {
        super(parent, "Edit Profile & Password", true); // Modal
        this.currentTeacher = teacher;

        setSize(480, 520); // Adjusted size
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        layoutComponents();
        addListeners();

        loadProfileData(); // Load current data into fields
    }

    private void initComponents() {
        // Info Labels (Read-only)
        teacherIdLabel = new JLabel(currentTeacher.getId());
        teacherNameLabel = new JLabel(currentTeacher.getFirstName() + " " + currentTeacher.getLastName());
        teacherIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        teacherNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Editable Fields
        emailField = new JTextField(25);
        departmentField = new JTextField(25);
        positionField = new JTextField(25);

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
        // Load current editable data
        emailField.setText(currentTeacher.getEmail());
        departmentField.setText(currentTeacher.getDepartment());
        positionField.setText(currentTeacher.getPosition());
        // Clear password fields
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Profile Info Panel ---
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(new TitledBorder("Profile Information"));
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.insets = new Insets(4, 4, 4, 4);
        gbcInfo.anchor = GridBagConstraints.WEST;
        gbcInfo.fill = GridBagConstraints.HORIZONTAL; // Make fields fill horizontally

        // Row 0: ID
        gbcInfo.gridx = 0; gbcInfo.gridy = 0; gbcInfo.fill = GridBagConstraints.NONE; infoPanel.add(new JLabel("Teacher ID:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.gridy = 0; gbcInfo.weightx = 1.0; infoPanel.add(teacherIdLabel, gbcInfo);

        // Row 1: Name
        gbcInfo.gridx = 0; gbcInfo.gridy = 1; gbcInfo.weightx = 0; infoPanel.add(new JLabel("Name:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.gridy = 1; gbcInfo.weightx = 1.0; infoPanel.add(teacherNameLabel, gbcInfo);

        // Row 2: Email (Editable)
        gbcInfo.gridx = 0; gbcInfo.gridy = 2; gbcInfo.weightx = 0; infoPanel.add(new JLabel("Email:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.gridy = 2; gbcInfo.weightx = 1.0; infoPanel.add(emailField, gbcInfo);

        // Row 3: Department (Editable)
        gbcInfo.gridx = 0; gbcInfo.gridy = 3; gbcInfo.weightx = 0; infoPanel.add(new JLabel("Department:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.gridy = 3; gbcInfo.weightx = 1.0; infoPanel.add(departmentField, gbcInfo);

        // Row 4: Position (Editable)
        gbcInfo.gridx = 0; gbcInfo.gridy = 4; gbcInfo.weightx = 0; infoPanel.add(new JLabel("Position:"), gbcInfo);
        gbcInfo.gridx = 1; gbcInfo.gridy = 4; gbcInfo.weightx = 1.0; infoPanel.add(positionField, gbcInfo);


        // --- Password Change Panel ---
        JPanel passwordPanel = new JPanel(new GridBagLayout());
        passwordPanel.setBorder(new TitledBorder("Change Password (Optional)"));
        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.insets = new Insets(4, 4, 4, 4);
        gbcPass.anchor = GridBagConstraints.WEST;
        gbcPass.fill = GridBagConstraints.HORIZONTAL; // Make fields fill

        // Row 0: Current Password
        gbcPass.gridx = 0; gbcPass.gridy = 0; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0; passwordPanel.add(new JLabel("Current Password:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 0; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0; passwordPanel.add(currentPasswordField, gbcPass);

        // Row 1: New Password
        gbcPass.gridx = 0; gbcPass.gridy = 1; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0; passwordPanel.add(new JLabel("New Password:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 1; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0; passwordPanel.add(newPasswordField, gbcPass);

        // Row 2: Confirm Password
        gbcPass.gridx = 0; gbcPass.gridy = 2; gbcPass.fill = GridBagConstraints.NONE; gbcPass.weightx = 0; passwordPanel.add(new JLabel("Confirm New Password:"), gbcPass);
        gbcPass.gridx = 1; gbcPass.gridy = 2; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0; passwordPanel.add(confirmPasswordField, gbcPass);

        // Row 3: Status Label
        gbcPass.gridx = 1; gbcPass.gridy = 3; gbcPass.fill = GridBagConstraints.HORIZONTAL; gbcPass.weightx = 1.0; passwordPanel.add(passwordStatusLabel, gbcPass);


        // --- Combine Panels ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(infoPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10)));
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
        String newDepartment = departmentField.getText().trim();
        String newPosition = positionField.getText().trim();
        char[] currentPassword = currentPasswordField.getPassword();
        char[] newPassword = newPasswordField.getPassword();
        char[] confirmPassword = confirmPasswordField.getPassword();

        // --- 2. Validate Profile Fields ---
        if (newEmail.isEmpty() || newDepartment.isEmpty() || newPosition.isEmpty()) {
            showError("Validation Error", "Email, Department, and Position cannot be empty.");
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
            if (currentPassword.length == 0) {
                passwordStatusLabel.setText("Current Password required to change password."); return;
            }
            Optional<User> userOpt = DataStorage.authenticateUser(currentTeacher.getId(), new String(currentPassword), "Teacher");
            if (userOpt.isEmpty()) {
                passwordStatusLabel.setText("Incorrect Current Password."); currentPasswordField.setText(""); currentPasswordField.requestFocus(); return;
            }
            if (newPassword.length == 0 || confirmPassword.length == 0) {
                passwordStatusLabel.setText("New Password and Confirmation are required."); return;
            }
            if (!Arrays.equals(newPassword, confirmPassword)) {
                passwordStatusLabel.setText("New passwords do not match."); newPasswordField.setText(""); confirmPasswordField.setText(""); newPasswordField.requestFocus(); return;
            }
            // Optional: Add password complexity rules here

            try {
                updateUserPassword(currentTeacher.getId(), new String(newPassword));
                passwordChanged = true;
                passwordStatusLabel.setText("Password updated successfully.");
                passwordStatusLabel.setForeground(Color.GREEN.darker());
            } catch (Exception ex) {
                showError("Password Update Error", "Could not update password: " + ex.getMessage()); ex.printStackTrace(); return;
            }
        } else {
            passwordStatusLabel.setText(" ");
        }

        // --- 4. Update Teacher Object (if changed) ---
        boolean profileChanged = !newEmail.equals(currentTeacher.getEmail()) ||
                !newDepartment.equals(currentTeacher.getDepartment()) ||
                !newPosition.equals(currentTeacher.getPosition());

        if (profileChanged) {
            // Update the existing Teacher object in memory before saving
            // Note: We are NOT changing the assignedSubjectCodes here
            currentTeacher.setEmail(newEmail);
            currentTeacher.setDepartment(newDepartment);
            currentTeacher.setPosition(newPosition);
        }

        // --- 5. Save Teacher Data (if profile changed) and Finalize ---
        if (profileChanged || passwordChanged) {
            try {
                if (profileChanged) { // Only save teacher details if they actually changed
                    DataStorage.saveTeacher(currentTeacher); // Save updated teacher object
                }
                saved = true;

                String successMessage = "Profile updated successfully.";
                if (passwordChanged) successMessage += " Password was changed.";
                JOptionPane.showMessageDialog(this, successMessage, "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            } catch (Exception ex) {
                showError("Profile Save Error", "Could not save teacher profile changes: " + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "No changes were detected.", "No Changes", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }

        // Clear password arrays
        Arrays.fill(currentPassword, ' ');
        Arrays.fill(newPassword, ' ');
        Arrays.fill(confirmPassword, ' ');
    }

    // Helper method to update password in users.json (same as in EditStudentProfileDialog)
    private void updateUserPassword(String username, String newPassword) {
        JSONObject usersData = JsonUtils.loadUsers();
        JSONArray usersArray = JsonUtils.getJSONArray(usersData, "users");
        boolean found = false;
        for (Object obj : usersArray) {
            JSONObject userJson = (JSONObject) obj;
            if (username.equals(JsonUtils.getString(userJson, "username", ""))) {
                userJson.put("password", newPassword);
                found = true;
                break;
            }
        }
        if (found) {
            JsonUtils.saveUsers(usersData);
        } else {
            throw new RuntimeException("User '" + username + "' not found in users file during password update.");
        }
    }

    // Helper to show error messages
    private void showError(String title, String message) {
        passwordStatusLabel.setText(message);
        passwordStatusLabel.setForeground(Color.RED);
        if (!title.contains("Validation") && !title.contains("Password")) { // Avoid double popups for password errors
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }

    // Public method to check if save was successful
    public boolean isSaved() {
        return saved;
    }
}