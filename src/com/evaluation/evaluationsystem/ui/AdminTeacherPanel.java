package com.evaluation.evaluationsystem.ui; // Adjust package if needed

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.Subject;
import com.evaluation.evaluationsystem.models.Teacher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter; // Import for sorting
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminTeacherPanel extends JPanel {

    private JTable teacherTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private TableRowSorter<DefaultTableModel> sorter; // For sorting

    public AdminTeacherPanel() {
        setLayout(new BorderLayout(10, 10)); // Use BorderLayout with gaps
        initComponents();
        layoutComponents();
        addListeners();

        // Load data initially
        loadTeacherData();
    }

    private void initComponents() {
        // Define table columns for teachers
        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Department", "Position", "Assigned Subject"};

        // Create a non-editable table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Cells are not directly editable
            }
        };

        teacherTable = new JTable(tableModel);
        teacherTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow only one row selection

        // Enable sorting
        sorter = new TableRowSorter<>(tableModel);
        teacherTable.setRowSorter(sorter);

        // Initialize buttons
        addButton = new JButton("Add Teacher");
        editButton = new JButton("Edit Selected");
        deleteButton = new JButton("Delete Selected");
        refreshButton = new JButton("Refresh List");
    }

    private void layoutComponents() {
        // Table with Scroll Pane in the Center
        JScrollPane scrollPane = new JScrollPane(teacherTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel at the Top
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH); // Add button panel to the top
    }

    private void addListeners() {
        refreshButton.addActionListener(e -> loadTeacherData());
        addButton.addActionListener(e -> handleAddTeacher());
        editButton.addActionListener(e -> handleEditTeacher());
        deleteButton.addActionListener(e -> handleDeleteTeacher());
    }

    // Method to load or refresh teacher data in the table
    public void loadTeacherData() {
        tableModel.setRowCount(0); // Clear existing rows

        try {
            List<Teacher> teachers = DataStorage.getAllTeachers();
            if (teachers.isEmpty()) {
                System.out.println("No teacher data found.");
            } else {
                for (Teacher teacher : teachers) {
                    // --- MODIFIED PART: Handle List of Subjects ---
                    List<String> assignedCodes = teacher.getAssignedSubjectCodes();
                    String subjectsDisplay; // String to show in the table cell

                    if (assignedCodes == null || assignedCodes.isEmpty()) {
                        subjectsDisplay = "N/A"; // Display if no subjects are assigned
                    } else {
                        // Join the list of codes into a comma-separated string
                        subjectsDisplay = String.join(", ", assignedCodes);

                        // --- Optional Enhancement: Display Code - Name ---
                        /* // Uncomment this block to show names instead of just codes
                           // (might make the column very wide)
                        subjectsDisplay = assignedCodes.stream()
                            .map(code -> {
                                Optional<Subject> subjOpt = DataStorage.getSubjectByCode(code);
                                return subjOpt.map(s -> s.getCode() + "-" + s.getName()) // Format as Code-Name
                                              .orElse(code + "?"); // Show code? if name not found
                            })
                            .collect(Collectors.joining("; ")); // Join with semicolon and space
                        */
                        // --- End Optional Enhancement ---
                    }
                    // --- END MODIFIED PART ---

                    tableModel.addRow(new Object[]{
                            teacher.getId(),
                            teacher.getFirstName(),
                            teacher.getLastName(),
                            teacher.getEmail(),
                            teacher.getDepartment(),
                            teacher.getPosition(),
                            subjectsDisplay // Use the generated string for display
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading teacher data: " + e.getMessage(),
                    "Data Loading Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // --- Action Handlers ---

    private void handleAddTeacher() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (parentWindow instanceof Frame) ? (Frame) parentWindow : null;

        // Create and show the AddTeacherDialog (You will create this class next)
        AddTeacherDialog addDialog = new AddTeacherDialog(parentFrame);
        addDialog.setVisible(true);

        // Refresh table if a teacher was successfully added
        if (addDialog.isSaved()) {
            loadTeacherData();
        }
    }

    private void handleEditTeacher() {
        int selectedRowView = teacherTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Please select a teacher to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert view row index to model row index to handle sorting
        int selectedRowModel = teacherTable.convertRowIndexToModel(selectedRowView);
        String teacherId = (String) tableModel.getValueAt(selectedRowModel, 0); // Get ID from the model

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = (parentWindow instanceof Frame) ? (Frame) parentWindow : null;

        // Create and show the EditTeacherDialog (You will create this class next)
        EditTeacherDialog editDialog = new EditTeacherDialog(parentFrame, teacherId);
        editDialog.setVisible(true);

        // Refresh table if changes were saved
        if (editDialog.isSaved()) {
            loadTeacherData();
        }
    }

    private void handleDeleteTeacher() {
        int selectedRowView = teacherTable.getSelectedRow();
        if (selectedRowView == -1) {
            JOptionPane.showMessageDialog(this, "Please select a teacher to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert to model index
        int selectedRowModel = teacherTable.convertRowIndexToModel(selectedRowView);
        String teacherId = (String) tableModel.getValueAt(selectedRowModel, 0);
        String teacherName = tableModel.getValueAt(selectedRowModel, 1) + " " + tableModel.getValueAt(selectedRowModel, 2);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete teacher:\nID: " + teacherId + "\nName: " + teacherName + "\n\nThis will also remove their user account.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                DataStorage.deleteTeacher(teacherId); // This should also delete the user
                loadTeacherData(); // Refresh table
                JOptionPane.showMessageDialog(this, "Teacher deleted successfully.", "Deletion Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting teacher: " + ex.getMessage(), "Deletion Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}