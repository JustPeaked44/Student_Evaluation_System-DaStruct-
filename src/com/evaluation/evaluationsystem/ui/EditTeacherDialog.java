package com.evaluation.evaluationsystem.ui; // Corrected package

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.Subject;
import com.evaluation.evaluationsystem.models.Teacher;

import javax.swing.*;
import javax.swing.border.TitledBorder; // Import TitledBorder
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList; // Import ArrayList
import java.util.Comparator; // Import Comparator
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors; // Import Collectors

public class EditTeacherDialog extends JDialog {

    // --- Input Fields ---
    private JTextField idField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField departmentField;
    private JTextField positionField;
    // private JComboBox<String> subjectComboBox; // REMOVE Single Subject ComboBox

    // --- NEW: Subject Assignment Components ---
    private JList<Subject> availableSubjectsList;
    private JList<Subject> assignedSubjectsList;
    private DefaultListModel<Subject> availableSubjectsModel;
    private DefaultListModel<Subject> assignedSubjectsModel;
    private JButton addSubjectButton;
    private JButton removeSubjectButton;
    private List<Subject> allSubjects; // Cache all subjects

    // --- Buttons ---
    private JButton saveButton;
    private JButton cancelButton;

    // --- State ---
    private final String teacherIdToEdit; // Store the ID of the teacher being edited
    private boolean saved = false;

    // --- Validation ---
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    // ID pattern remains the same (or adjust if needed)
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");


    // Constructor accepts the teacher ID to edit
    public EditTeacherDialog(Frame parent, String teacherIdToEdit) {
        super(parent, "Edit Teacher Details", true); // Updated title
        this.teacherIdToEdit = teacherIdToEdit;

        setSize(650, 550); // Adjusted size for lists
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        loadAvailableSubjects(); // Load all subjects FIRST
        loadTeacherData();     // THEN load teacher data to populate assigned list
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        idField = new JTextField(15);
        firstNameField = new JTextField(15);
        lastNameField = new JTextField(15);
        emailField = new JTextField(15);
        departmentField = new JTextField(15);
        positionField = new JTextField(15);

        // --- Initialize Subject Assignment Lists ---
        availableSubjectsModel = new DefaultListModel<>();
        availableSubjectsList = new JList<>(availableSubjectsModel);
        availableSubjectsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        availableSubjectsList.setVisibleRowCount(8);

        assignedSubjectsModel = new DefaultListModel<>();
        assignedSubjectsList = new JList<>(assignedSubjectsModel);
        assignedSubjectsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        assignedSubjectsList.setVisibleRowCount(8);

        addSubjectButton = new JButton(">>");
        addSubjectButton.setToolTipText("Assign selected subject(s) to teacher");
        removeSubjectButton = new JButton("<<");
        removeSubjectButton.setToolTipText("Unassign selected subject(s)");
        // --- End Subject Assignment Init ---

        saveButton = new JButton("Save Changes"); // Updated text
        cancelButton = new JButton("Cancel");
    }

    // Load all subjects into the "Available" list initially
    // We will remove the already assigned ones in loadTeacherData
    private void loadAvailableSubjects() {
        availableSubjectsModel.clear();
        try {
            allSubjects = DataStorage.getAllSubjects();
            allSubjects.sort(Comparator.comparing(Subject::getCode));
            for (Subject subject : allSubjects) {
                availableSubjectsModel.addElement(subject); // Add all initially
            }
        } catch (Exception e) {
            showError("Subject Load Error", "Could not load subjects for assignment: " + e.getMessage());
        }
    }

    // Load existing data for the teacher being edited
    private void loadTeacherData() {
        Optional<Teacher> teacherOptional = DataStorage.getTeacherById(this.teacherIdToEdit);
        if (teacherOptional.isPresent()) {
            Teacher teacher = teacherOptional.get();

            // Populate basic info fields
            idField.setText(teacher.getId());
            idField.setEditable(false); // ID cannot be edited
            idField.setBackground(Color.LIGHT_GRAY);
            firstNameField.setText(teacher.getFirstName());
            lastNameField.setText(teacher.getLastName());
            emailField.setText(teacher.getEmail());
            departmentField.setText(teacher.getDepartment());
            positionField.setText(teacher.getPosition());

            // Populate the assigned subjects list and adjust available list
            assignedSubjectsModel.clear(); // Clear just in case
            List<String> currentAssignedCodes = teacher.getAssignedSubjectCodes();

            if (currentAssignedCodes != null) {
                List<Subject> currentlyAssignedSubjects = new ArrayList<>();
                for (String code : currentAssignedCodes) {
                    // Find the Subject object from our cached 'allSubjects' list
                    Optional<Subject> subjectOpt = allSubjects.stream()
                            .filter(s -> s.getCode().equalsIgnoreCase(code))
                            .findFirst();
                    if (subjectOpt.isPresent()) {
                        Subject assignedSub = subjectOpt.get();
                        currentlyAssignedSubjects.add(assignedSub);
                        // Remove from available model if present
                        availableSubjectsModel.removeElement(assignedSub);
                    } else {
                        System.err.println("Warning: Assigned subject code '" + code + "' for teacher '" + teacherIdToEdit + "' not found in all subjects list.");
                    }
                }
                // Sort and add to the assigned model
                currentlyAssignedSubjects.sort(Comparator.comparing(Subject::getCode));
                for(Subject s : currentlyAssignedSubjects) {
                    assignedSubjectsModel.addElement(s);
                }
            }

        } else {
            showError("Load Error", "Could not find teacher with ID: " + this.teacherIdToEdit);
            saveButton.setEnabled(false); // Disable save if data couldn't load
        }
    }


    private void layoutComponents() {
        // --- Top Panel for Basic Teacher Info ---
        // (Identical layout to AddTeacherDialog's formPanel)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Teacher Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: ID & First Name
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Teacher ID (Cannot Edit):"), gbc); // Label updated
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(idField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(firstNameField, gbc);
        gbc.weightx = 0;

        // Row 1: Last Name & Email
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(lastNameField, gbc);
        gbc.gridx = 2; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(emailField, gbc);

        // Row 2: Department & Position
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(departmentField, gbc);
        gbc.gridx = 2; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Position:"), gbc);
        gbc.gridx = 3; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(positionField, gbc);


        // --- Center Panel for Subject Assignment ---
        // (Identical layout to AddTeacherDialog's subjectAssignPanel)
        JPanel subjectAssignPanel = new JPanel(new GridBagLayout());
        subjectAssignPanel.setBorder(BorderFactory.createTitledBorder("Assign Subjects"));
        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.insets = new Insets(5, 5, 5, 5);
        pgbc.fill = GridBagConstraints.BOTH;
        pgbc.weightx = 1.0;
        pgbc.weighty = 1.0;

        pgbc.gridx = 0; pgbc.gridy = 0; pgbc.gridheight = 2;
        subjectAssignPanel.add(new JScrollPane(availableSubjectsList), pgbc);
        pgbc.gridheight = 1;

        pgbc.gridx = 1; pgbc.gridy = 0; pgbc.fill = GridBagConstraints.NONE;
        pgbc.weightx = 0; pgbc.weighty = 0;
        pgbc.anchor = GridBagConstraints.CENTER;
        subjectAssignPanel.add(addSubjectButton, pgbc);

        pgbc.gridx = 1; pgbc.gridy = 1;
        subjectAssignPanel.add(removeSubjectButton, pgbc);

        pgbc.gridx = 2; pgbc.gridy = 0; pgbc.gridheight = 2;
        pgbc.fill = GridBagConstraints.BOTH;
        pgbc.weightx = 1.0; pgbc.weighty = 1.0;
        subjectAssignPanel.add(new JScrollPane(assignedSubjectsList), pgbc);


        // --- Bottom Panel for Dialog Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Add Panels to Dialog ---
        setLayout(new BorderLayout(10, 10));
        add(formPanel, BorderLayout.NORTH);
        add(subjectAssignPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        // Move selected subjects from Available to Assigned
        addSubjectButton.addActionListener(e -> {
            List<Subject> selected = availableSubjectsList.getSelectedValuesList();
            for (Subject subject : selected) {
                assignedSubjectsModel.addElement(subject);
                availableSubjectsModel.removeElement(subject); // Remove from available
            }
            sortListModel(assignedSubjectsModel); // Keep assigned sorted
        });

        // Move selected subjects from Assigned back to Available
        removeSubjectButton.addActionListener(e -> {
            List<Subject> selected = assignedSubjectsList.getSelectedValuesList();
            for (Subject subject : selected) {
                assignedSubjectsModel.removeElement(subject);
                availableSubjectsModel.addElement(subject); // Add back to available
            }
            sortListModel(availableSubjectsModel); // Keep available sorted
        });

        saveButton.addActionListener(e -> performSave());
        cancelButton.addActionListener(e -> dispose());
    }

    // Helper to sort a DefaultListModel based on Subject code
    private void sortListModel(DefaultListModel<Subject> model) {
        List<Subject> list = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            list.add(model.getElementAt(i));
        }
        list.sort(Comparator.comparing(Subject::getCode));
        model.clear();
        for (Subject s : list) {
            model.addElement(s);
        }
    }

    private void performSave() {
        // 1. Get basic info (ID comes from teacherIdToEdit)
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String department = departmentField.getText().trim();
        String position = positionField.getText().trim();

        // 2. Get FINAL assigned subject codes from the list model
        List<String> assignedSubjectCodes = new ArrayList<>();
        for (int i = 0; i < assignedSubjectsModel.getSize(); i++) {
            assignedSubjectCodes.add(assignedSubjectsModel.getElementAt(i).getCode());
        }

        // 3. Validate basic info
        if (!validateInput(firstName, lastName, email, department, position)) {
            return;
        }

        // 4. Create Teacher object with ORIGINAL ID and updated details/subjects
        Teacher updatedTeacher = new Teacher(this.teacherIdToEdit, firstName, lastName, email, department, position, assignedSubjectCodes);

        // 5. Save using DataStorage (saveTeacher handles updates)
        try {
            DataStorage.saveTeacher(updatedTeacher);
            saved = true;
            JOptionPane.showMessageDialog(this, "Teacher details updated successfully!", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close dialog

        } catch (Exception ex) {
            showError("Save Error", "An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Validation for editing (ID is not validated)
    private boolean validateInput(String firstName, String lastName, String email, String department, String position) {
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || department.isEmpty() || position.isEmpty()) {
            showError("Validation Error", "First Name, Last Name, Email, Department, and Position are required.");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Validation Error", "Please enter a valid email address.");
            return false;
        }
        return true;
    }

    // Helper for errors
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Check if saved
    public boolean isSaved() {
        return saved;
    }
}