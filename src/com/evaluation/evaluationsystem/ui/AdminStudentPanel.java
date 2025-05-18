package com.evaluation.evaluationsystem.ui; // Adjust package if needed

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.Window;
import javax.swing.SwingUtilities;

public class AdminStudentPanel extends JPanel {

    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;

    public AdminStudentPanel() {
        setLayout(new BorderLayout(10, 10)); // Use BorderLayout
        initComponents();
        layoutComponents();
        addListeners(); // Add listeners for buttons (implement actions later)

        // Load data initially
        loadStudentData();
    }

    private void initComponents() {
        // Define table columns
        String[] columnNames = {"ID", "First Name", "Last Name", "Email", "Year Level", "Semester"};

        // Create a non-editable table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Make table cells non-editable by default
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow only one row selection
        studentTable.setAutoCreateRowSorter(true); // Enable sorting by clicking headers

        // Initialize buttons
        addButton = new JButton("Add Student");
        editButton = new JButton("Edit Selected");
        deleteButton = new JButton("Delete Selected");
        refreshButton = new JButton("Refresh List");
    }

    private void layoutComponents() {
        // Table with Scroll Pane in the Center
        JScrollPane scrollPane = new JScrollPane(studentTable);
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel at the Top (or Bottom)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Align buttons left
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);

        add(buttonPanel, BorderLayout.NORTH); // Add button panel to the top
    }

    private void addListeners() {
        refreshButton.addActionListener(e -> loadStudentData());

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddStudent(); // Call method to handle adding
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEditStudent(); // Call method to handle editing
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDeleteStudent(); // Call method to handle deleting
            }
        });
    }

    // Method to load or refresh student data in the table
    public void loadStudentData() {
        // Clear existing rows
        tableModel.setRowCount(0);

        // Get data from storage
        try {
            List<Student> students = DataStorage.getAllStudents();
            if (students.isEmpty()) {
                // Optionally show a message in the table or a label
                System.out.println("No student data found.");
            } else {
                for (Student student : students) {
                    tableModel.addRow(new Object[]{
                            student.getId(),
                            student.getFirstName(),
                            student.getLastName(),
                            student.getEmail(),
                            student.getYearLevel(),
                            student.getSemester()
                    });
                }
            }
        } catch (Exception e) {
            // Handle potential errors during data loading (e.g., file not found, JSON parsing issues)
            JOptionPane.showMessageDialog(this,
                    "Error loading student data: " + e.getMessage(),
                    "Data Loading Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Log the full error for debugging
        }
    }

    // --- Placeholder Action Handlers (Implement logic later) ---

    private void handleAddStudent() {
        // Find the parent window (the AdminDashboardFrame)
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = null;
        if (parentWindow instanceof Frame) {
            parentFrame = (Frame) parentWindow;
        }

        // Create and show the dialog
        AddStudentDialog addDialog = new AddStudentDialog(parentFrame);
        addDialog.setVisible(true); // This blocks until the dialog is closed

        // After the dialog is closed, check if data was saved and refresh
        if (addDialog.isSaved()) {
            loadStudentData(); // Refresh the JTable in this panel
        }
    }

    private void handleEditStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert view row to model row in case of sorting
        int modelRow = studentTable.convertRowIndexToModel(selectedRow);
        String studentId = (String) tableModel.getValueAt(modelRow, 0); // Get ID from the model

        // Find the parent window (the AdminDashboardFrame)
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        Frame parentFrame = null;
        if (parentWindow instanceof Frame) {
            parentFrame = (Frame) parentWindow;
        } else {
            // Handle case where parent is not a Frame (less likely for a main panel)
            // You might want to pass null or handle it differently
            System.err.println("Warning: Parent window is not a Frame.");
        }

        // Create and show the EditStudentDialog, passing the student ID
        EditStudentDialog editDialog = new EditStudentDialog(parentFrame, studentId);
        editDialog.setVisible(true); // Blocks until the dialog is closed

        // After the dialog is closed, check if data was saved and refresh
        if (editDialog.isSaved()) {
            loadStudentData(); // Refresh the JTable in this panel
        }
    }

    private void handleDeleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a student to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = studentTable.convertRowIndexToModel(selectedRow);
        String studentId = (String) tableModel.getValueAt(modelRow, 0);
        String studentName = tableModel.getValueAt(modelRow, 1) + " " + tableModel.getValueAt(modelRow, 2);


        int confirmation = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete student:\nID: " + studentId + "\nName: " + studentName + "\n\nThis will also remove their user account and enrollment records.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                DataStorage.deleteStudent(studentId); // This should handle deleting related data too
                loadStudentData(); // Refresh table
                JOptionPane.showMessageDialog(this, "Student deleted successfully.", "Deletion Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting student: " + ex.getMessage(), "Deletion Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}