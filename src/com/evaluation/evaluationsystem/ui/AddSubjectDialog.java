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
import java.util.stream.Collectors;

public class AddSubjectDialog extends JDialog {

    // --- Input Fields ---
    private JTextField codeField;
    private JTextField nameField;
    private JTextField unitsField; // Use JTextField for units, validate as integer later
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
    private boolean saved = false;
    private List<Subject> allSubjects; // Cache all subjects for prerequisite selection

    public AddSubjectDialog(Frame parent) {
        super(parent, "Add New Subject", true); // Modal
        // Increased size to accommodate prerequisite lists
        setSize(650, 550);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        loadAvailableSubjects(); // Load subjects for prerequisite list
        layoutComponents();
        addListeners();
    }

    private void initComponents() {
        codeField = new JTextField(10);
        nameField = new JTextField(20);
        unitsField = new JTextField(5);
        departmentField = new JTextField(15);

        String[] yearLevels = {"1st Year", "2nd Year", "3rd Year", "4th Year", "Summer"}; // Added Summer
        yearLevelComboBox = new JComboBox<>(yearLevels);

        String[] semesters = {"1st Semester", "2nd Semester", "Summer"}; // Added Summer
        semesterComboBox = new JComboBox<>(semesters);

        // Prerequisite List Setup
        availableSubjectsModel = new DefaultListModel<>();
        availableSubjectsList = new JList<>(availableSubjectsModel);
        availableSubjectsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        availableSubjectsList.setVisibleRowCount(8); // Show 8 items at a time

        selectedPrereqsModel = new DefaultListModel<>();
        selectedPrereqsList = new JList<>(selectedPrereqsModel);
        selectedPrereqsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        selectedPrereqsList.setVisibleRowCount(8);

        addPrereqButton = new JButton(">>");
        addPrereqButton.setToolTipText("Add selected subject(s) as prerequisite(s)");
        removePrereqButton = new JButton("<<");
        removePrereqButton.setToolTipText("Remove selected prerequisite(s)");

        saveButton = new JButton("Save Subject");
        cancelButton = new JButton("Cancel");
    }

    // Load subjects into the "Available" list for prerequisite selection
    private void loadAvailableSubjects() {
        availableSubjectsModel.clear(); // Clear previous items
        try {
            allSubjects = DataStorage.getAllSubjects();
            // Sort for better usability
            allSubjects.sort(Comparator.comparing(Subject::getCode));
            for (Subject subject : allSubjects) {
                availableSubjectsModel.addElement(subject); // Add Subject objects directly
            }
        } catch (Exception e) {
            showError("Error Loading Subjects", "Could not load subjects for prerequisite selection: " + e.getMessage());
        }
    }

    private void layoutComponents() {
        // --- Top Panel for Basic Subject Info ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Subject Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        // Code & Name (Row 0)
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Code:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; formPanel.add(codeField, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; formPanel.add(nameField, gbc);
        gbc.weightx = 0; // Reset weight

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
        pgbc.fill = GridBagConstraints.BOTH; // Make lists fill space
        pgbc.weightx = 1.0;
        pgbc.weighty = 1.0;

        // Available List (Left)
        pgbc.gridx = 0; pgbc.gridy = 0; pgbc.gridheight = 2; // Span 2 rows
        prereqPanel.add(new JScrollPane(availableSubjectsList), pgbc);
        pgbc.gridheight = 1; // Reset grid height

        // Add Button (Center Top)
        pgbc.gridx = 1; pgbc.gridy = 0; pgbc.fill = GridBagConstraints.NONE; // No fill
        pgbc.weightx = 0; pgbc.weighty = 0; // No weight
        pgbc.anchor = GridBagConstraints.CENTER;
        prereqPanel.add(addPrereqButton, pgbc);

        // Remove Button (Center Bottom)
        pgbc.gridx = 1; pgbc.gridy = 1;
        prereqPanel.add(removePrereqButton, pgbc);

        // Selected List (Right)
        pgbc.gridx = 2; pgbc.gridy = 0; pgbc.gridheight = 2; // Span 2 rows
        pgbc.fill = GridBagConstraints.BOTH; // Fill space
        pgbc.weightx = 1.0; pgbc.weighty = 1.0; // Add weight
        prereqPanel.add(new JScrollPane(selectedPrereqsList), pgbc);

        // --- Bottom Panel for Dialog Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Add Panels to Dialog ---
        setLayout(new BorderLayout(10, 10)); // Add gaps
        add(formPanel, BorderLayout.NORTH);
        add(prereqPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void addListeners() {
        // Move selected subjects from Available to Selected Prerequisites
        addPrereqButton.addActionListener(e -> {
            List<Subject> selected = availableSubjectsList.getSelectedValuesList();
            for (Subject subject : selected) {
                if (!selectedPrereqsModel.contains(subject)) { // Avoid duplicates
                    selectedPrereqsModel.addElement(subject);
                }
                // Optionally remove from available list upon adding
                // availableSubjectsModel.removeElement(subject);
            }
            // Sort selected list after adding
            sortListModel(selectedPrereqsModel);
        });

        // Move selected subjects from Selected Prerequisites back to Available
        removePrereqButton.addActionListener(e -> {
            List<Subject> selected = selectedPrereqsList.getSelectedValuesList();
            for (Subject subject : selected) {
                selectedPrereqsModel.removeElement(subject);
                // Optionally add back to available list if it was removed
                // if (!availableSubjectsModel.contains(subject)) {
                //     availableSubjectsModel.addElement(subject);
                //     sortListModel(availableSubjectsModel); // Keep available sorted
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
        // 1. Get basic info
        String code = codeField.getText().trim().toUpperCase(); // Standardize code case
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
            if (units <= 0) throw new NumberFormatException(); // Units must be positive
        } catch (NumberFormatException ex) {
            showError("Validation Error", "Units must be a positive whole number.");
            return;
        }

        if (!validateInput(code, name, department)) {
            return; // Stop if basic validation fails
        }

        // 4. Create Subject object
        Subject newSubject = new Subject(code, name, units, department, yearLevel, semester, prerequisiteCodes);

        // 5. Save using DataStorage
        try {
            DataStorage.saveSubject(newSubject);
            saved = true;
            JOptionPane.showMessageDialog(this, "Subject added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close dialog
        } catch (IllegalArgumentException ex) {
            showError("Save Error", ex.getMessage());
        } catch (Exception ex) {
            showError("Save Error", "An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private boolean validateInput(String code, String name, String department) {
        if (code.isEmpty() || name.isEmpty() || department.isEmpty()) {
            showError("Validation Error", "Subject Code, Name, and Department are required.");
            return false;
        }
        // Check if Subject Code already exists
        if (DataStorage.getSubjectByCode(code).isPresent()) {
            showError("Validation Error", "Subject Code '" + code + "' already exists.");
            return false;
        }
        // Add more specific code format validation if needed
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