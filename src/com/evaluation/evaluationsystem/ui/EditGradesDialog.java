package com.evaluation.evaluationsystem.ui; // Adjust package if needed

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.EnrolledSubject;
import com.evaluation.evaluationsystem.models.Enrollment;
import com.evaluation.evaluationsystem.models.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class EditGradesDialog extends JDialog {

    private JTable gradesTable;
    private DefaultTableModel gradesTableModel;
    private JButton saveButton;
    private JButton cancelButton;

    private final String subjectCode; // Subject code to edit grades for
    private List<StudentGradeEntry> gradeEntries; // Store data including original grade

    // Inner class to hold student info and grade for the table model
    private static class StudentGradeEntry {
        String studentId;
        String lastName;
        String firstName;
        double currentGrade; // The grade currently stored
        Double gradeToSave; // The grade entered by the user (null if not edited/invalid)

        StudentGradeEntry(String id, String last, String first, double grade) {
            this.studentId = id;
            this.lastName = last;
            this.firstName = first;
            this.currentGrade = grade;
            this.gradeToSave = null; // Initially null, meaning no change
        }

        // Getters needed by the table model
        public String getStudentId() { return studentId; }
        public String getLastName() { return lastName; }
        public String getFirstName() { return firstName; }
        public Double getCurrentGradeValue() { return currentGrade; } // Raw value

        // Display representation of the current grade
        public String getCurrentGradeDisplay() {
            return currentGrade == 0.0 ? "NG" : String.format("%.1f", currentGrade);
        }

        // Getter/Setter for the editable grade column
        public Double getEditableGrade() {
            // If gradeToSave is set (edited), show that, otherwise show current
            return gradeToSave != null ? gradeToSave : currentGrade;
        }

        public void setEditableGrade(Double newGrade) {
            // Basic validation within the setter
            if (newGrade != null && (newGrade < 1.0 || newGrade > 5.0)) {
                throw new IllegalArgumentException("Grade must be between 1.0 and 5.0");
            }
            // Only mark as changed if the new value is different from current grade
            // and handle the NG (0.0) case correctly
            if (newGrade == null || Math.abs(newGrade - this.currentGrade) > 0.001 || (this.currentGrade == 0.0 && newGrade != 0.0) ) {
                this.gradeToSave = newGrade;
            } else {
                this.gradeToSave = null; // Reset if set back to original value
            }
        }

        public boolean isEdited() {
            return gradeToSave != null;
        }
    }


    public EditGradesDialog(Frame parent, String subjectCode) {
        super(parent, "Enter/Edit Grades for " + subjectCode, true); // Modal dialog
        this.subjectCode = subjectCode;
        this.gradeEntries = new ArrayList<>();

        setSize(600, 450);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        layoutComponents();
        addListeners();

        loadStudentGradeData();
    }

    private void initComponents() {
        // Define table columns
        String[] columnNames = {"Student ID", "Last Name", "First Name", "Current Grade", "New Grade"};

        // Create table model - make "New Grade" column editable
        gradesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only the "New Grade" column (index 4) is editable
            }

            // Override to handle Double type for the editable column
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) {
                    return Double.class; // Treat "New Grade" column as Double
                }
                if (columnIndex == 3) {
                    return String.class; // Current Grade is displayed as String ("NG" or number)
                }
                return String.class; // Other columns are Strings
            }
        };

        gradesTable = new JTable(gradesTableModel);
        gradesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gradesTable.setRowHeight(25); // Increase row height slightly

        // Set custom renderer and editor for the "New Grade" column
        TableColumn newGradeColumn = gradesTable.getColumnModel().getColumn(4);
        newGradeColumn.setCellRenderer(new GradeCellRenderer()); // Custom renderer
        // Use a default editor that handles Doubles, add validation later if needed
        // Or create a custom editor for stricter validation during input

        // Set preferred widths
        gradesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        gradesTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        gradesTable.getColumnModel().getColumn(2).setPreferredWidth(120);
        gradesTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        gradesTable.getColumnModel().getColumn(4).setPreferredWidth(80);


        saveButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel");
    }

    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(gradesTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void addListeners() {
        saveButton.addActionListener(e -> performSave());
        cancelButton.addActionListener(e -> dispose());

        // Add listener to update the internal data model when cell editing stops
        gradesTableModel.addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();

                // Check if the "New Grade" column (index 4) was updated
                // and the row index is valid
                if (column == 4 && row >= 0 && row < gradeEntries.size()) {
                    // Get the new value entered by the user from the table model
                    Object valueFromModel = gradesTableModel.getValueAt(row, column);
                    Double newGrade = null;

                    try {
                        // Attempt to convert the value from the model to a Double
                        if (valueFromModel instanceof Double) {
                            newGrade = (Double) valueFromModel;
                        } else if (valueFromModel instanceof String && !((String) valueFromModel).trim().isEmpty()) {
                            // Try parsing if it's a non-empty string
                            newGrade = Double.parseDouble(((String) valueFromModel).trim());
                        }
                        // Note: If the user clears the cell, valueFromModel might be null or empty string,
                        // resulting in newGrade being null here.

                        // Update the corresponding StudentGradeEntry's internal state
                        // The setEditableGrade method already handles validation (1.0-5.0)
                        gradeEntries.get(row).setEditableGrade(newGrade);

                        // --- REMOVED problematic lines ---
                        // gradesTableModel.fireTableCellUpdated(row, column); // REMOVE THIS
                        // ---

                    } catch (NumberFormatException nfe) {
                        // Error parsing the input (e.g., user typed text)
                        // Show error and revert the *visual* cell in the table model
                        // back to what it should be based on the *current* internal state.
                        showError("Invalid Input", "Please enter a valid number for the grade (1.0-5.0).");
                        // Use invokeLater to avoid modifying model during event dispatch
                        SwingUtilities.invokeLater(() -> {
                            gradesTableModel.setValueAt(gradeEntries.get(row).getEditableGrade(), row, column);
                        });
                    } catch (IllegalArgumentException iae) {
                        // Error from setEditableGrade validation (e.g., out of range)
                        showError("Invalid Grade", iae.getMessage());
                        // Revert the visual cell
                        SwingUtilities.invokeLater(() -> {
                            gradesTableModel.setValueAt(gradeEntries.get(row).getEditableGrade(), row, column);
                        });
                    }
                }
            }
        });
    }

    // Load students enrolled in the specific subject
    private void loadStudentGradeData() {
        gradesTableModel.setRowCount(0); // Clear table
        gradeEntries.clear(); // Clear internal list

        try {
            // Fetch all students and all enrollments (can be optimized in DataStorage)
            List<Student> allStudents = DataStorage.getAllStudents();
            List<Enrollment> allEnrollments = DataStorage.getAllEnrollments();

            List<StudentGradeEntry> tempEntries = new ArrayList<>();

            for (Student student : allStudents) {
                for (Enrollment enrollment : allEnrollments) {
                    if (enrollment.getStudentId().equals(student.getId())) {
                        for (EnrolledSubject enrolledSub : enrollment.getSubjects()) {
                            if (enrolledSub.getCode().equals(this.subjectCode)) {
                                // Create an entry for the internal list
                                tempEntries.add(new StudentGradeEntry(
                                        student.getId(),
                                        student.getLastName(),
                                        student.getFirstName(),
                                        enrolledSub.getGrade() // Use the grade from storage
                                ));
                                break; // Found subject for this student's enrollment
                            }
                        }
                    }
                }
            }

            // Sort entries by student last name (optional)
            tempEntries.sort(Comparator.comparing(StudentGradeEntry::getLastName));

            // Populate internal list and table model
            for (StudentGradeEntry entry : tempEntries) {
                gradeEntries.add(entry);
                gradesTableModel.addRow(new Object[]{
                        entry.getStudentId(),
                        entry.getLastName(),
                        entry.getFirstName(),
                        entry.getCurrentGradeDisplay(), // Display "NG" or formatted grade
                        entry.getEditableGrade() // Initial value for editable column
                });
            }

        } catch (Exception e) {
            showError("Data Loading Error", "Error loading student grades: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Save the modified grades
    private void performSave() {
        // Stop cell editing if currently active
        if (gradesTable.isEditing()) {
            gradesTable.getCellEditor().stopCellEditing();
        }

        int changesMade = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < gradeEntries.size(); i++) {
            StudentGradeEntry entry = gradeEntries.get(i);
            if (entry.isEdited()) { // Check if gradeToSave is not null
                Double gradeToSave = entry.gradeToSave; // The validated grade to save

                // Double-check validation (although setter should handle it)
                if (gradeToSave == null || gradeToSave < 1.0 || gradeToSave > 5.0) {
                    errors.add("Invalid grade entered for student " + entry.getStudentId() + ". Must be 1.0-5.0.");
                    continue; // Skip saving this invalid entry
                }

                try {
                    // Call DataStorage to update the grade in the JSON file
                    DataStorage.updateSubjectGrade(entry.getStudentId(), this.subjectCode, gradeToSave);
                    entry.currentGrade = gradeToSave; // Update the 'currentGrade' in our local model
                    entry.gradeToSave = null; // Reset the edited state
                    // Update the "Current Grade" display column in the table model
                    gradesTableModel.setValueAt(entry.getCurrentGradeDisplay(), i, 3);
                    changesMade++;
                } catch (Exception ex) {
                    errors.add("Error saving grade for student " + entry.getStudentId() + ": " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }

        gradesTable.repaint(); // Repaint table to potentially clear edit highlights

        // --- Provide Feedback ---
        if (!errors.isEmpty()) {
            // Show combined error messages
            StringBuilder errorMsg = new StringBuilder("Errors occurred during save:\n");
            for (String error : errors) {
                errorMsg.append("- ").append(error).append("\n");
            }
            if (changesMade > 0) {
                errorMsg.append("\n").append(changesMade).append(" grade(s) were saved successfully.");
            }
            showError("Save Errors", errorMsg.toString());
        } else if (changesMade > 0) {
            JOptionPane.showMessageDialog(this, changesMade + " grade(s) saved successfully!", "Save Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close dialog only if save was fully successful without errors
        } else {
            JOptionPane.showMessageDialog(this, "No changes were made or saved.", "No Changes", JOptionPane.INFORMATION_MESSAGE);
            // Optionally close here too, or leave open
            dispose();
        }
    }

    // Helper to show error messages
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Custom renderer to highlight edited cells and format NG
    private class GradeCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Reset background/foreground
            c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
            setHorizontalAlignment(JLabel.RIGHT); // Right-align grades

            if (row >= 0 && row < gradeEntries.size()) {
                StudentGradeEntry entry = gradeEntries.get(row);
                // Highlight if edited
                if (entry.isEdited()) {
                    c.setBackground(new Color(255, 255, 224)); // Light yellow background for edited
                }

                // Format display value (handle null for initial state)
                Double gradeValue = (value instanceof Double) ? (Double) value : null;
                if (gradeValue != null) {
                    setText(String.format("%.1f", gradeValue));
                } else if (entry.currentGrade == 0.0) {
                    // If original was NG and no new grade entered yet, show NG
                    setText("NG");
                    c.setForeground(Color.GRAY);
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else {
                    // If original had a grade and user cleared input, show original
                    setText(String.format("%.1f", entry.currentGrade));
                }

            } else {
                // Default rendering if row index is invalid
                setText(value == null ? "" : value.toString());
            }

            return c;
        }
    }
}