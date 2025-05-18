package com.evaluation.evaluationsystem.ui; // Adjust package if needed

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddStudentDialog extends JDialog {

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

    // Flag to indicate successful save
    private boolean saved = false;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // ID validation pattern (example: exactly 4 digits)
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d{4}$");

    public AddStudentDialog(Frame parent) {
        super(parent, "Add New Student", true); // Modal dialog
        setSize(450, 350); // Adjusted size
        setLocationRelativeTo(parent); // Center relative to parent
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Dispose on close

        initComponents();
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        idField = new JTextField(15);
        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(15);

        String[] yearLevels = {"1st Year", "2nd Year", "3rd Year", "4th Year"};
        yearLevelComboBox = new JComboBox<>(yearLevels);
        yearLevelComboBox.setSelectedItem("1st Year"); // Default to 1st Year

        String[] semesters = {"1st Semester", "2nd Semester"};
        semesterComboBox = new JComboBox<>(semesters);
        semesterComboBox.setSelectedItem("1st Semester"); // Default to 1st Semester

        saveButton = new JButton("Save Student & Create Initial Enrollment"); // Updated Button Text
        cancelButton = new JButton("Cancel");
    }

    private void layoutComponents() {
        // Layout remains the same as before...
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: ID
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Student ID (4 digits):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(idField, gbc);
        // Row 1: First Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(firstNameField, gbc);
        // Row 2: Last Name
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(lastNameField, gbc);
        // Row 3: Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(emailField, gbc);
        // Row 4: Year Level (Defaults to 1st Year)
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; formPanel.add(new JLabel("Starting Year Level:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(yearLevelComboBox, gbc);
        // Row 5: Semester (Defaults to 1st Semester)
        gbc.gridx = 0; gbc.gridy = 5; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; formPanel.add(new JLabel("Starting Semester:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(semesterComboBox, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        setLayout(new BorderLayout());
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Add action listeners to buttons.
     */
    private void addListeners() {
        saveButton.addActionListener(e -> performSaveAndInitialEnroll()); // Call the combined method
        cancelButton.addActionListener(e -> dispose());
    }

    private void performSaveAndInitialEnroll() {
        // 1. Get data from fields
        String id = idField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        // Use the selected values for the student's initial record, defaulting to 1st/1st
        String initialYearLevel = (String) yearLevelComboBox.getSelectedItem();
        String initialSemester = (String) semesterComboBox.getSelectedItem();

        // 2. Validate basic student input
        if (!validateInput(id, firstName, lastName, email)) {
            return; // Stop if validation fails
        }

        // --- Save Student and User ---
        Student newStudent = new Student(id, firstName, lastName, email, initialYearLevel, initialSemester);
        String password = generatePassword(id);
        User newUser = new User(id, password, "Student");

        try {
            DataStorage.saveStudent(newStudent);
            DataStorage.addUser(newUser);
            System.out.println("Student and User account created for ID: " + id);

            // --- Create Initial Enrollment (1st Year, 1st Semester) ---
            boolean initialEnrollmentCreated = createInitialEnrollment(id);

            // --- Show Final Message ---
            String enrollmentMessage = initialEnrollmentCreated ?
                    "\nInitial enrollment for 1st Year, 1st Semester created." :
                    "\nWarning: Could not create initial enrollment (check subject data).";

            JOptionPane.showMessageDialog(this,
                    "Student added successfully!\n\nUsername: " + id + "\nPassword: " + password +
                            "\n\nPlease provide this password to the student." + enrollmentMessage,
                    "Student Added", JOptionPane.INFORMATION_MESSAGE);

            saved = true; // Set flag indicating success
            dispose(); // Close the dialog

        } catch (IllegalArgumentException ex) {
            showError("Save Error", ex.getMessage());
        } catch (Exception ex) {
            showError("Save Error", "An unexpected error occurred while saving: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean createInitialEnrollment(String studentId) {
        String targetYear = "1st Year";
        String targetSemester = "1st Semester";

        System.out.println("Attempting to create initial enrollment for: " + studentId + " - " + targetYear + " " + targetSemester);

        try {
            // 1. Get all subjects from storage
            List<Subject> allSubjects = DataStorage.getAllSubjects();

            // 2. Filter subjects for 1st Year, 1st Semester
            List<Subject> firstSemSubjects = allSubjects.stream()
                    .filter(s -> targetYear.equalsIgnoreCase(s.getYearLevel()) &&
                            targetSemester.equalsIgnoreCase(s.getSemester()))
                    .collect(Collectors.toList());

            if (firstSemSubjects.isEmpty()) {
                System.err.println("Warning: No subjects found defined for " + targetYear + ", " + targetSemester + " in subjects.json.");
                return false; // Cannot create enrollment if no subjects are defined
            }

            System.out.println("Found " + firstSemSubjects.size() + " subjects for initial enrollment.");

            // 3. Create list of EnrolledSubject objects
            List<EnrolledSubject> subjectsToEnroll = new ArrayList<>();
            for (Subject subject : firstSemSubjects) {
                subjectsToEnroll.add(new EnrolledSubject(
                        subject.getCode(),
                        subject.getName(),
                        subject.getUnits(),
                        0.0 // Initial grade is 0.0 (NG)
                ));
            }

            // 4. Create the Enrollment object
            Enrollment initialEnrollment = new Enrollment(
                    studentId,
                    targetYear,
                    targetSemester,
                    "Enrolled", // Initial status
                    subjectsToEnroll
            );

            // 5. Save the Enrollment record
            DataStorage.saveEnrollment(initialEnrollment);
            System.out.println("Initial enrollment record saved successfully.");
            return true; // Success

        } catch (Exception e) {
            System.err.println("Error creating initial enrollment for student " + studentId + ": " + e.getMessage());
            e.printStackTrace();
            return false; // Indicate failure
        }
    }

    private boolean validateInput(String id, String firstName, String lastName, String email) {
        // Validation logic remains the same as before...
        if (id.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            showError("Validation Error", "All fields are required.");
            return false;
        }
        if (!ID_PATTERN.matcher(id).matches()) {
            showError("Validation Error", "Student ID must be exactly 4 digits.");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Validation Error", "Please enter a valid email address.");
            return false;
        }
        if (DataStorage.getStudentById(id).isPresent()) {
            showError("Validation Error", "Student ID '" + id + "' already exists.");
            return false;
        }
        // TODO: Check if username (ID) exists in users.json
        return true;
    }

    private String generatePassword(String studentId) {
        // Password generation logic remains the same...
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int suffixLength = 4;
        Random random = new Random();
        StringBuilder suffix = new StringBuilder(suffixLength);
        for (int i = 0; i < suffixLength; i++) {
            suffix.append(characters.charAt(random.nextInt(characters.length())));
        }
        return studentId + suffix.toString();
    }

    /**
     * Helper to show error messages.
     */
    private void showError(String title, String message) {
        // Error display logic remains the same...
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Public method to check if save was successful.
     */
    public boolean isSaved() {
        // Flag logic remains the same...
        return saved;
    }
}