package com.evaluation.evaluationsystem.ui; // Adjust package if needed

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.Subject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

public class AdminSubjectPanel extends JPanel {

    private JTable subjectTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private TableRowSorter<DefaultTableModel> sorter;

    public AdminSubjectPanel() {
        setLayout(new BorderLayout(10, 10));
        initComponents();
        layoutComponents();
        addListeners();
        loadSubjectData();
    }

    private void initComponents() {
        // Define table columns for subjects
        String[] columnNames = {"Code", "Name", "Units", "Department", "Year Level", "Semester", "Prerequisites"};

        // Create a non-editable table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Cells not directly editable
            }
        };

        subjectTable = new JTable(tableModel);
        subjectTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Enable sorting
        sorter = new TableRowSorter<>(tableModel);
        subjectTable.setRowSorter(sorter);

        // Adjust column widths (optional, but helpful for prerequisites)
        subjectTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Code
        subjectTable.getColumnModel().getColumn(1).setPreferredWidth(250); // Name
        subjectTable.getColumnModel().getColumn(2).setPreferredWidth(50);  // Units
        subjectTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Department
        subjectTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Year
        subjectTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Semester
        subjectTable.getColumnModel().getColumn(6).setPreferredWidth(150); // Prerequisites

        // Initialize buttons
        addButton = new JButton("Add Subject");
        editButton = new JButton("Edit Selected");
        deleteButton = new JButton("Delete Selected");
        refreshButton = new JButton("Refresh List");
    }

    private void layoutComponents() {
        // Table with Scroll Pane in the Center
        JScrollPane scrollPane = new JScrollPane(subjectTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel at the Top
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH);
    }

    private void addListeners() {
        refreshButton.addActionListener(e -> loadSubjectData());
        addButton.addActionListener(e -> handleAddSubject());
        editButton.addActionListener(e -> handleEditSubject());
        deleteButton.addActionListener(e -> handleDeleteSubject());
    }

    // Method to load or refresh subject data in the table
    public void loadSubjectData() {
        tableModel.setRowCount(0); // Clear existing rows

        try {
            List<Subject> subjects = DataStorage.getAllSubjects();
            if (subjects.isEmpty()) {
                System.out.println("No subject data found.");
            } else {
                // Sort subjects by code before adding to table (optional)
                subjects.sort((s1, s2) -> s1.getCode().compareToIgnoreCase(s2.getCode()));

                for (Subject subject : subjects) {
                    // Join prerequisites list into a comma-separated string for display
                    String prereqs = subject.getPrerequisites().stream()
                            .collect(Collectors.joining(", "));

                    tableModel.addRow(new Object[]{
                            subject.getCode(),
                            subject.getName(),
                            subject.getUnits(),
                            subject.getDepartment(),
                            subject.getYearLevel(),
                            subject.getSemester(),
                            prereqs // Display formatted prerequisites
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading subject data: " + e.getMessage(),
                    "Data Loading Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // --- Action Handlers ---

    private void handleAddSubject() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (parentWindow instanceof Frame) ? (Frame) parentWindow : null;

        // Create and show the AddSubjectDialog (Create this class next)
        AddSubjectDialog addDialog = new AddSubjectDialog(parentFrame);
        addDialog.setVisible(true);

        // Refresh table if a subject was successfully added
        if (addDialog.isSaved()) {
            loadSubjectData();
        }
    }

    private void handleEditSubject() {
        int selectedRowView = subjectTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert view row index to model row index to handle sorting
        int selectedRowModel = subjectTable.convertRowIndexToModel(selectedRowView);
        String subjectCode = (String) tableModel.getValueAt(selectedRowModel, 0); // Get Code from the model

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (parentWindow instanceof Frame) ? (Frame) parentWindow : null;

        // Create and show the EditSubjectDialog (Create this class next)
        EditSubjectDialog editDialog = new EditSubjectDialog(parentFrame, subjectCode);
        editDialog.setVisible(true);

        // Refresh table if changes were saved
        if (editDialog.isSaved()) {
            loadSubjectData();
        }
    }

    private void handleDeleteSubject() {
        int selectedRowView = subjectTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subject to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRowModel = subjectTable.convertRowIndexToModel(selectedRowView);
        String subjectCode = (String) tableModel.getValueAt(selectedRowModel, 0);
        String subjectName = (String) tableModel.getValueAt(selectedRowModel, 1);

        // Optional: Check if this subject is a prerequisite for others before deleting
        boolean isPrereq = isSubjectPrerequisite(subjectCode);
        String warning = "";
        if (isPrereq) {
            warning = "\n\nWarning: This subject is a prerequisite for other courses!";
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete subject:\nCode: " + subjectCode + "\nName: " + subjectName + "?" + warning,
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                DataStorage.deleteSubject(subjectCode);
                loadSubjectData(); // Refresh table
                JOptionPane.showMessageDialog(this, "Subject deleted successfully.", "Deletion Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting subject: " + ex.getMessage(), "Deletion Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    // Helper method to check if a subject is used as a prerequisite
    private boolean isSubjectPrerequisite(String subjectCodeToCheck) {
        try {
            List<Subject> allSubjects = DataStorage.getAllSubjects();
            for (Subject subject : allSubjects) {
                if (subject.getPrerequisites() != null && subject.getPrerequisites().contains(subjectCodeToCheck)) {
                    return true; // Found it as a prerequisite
                }
            }
        } catch (Exception e) {
            // Log error or ignore, depending on desired behavior
            System.err.println("Error checking prerequisites: " + e.getMessage());
        }
        return false; // Not found as a prerequisite
    }
}