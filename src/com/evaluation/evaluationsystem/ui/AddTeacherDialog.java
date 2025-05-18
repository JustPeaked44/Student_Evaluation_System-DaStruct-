package com.evaluation.evaluationsystem.ui; // Corrected package

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.Subject;
import com.evaluation.evaluationsystem.models.Teacher;
import com.evaluation.evaluationsystem.models.User;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AddTeacherDialog extends JDialog {

    // --- Input Fields ---
    private JTextField idField;
    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField departmentField;
    private JTextField positionField;

    // --- Subject Assignment Components ---
    private JList<Subject> availableSubjectsList;
    private JList<Subject> assignedSubjectsList;
    private DefaultListModel<Subject> availableSubjectsModel;
    private DefaultListModel<Subject> assignedSubjectsModel;
    private JButton addSubjectButton;
    private JButton removeSubjectButton;
    private List<Subject> allSubjects;

    // --- Buttons ---
    private JButton saveButton;
    private JButton cancelButton;

    // --- State ---
    private boolean saved = false;

    // --- Validation ---
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    public AddTeacherDialog(Frame parent) {
        super(parent, "Add New Teacher", true);
        setSize(650, 550); // Adjusted size for lists
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        loadAvailableSubjects();
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

        // Initialize Subject Assignment Lists
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

        saveButton = new JButton("Save Teacher");
        cancelButton = new JButton("Cancel");
    }

    private void loadAvailableSubjects() {
        availableSubjectsModel.clear();
        try {
            allSubjects = DataStorage.getAllSubjects();
            allSubjects.sort(Comparator.comparing(Subject::getCode));
            for (Subject subject : allSubjects) {
                availableSubjectsModel.addElement(subject);
            }
        } catch (Exception e) {
            showError("Subject Load Error", "Could not load subjects for assignment: " + e.getMessage());
        }
    }

    private void layoutComponents() {
        // Top Panel for Basic Teacher Info
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Teacher Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Layout fields (ID, First Name, Last Name, Email, Department, Position)
        // Row 0: ID & First Name
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Teacher ID:"), gbc);
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


        // Center Panel for Subject Assignment
        JPanel subjectAssignPanel = new JPanel(new GridBagLayout());
        subjectAssignPanel.setBorder(BorderFactory.createTitledBorder("Assign Subjects"));
        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.insets = new Insets(5, 5, 5, 5);
        pgbc.fill = GridBagConstraints.BOTH;
        pgbc.weightx = 1.0; pgbc.weighty = 1.0;

        // Layout dual lists and buttons (Available, >>, <<, Assigned)
        pgbc.gridx = 0; pgbc.gridy = 0; pgbc.gridheight = 2;
        subjectAssignPanel.add(new JScrollPane(availableSubjectsList), pgbc);
        pgbc.gridheight = 1;
        pgbc.gridx = 1; pgbc.gridy = 0; pgbc.fill = GridBagConstraints.NONE;
        pgbc.weightx = 0; pgbc.weighty = 0; pgbc.anchor = GridBagConstraints.CENTER;
        subjectAssignPanel.add(addSubjectButton, pgbc);
        pgbc.gridx = 1; pgbc.gridy = 1;
        subjectAssignPanel.add(removeSubjectButton, pgbc);
        pgbc.gridx = 2; pgbc.gridy = 0; pgbc.gridheight = 2;
        pgbc.fill = GridBagConstraints.BOTH; pgbc.weightx = 1.0; pgbc.weighty = 1.0;
        subjectAssignPanel.add(new JScrollPane(assignedSubjectsList), pgbc);


        // Bottom Panel for Dialog Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Add Panels to Dialog
        setLayout(new BorderLayout(10, 10));
        add(formPanel, BorderLayout.NORTH);
        add(subjectAssignPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        // Move selected subjects from Available to Assigned
        addSubjectButton.addActionListener(e -> moveSubjects(availableSubjectsList, availableSubjectsModel, assignedSubjectsModel));
        // Move selected subjects from Assigned back to Available
        removeSubjectButton.addActionListener(e -> moveSubjects(assignedSubjectsList, assignedSubjectsModel, availableSubjectsModel));

        saveButton.addActionListener(e -> performSave());
        cancelButton.addActionListener(e -> dispose());
    }

    // Helper method to move subjects between list models
    private void moveSubjects(JList<Subject> sourceList, DefaultListModel<Subject> sourceModel, DefaultListModel<Subject> destModel) {
        List<Subject> selected = sourceList.getSelectedValuesList();
        for (Subject subject : selected) {
            if (!destModel.contains(subject)) { // Avoid duplicates if necessary
                destModel.addElement(subject);
            }
            sourceModel.removeElement(subject); // Remove from source
        }
        sortListModel(destModel); // Keep destination sorted
        sortListModel(sourceModel); // Keep source sorted
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
        String id = idField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String department = departmentField.getText().trim();
        String position = positionField.getText().trim();

        // Get assigned subject codes from the assignedSubjectsModel
        List<String> assignedSubjectCodes = new ArrayList<>();
        for (int i = 0; i < assignedSubjectsModel.getSize(); i++) {
            assignedSubjectCodes.add(assignedSubjectsModel.getElementAt(i).getCode());
        }

        if (!validateInput(id, firstName, lastName, email, department, position)) {
            return;
        }

        // Create Teacher object with the LIST of codes
        Teacher newTeacher = new Teacher(id, firstName, lastName, email, department, position, assignedSubjectCodes); // Pass the list

        String password = generatePassword(id);
        User newUser = new User(id, password, "Teacher");

        try {
            DataStorage.saveTeacher(newTeacher); // saveTeacher should handle the list
            DataStorage.addUser(newUser);

            JOptionPane.showMessageDialog(this,
                    "Teacher added successfully!\n\nUsername (ID): " + id + "\nPassword: " + password,
                    "Teacher Added", JOptionPane.INFORMATION_MESSAGE);

            saved = true;
            dispose();

        } catch (IllegalArgumentException ex) {
            showError("Save Error", ex.getMessage());
        } catch (Exception ex) {
            showError("Save Error", "An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Validation (same as before)
    private boolean validateInput(String id, String firstName, String lastName, String email, String department, String position) {
        if (id.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || department.isEmpty() || position.isEmpty()) {
            showError("Validation Error", "All teacher detail fields are required.");
            return false;
        }
        if (!ID_PATTERN.matcher(id).matches()) {
            showError("Validation Error", "Teacher ID can only contain letters and numbers.");
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Validation Error", "Please enter a valid email address.");
            return false;
        }
        if (DataStorage.getTeacherById(id).isPresent()) {
            showError("Validation Error", "Teacher ID '" + id + "' already exists.");
            return false;
        }
        // TODO: Check if username exists in users.json
        return true;
    }

    // Generate password (same as before)
    private String generatePassword(String teacherId) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int suffixLength = 4;
        Random random = new Random();
        StringBuilder suffix = new StringBuilder(suffixLength);
        for (int i = 0; i < suffixLength; i++) {
            suffix.append(characters.charAt(random.nextInt(characters.length())));
        }
        return teacherId + suffix.toString();
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