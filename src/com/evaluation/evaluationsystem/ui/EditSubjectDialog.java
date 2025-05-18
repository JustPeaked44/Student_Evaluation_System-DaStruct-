package com.evaluation.evaluationsystem.ui; // Adjust package if needed

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.Subject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EditSubjectDialog extends JDialog {

    // --- Input Fields ---
    private JTextField codeField;
    private JTextField nameField;
    private JTextField unitsField;
    private JTextField departmentField;
    private JComboBox<String> yearLevelComboBox;
    private JComboBox<String> semesterComboBox;

    // --- Prerequisite Selection ---
    private JList<Subject> availableSubjectsList;
    private JList<Subject> selectedPrereqsList;
    private DefaultListModel<Subject> availableSubjectsModel;
    private DefaultListModel<Subject> selectedPrereqsModel;
    private JButton addPrereqButton;
    private JButton removePrereqButton;

    // --- Dialog Buttons ---
    private JButton saveButton;
    private JButton cancelButton;

    // --- State ---
    private final String subjectCodeToEdit; // Store the code of the subject being edited
    private boolean saved = false;
    private List<Subject> allSubjects; // Cache all subjects

    // Constructor accepts the subject code to edit
    public EditSubjectDialog(Frame parent, String subjectCodeToEdit) {
        super(parent, "Edit Subject Details", true); // Updated title
        this.subjectCodeToEdit = subjectCodeToEdit;

        setSize(650, 550);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        loadAvailableSubjects(); // Load subjects for prerequisite list FIRST
        loadSubjectData(); // Load existing data AFTER components are ready
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        codeField = new JTextField(10);
        nameField = new JTextField(20);
        unitsField = new JTextField(5);
        departmentField = new JTextField(15);

        String[] yearLevels = {"1st Year", "2nd Year", "3rd Year", "4th Year", "Summer"};
        yearLevelComboBox = new JComboBox<>(yearLevels);

        String[] semesters = {"1st Semester", "2nd Semester", "Summer"};
        semesterComboBox = new JComboBox<>(semesters);

        // Prerequisite List Setup
        availableSubjectsModel = new DefaultListModel<>();
        availableSubjectsList = new JList<>(availableSubjectsModel);
        availableSubjectsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        availableSubjectsList.setVisibleRowCount(8);

        selectedPrereqsModel = new DefaultListModel<>();
        selectedPrereqsList = new JList<>(selectedPrereqsModel);
        selectedPrereqsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        selectedPrereqsList.setVisibleRowCount(8);

        addPrereqButton = new JButton(">>");
        addPrereqButton.setToolTipText("Add selected subject(s) as prerequisite(s)");
        removePrereqButton = new JButton("<<");
        removePrereqButton.setToolTipText("Remove selected prerequisite(s)");

        saveButton = new JButton("Save Changes"); // Updated text
        cancelButton = new JButton("Cancel");
    }

    // Load all subjects into the "Available" list, excluding the subject being edited
    private void loadAvailableSubjects() {
        availableSubjectsModel.clear();
        try {
            allSubjects = DataStorage.getAllSubjects();
            allSubjects.sort(Comparator.comparing(Subject::getCode));
            for (Subject subject : allSubjects) {
                // Don't allow a subject to be its own prerequisite
                if (!subject.getCode().equalsIgnoreCase(this.subjectCodeToEdit)) {
                    availableSubjectsModel.addElement(subject);
                }
            }
        } catch (Exception e) {
            showError("Error Loading Subjects", "Could not load subjects for prerequisite selection: " + e.getMessage());
        }
    }

    // Load existing data for the subject being edited
    private void loadSubjectData() {
        Optional<Subject> subjectOptional = DataStorage.getSubjectByCode(this.subjectCodeToEdit);
        if (subjectOptional.isPresent()) {
            Subject subject = subjectOptional.get();
            codeField.setText(subject.getCode());
            nameField.setText(subject.getName());
            unitsField.setText(String.valueOf(subject.getUnits())); // Convert int to String
            departmentField.setText(subject.getDepartment());
            yearLevelComboBox.setSelectedItem(subject.getYearLevel());
            semesterComboBox.setSelectedItem(subject.getSemester());

            // Populate the selected prerequisites list
            selectedPrereqsModel.clear();
            List<String> currentPrereqCodes = subject.getPrerequisites();
            if (currentPrereqCodes != null) {
                for (String prereqCode : currentPrereqCodes) {
                    // Find the corresponding Subject object from allSubjects
                    Optional<Subject> prereqOpt = allSubjects.stream()
                            .filter(s -> s.getCode().equalsIgnoreCase(prereqCode))
                            .findFirst();
                    prereqOpt.ifPresent(prereq -> selectedPrereqsModel.addElement(prereq));
                }
                sortListModel(selectedPrereqsModel); // Sort initially loaded prerequisites
            }

        } else {
            showError("Load Error", "Could not find subject with code: " + this.subjectCodeToEdit);
            saveButton.setEnabled(false); // Disable save if data couldn't load
        }
    }


    private void layoutComponents() {
        // --- Top Panel for Basic Subject Info ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Subject Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Code (Non-Editable) & Name (Row 0)
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Code (Cannot Edit):"), gbc); // Label updated
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        codeField.setEditable(false); // Code is not editable
        codeField.setBackground(Color.LIGHT_GRAY);
        formPanel.add(codeField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(nameField, gbc);
        gbc.weightx = 0;

        // Units & Department (Row 1)
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Units:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(unitsField, gbc);
        gbc.gridx = 2; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(departmentField, gbc);

        // Year & Semester (Row 2)
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Year Level:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(yearLevelComboBox, gbc);
        gbc.gridx = 2; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 3; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(semesterComboBox, gbc);

        // --- Center Panel for Prerequisite Selection ---
        JPanel prereqPanel = new JPanel(new GridBagLayout());
        prereqPanel.setBorder(BorderFactory.createTitledBorder("Select Prerequisites"));
        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.insets = new Insets(5, 5, 5, 5);
        pgbc.fill = GridBagConstraints.BOTH;
        pgbc.weightx = 1.0;
        pgbc.weighty = 1.0;

        // Available List (Left)
        pgbc.gridx = 0; pgbc.gridy = 0; pgbc.gridheight = 2;
        prereqPanel.add(new JScrollPane(availableSubjectsList), pgbc);
        pgbc.gridheight = 1;

        // Add Button (Center Top)
        pgbc.gridx = 1; pgbc.gridy = 0; pgbc.fill = GridBagConstraints.NONE;
        pgbc.weightx = 0; pgbc.weighty = 0;
        pgbc.anchor = GridBagConstraints.CENTER;
        prereqPanel.add(addPrereqButton, pgbc);

        // Remove Button (Center Bottom)
        pgbc.gridx = 1; pgbc.gridy = 1;
        prereqPanel.add(removePrereqButton, pgbc);

        // Selected List (Right)
        pgbc.gridx = 2; pgbc.gridy = 0; pgbc.gridheight = 2;
        pgbc.fill = GridBagConstraints.BOTH;
        pgbc.weightx = 1.0; pgbc.weighty = 1.0;
        prereqPanel.add(new JScrollPane(selectedPrereqsList), pgbc);

        // --- Bottom Panel for Dialog Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Add Panels to Dialog ---
        setLayout(new BorderLayout(10, 10));
        add(formPanel, BorderLayout.NORTH);
        add(prereqPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        // Move selected subjects from Available to Selected Prerequisites
        addPrereqButton.addActionListener(e -> {
            List<Subject> selected = availableSubjectsList.getSelectedValuesList();
            for (Subject subject : selected) {
                if (!selectedPrereqsModel.contains(subject)) {
                    selectedPrereqsModel.addElement(subject);
                }
                // availableSubjectsModel.removeElement(subject); // Optional removal
            }
            sortListModel(selectedPrereqsModel); // Sort after adding
        });

        // Move selected subjects from Selected Prerequisites back to Available
        removePrereqButton.addActionListener(e -> {
            List<Subject> selected = selectedPrereqsList.getSelectedValuesList();
            for (Subject subject : selected) {
                selectedPrereqsModel.removeElement(subject);
                // Optional: Add back to available list if it was removed
                // if (!availableSubjectsModel.contains(subject)) {
                //     availableSubjectsModel.addElement(subject);
                //     sortListModel(availableSubjectsModel);
                // }
            }
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
        // 1. Get basic info (Code comes from subjectCodeToEdit)
        String name = nameField.getText().trim();
        String unitsStr = unitsField.getText().trim();
        String department = departmentField.getText().trim();
        String yearLevel = (String) yearLevelComboBox.getSelectedItem();
        String semester = (String) semesterComboBox.getSelectedItem();

        // 2. Get selected prerequisite codes
        List<String> prerequisiteCodes = new ArrayList<>();
        for (int i = 0; i < selectedPrereqsModel.getSize(); i++) {
            prerequisiteCodes.add(selectedPrereqsModel.getElementAt(i).getCode());
        }

        // 3. Validate Input
        int units;
        try {
            units = Integer.parseInt(unitsStr);
            if (units <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Validation Error", "Units must be a positive whole number.");
            return;
        }

        if (!validateInput(name, department)) { // No need to validate code uniqueness
            return;
        }

        // 4. Create Subject object with ORIGINAL code and updated details
        Subject updatedSubject = new Subject(this.subjectCodeToEdit, name, units, department, yearLevel, semester, prerequisiteCodes);

        // 5. Save using DataStorage (saveSubject handles updates)
        try {
            DataStorage.saveSubject(updatedSubject);
            saved = true;
            JOptionPane.showMessageDialog(this, "Subject updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close dialog
        } catch (Exception ex) {
            showError("Save Error", "An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Validation for editing (Code is not validated for uniqueness)
    private boolean validateInput(String name, String department) {
        if (name.isEmpty() || department.isEmpty()) {
            showError("Validation Error", "Subject Name and Department are required.");
            return false;
        }
        return true;
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