package com.evaluation.evaluationsystem.ui; // Adjust package if needed

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.Student;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.util.regex.Pattern;

public class EditStudentDialog extends JDialog {

    // Input Fields
    private JTextField idField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JComboBox<String> yearLevelComboBox;
    private JComboBox<String> semesterComboBox;

    // Buttons
    private JButton saveButton;
    private JButton cancelButton;

    // Student ID being edited
    private final String studentIdToEdit;

    // Flag to indicate successful update
    private boolean saved = false;

    // Validation patterns (can be shared or defined here)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // Constructor now accepts the student ID
    public EditStudentDialog(Frame parent, String studentIdToEdit) {
        super(parent, "Edit Student Details", true); // Modal dialog
        this.studentIdToEdit = studentIdToEdit; // Store the ID

        setSize(450, 350);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        layoutComponents();
        addListeners();

        // Load existing data after components are initialized
        loadStudentData();
    }

    private void initComponents() {
        idField = new JTextField(15);
        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(15);

        String[] yearLevels = {"1st Year", "2nd Year", "3rd Year", "4th Year"};
        yearLevelComboBox = new JComboBox<>(yearLevels);

        String[] semesters = {"1st Semester", "2nd Semester"};
        semesterComboBox = new JComboBox<>(semesters);

        saveButton = new JButton("Save Changes"); // Changed button text
        cancelButton = new JButton("Cancel");
    }

    private void layoutComponents() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: ID (Non-Editable)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Student ID (Cannot Edit):"), gbc); // Updated label
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        idField.setEditable(false); // Make ID field non-editable
        idField.setBackground(Color.LIGHT_GRAY); // Visually indicate non-editable
        formPanel.add(idField, gbc);

        // Row 1: First Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(firstNameField, gbc);

        // Row 2: Last Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(lastNameField, gbc);

        // Row 3: Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(emailField, gbc);

        // Row 4: Year Level
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Year Level:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(yearLevelComboBox, gbc);

        // Row 5: Semester
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        formPanel.add(semesterComboBox, gbc);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        saveButton.addActionListener(e -> performSave());
        cancelButton.addActionListener(e -> dispose());
    }

    // Method to load existing student data into the fields
    private void loadStudentData() {
        Optional<Student> studentOptional = DataStorage.getStudentById(this.studentIdToEdit);
        if (studentOptional.isPresent()) {
            Student student = studentOptional.get();
            idField.setText(student.getId());
            firstNameField.setText(student.getFirstName());
            lastNameField.setText(student.getLastName());
            emailField.setText(student.getEmail());
            yearLevelComboBox.setSelectedItem(student.getYearLevel());
            semesterComboBox.setSelectedItem(student.getSemester());
        } else {
            // Handle case where student ID is somehow invalid (though shouldn't happen if called correctly)
            showError("Load Error", "Could not find student with ID: " + this.studentIdToEdit);
            saveButton.setEnabled(false); // Disable save if data couldn't load
        }
    }

    private void performSave() {
        // 1. Get data from fields
        // ID is taken from the stored studentIdToEdit, not the field
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String yearLevel = (String) yearLevelComboBox.getSelectedItem();
        String semester = (String) semesterComboBox.getSelectedItem();

        // 2. Validate data (excluding ID format/uniqueness check)
        if (!validateInput(firstName, lastName, email)) {
            return;
        }

        // 3. Create Student object with updated details and ORIGINAL ID
        Student updatedStudent = new Student(this.studentIdToEdit, firstName, lastName, email, yearLevel, semester);

        // 4. Save data using DataStorage (saveStudent handles updates)
        try {
            DataStorage.saveStudent(updatedStudent);

            // 5. Show success message
            JOptionPane.showMessageDialog(this,
                    "Student details updated successfully!",
                    "Update Successful", JOptionPane.INFORMATION_MESSAGE);

            saved = true; // Set flag
            dispose(); // Close the dialog

        } catch (Exception ex) {
            // Catch general errors during save
            showError("Save Error", "An unexpected error occurred while saving: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Updated validation for editing (no ID checks needed)
    private boolean validateInput(String firstName, String lastName, String email) {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            showError("Validation Error", "First Name, Last Name, and Email are required.");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Validation Error", "Please enter a valid email address.");
            return false;
        }
        return true; // All validations passed
    }

    // Helper to show error messages
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Public method to check if save was successful
    public boolean isSaved() {
        return saved;
    }
}