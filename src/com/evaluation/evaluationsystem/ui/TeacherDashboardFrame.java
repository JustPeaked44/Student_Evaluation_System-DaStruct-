package com.evaluation.evaluationsystem.ui; // Corrected package

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.EnrolledSubject;
import com.evaluation.evaluationsystem.models.Enrollment;
import com.evaluation.evaluationsystem.models.Student;
import com.evaluation.evaluationsystem.models.Subject;
import com.evaluation.evaluationsystem.models.Teacher;
import com.evaluation.evaluationsystem.utils.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent; // new
import java.awt.event.ItemListener; // new
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap; // new
import java.util.Map; // new
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TeacherDashboardFrame extends JFrame {

    // --- Teacher Info ---
    private JLabel teacherNameLabel;
    private JLabel teacherIdLabel;
    private JLabel teacherEmailLabel;
    private JLabel teacherDepartmentLabel;

    // --- NEW: Subject Selection ---
    private JComboBox<Subject> subjectSelectorComboBox; // Use Subject objects directly
    private Map<String, Subject> subjectDisplayMap; // displays string to subj obj
    private Subject currentlySelectedSubject; // tracks current selected subj obj
    private JLabel subjectSelectorLabel;

    // --- Student Table ---
    private JTable studentTable;
    private DefaultTableModel studentTableModel;
    private JScrollPane studentTableScrollPane;
    private TableRowSorter<DefaultTableModel> sorter;

    // --- Buttons ---
    private JButton editGradesButton;
    private JButton refreshButton; // Refreshes students for the *currently selected* subject
    private JButton logoutButton;
    private JButton editProfileButton;

    // --- Data ---
    private Teacher currentTeacher;
    private List<Subject> allAssignedSubjects; // Store all assigned subjects

    public TeacherDashboardFrame() {
        // --- Security Check ---
        if (!SessionManager.isLoggedIn() || !"Teacher".equalsIgnoreCase(SessionManager.getCurrentUserRole())) {
            handleInvalidAccess();
            SwingUtilities.invokeLater(this::dispose);
            return;
        }

        // --- Load Teacher Data ---
        if (!loadCurrentTeacherDataAndSubjects()) { // Renamed method
            handleInvalidAccess("Could not load teacher data or subjects.");
            SwingUtilities.invokeLater(this::dispose);
            return;
        }

        // --- Frame Setup ---
        setTitle("Teacher Dashboard - Welcome Prof. " + currentTeacher.getLastName());
        setSize(950, 650);
        setMinimumSize(new Dimension(750, 500));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Initialize UI Components ---
        initComponents(); // Initializes components including the combo box
        populateSubjectSelector(); // Populates the combo box AFTER it's created
        layoutComponents();
        addListeners();

        updateStudentTableForSelectedSubject();
        // --- Load Initial Data for Table ---
        // Trigger loading students for the initially selected subject (if any)
        selectInitialSubject();
    }

    // Handles cases where access is denied or data is missing
    private void handleInvalidAccess(String message) {
        JOptionPane.showMessageDialog(null, message, "Access Error", JOptionPane.ERROR_MESSAGE);
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
    private void handleInvalidAccess() {
        handleInvalidAccess("Access Denied. Please login as Teacher.");
    }


    // Load data for the currently logged-in teacher AND their assigned subjects
    private boolean loadCurrentTeacherDataAndSubjects() {
        String teacherId = SessionManager.getCurrentUserId();
        Optional<Teacher> teacherOpt = DataStorage.getTeacherById(teacherId);

        if (teacherOpt.isPresent()) {
            currentTeacher = teacherOpt.get();
            allAssignedSubjects = new ArrayList<>(); // Initialize the list

            // --- Handle Multiple Assigned Subjects ---
            List<String> assignedCodes = currentTeacher.getAssignedSubjectCodes();

            if (assignedCodes != null && !assignedCodes.isEmpty()) {
                for (String code : assignedCodes) {
                    // Fetch the full Subject object for each code
                    DataStorage.getSubjectByCode(code).ifPresent(allAssignedSubjects::add);
                }
                // Sort the list of Subject objects (e.g., by code)
                allAssignedSubjects.sort(Comparator.comparing(Subject::getCode));
            }
            // If list is empty, allAssignedSubjects remains empty
            return true; // Teacher data loaded (even if no subjects assigned)
        }
        // Teacher not found
        return false;
    }

    private void initComponents() {
        // --- Labels for Teacher Info ---
        teacherNameLabel = new JLabel("Prof. " + currentTeacher.getFirstName() + " " + currentTeacher.getLastName());
        teacherNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        teacherIdLabel = new JLabel("ID: " + currentTeacher.getId());
        teacherEmailLabel = new JLabel("Email: " + currentTeacher.getEmail());
        teacherDepartmentLabel = new JLabel("Department: " + currentTeacher.getDepartment());

        // --- Subject Selector ---
        subjectSelectorLabel = new JLabel("Manage Subject:");
        subjectSelectorComboBox = new JComboBox<>(); // Model will be set later
        subjectSelectorComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subjectSelectorComboBox.setPreferredSize(new Dimension(300, 28)); // Give it some width



        // --- Student Table ---
        String[] columnNames = {"Student ID", "Last Name", "First Name", "Year Level", "Current Grade"};
        studentTableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) { return String.class; } // Display only
        };
        studentTable = new JTable(studentTableModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setFillsViewportHeight(true);
        sorter = new TableRowSorter<>(studentTableModel);
        studentTable.setRowSorter(sorter);
        studentTableScrollPane = new JScrollPane(studentTable);

        // --- Buttons ---
        editGradesButton = new JButton("Enter/Edit Grades");
        refreshButton = new JButton("Refresh Student List");
        logoutButton = new JButton("Logout");
        editProfileButton = new JButton("Edit Profile / Password");

        // Initially disable grading button until a subject is selected/loaded
        editGradesButton.setEnabled(false);
        refreshButton.setEnabled(false);
    }

    // Populate the subject selector combo box
    private void populateSubjectSelector() {
        // Use DefaultComboBoxModel for easier manipulation
        DefaultComboBoxModel<Subject> comboBoxModel = new DefaultComboBoxModel<>();
        subjectSelectorComboBox.setModel(comboBoxModel); // Set the model

        if (allAssignedSubjects != null && !allAssignedSubjects.isEmpty()) {
            comboBoxModel.addElement(null); // Add a placeholder/prompt item first
            for (Subject subject : allAssignedSubjects) {
                comboBoxModel.addElement(subject); // Add Subject objects
            }
            subjectSelectorComboBox.setSelectedIndex(0); // Select the placeholder initially
            // Set a custom renderer to display "Select Subject..." for null
            subjectSelectorComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Subject) {
                        Subject subject = (Subject) value;
                        setText(subject.getCode() + " - " + subject.getName()); // Use Subject's toString or custom format
                    } else if (value == null) {
                        setText("--- Select Subject ---"); // Placeholder text
                        setForeground(Color.GRAY);
                    }
                    return this;
                }
            });
            subjectSelectorComboBox.setEnabled(true);

        } else {
            // Handle case where teacher has no subjects assigned
            comboBoxModel.addElement(null); // Add placeholder
            subjectSelectorComboBox.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    setText("No subjects assigned");
                    setEnabled(false); // Disable the item
                    return this;
                }
            });
            subjectSelectorComboBox.setEnabled(false); // Disable the whole combo box
            editGradesButton.setEnabled(false); // Ensure button is disabled
            refreshButton.setEnabled(false);
        }
    }


    private void layoutComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Top Panel: Teacher Info & Subject Selector ---
        JPanel infoPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for more control
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                new EmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5); // Padding
        gbc.anchor = GridBagConstraints.WEST; // Align left

        // Teacher Info (Column 0)
        gbc.gridx = 0; gbc.gridy = 0; infoPanel.add(teacherNameLabel, gbc);
        gbc.gridy = 1; infoPanel.add(teacherIdLabel, gbc);
        gbc.gridy = 2; infoPanel.add(teacherEmailLabel, gbc);
        gbc.gridy = 3; infoPanel.add(teacherDepartmentLabel, gbc);

        // Separator (Column 1) - Optional visual separation
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridheight = 4; gbc.fill = GridBagConstraints.VERTICAL;
        infoPanel.add(new JSeparator(SwingConstants.VERTICAL), gbc);
        gbc.gridheight = 1; gbc.fill = GridBagConstraints.NONE; // Reset

        // Subject Selector (Column 2)
        gbc.gridx = 2; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST; infoPanel.add(subjectSelectorLabel, gbc);
        gbc.gridy = 1; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0; // Allow combo box to stretch
        infoPanel.add(subjectSelectorComboBox, gbc);

        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // --- Center Panel for Student Table ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        // Title will now depend on selected subject, maybe update dynamically or remove
        tablePanel.setBorder(new TitledBorder("Enrolled Students"));
        tablePanel.add(studentTableScrollPane, BorderLayout.CENTER);
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        // --- Bottom Panel for Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(editGradesButton);
        buttonPanel.add(refreshButton);

        // --- Bottom Panel for Buttons ---
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel for action buttons
        actionButtonPanel.add(editGradesButton);
        actionButtonPanel.add(refreshButton);

        JPanel profileLogoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Panel for profile/logout
        // --- Verify Button is Added Here ---
        profileLogoutPanel.add(editProfileButton); // *** THIS LINE MUST BE PRESENT ***
        profileLogoutPanel.add(logoutButton); // Add Logout button

        JPanel bottomOuterPanel = new JPanel(new BorderLayout());
        bottomOuterPanel.add(actionButtonPanel, BorderLayout.WEST); // Action buttons on the left
        bottomOuterPanel.add(profileLogoutPanel, BorderLayout.EAST); // Profile/Logout on the right

        mainPanel.add(bottomOuterPanel, BorderLayout.SOUTH);
        setContentPane(mainPanel);
    }

    private void addListeners() {
        logoutButton.addActionListener(e -> handleLogout());
        refreshButton.addActionListener(e -> {
            // Refresh students for the CURRENTLY selected subject
            Subject selectedSubject = (Subject) subjectSelectorComboBox.getSelectedItem();
            if (selectedSubject != null) {
                loadAssignedStudents(selectedSubject.getCode());
            } else {
                studentTableModel.setRowCount(0); // Clear table if no subject selected
            }
        });
        editGradesButton.addActionListener(e -> handleEditGrades());
        editProfileButton.addActionListener(e -> handleEditProfile());


        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { handleLogout(); }
        });


        // --- Listener for Subject Selector ComboBox ---
        subjectSelectorComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Subject selectedSubject = (Subject) subjectSelectorComboBox.getSelectedItem();
                if (selectedSubject != null) {
                    // Load students for the newly selected subject
                    loadAssignedStudents(selectedSubject.getCode());
                    editGradesButton.setEnabled(true); // Enable grading button
                    refreshButton.setEnabled(true);
                } else {
                    // No subject selected (placeholder selected)
                    studentTableModel.setRowCount(0); // Clear the table
                    editGradesButton.setEnabled(false); // Disable grading button
                    refreshButton.setEnabled(false);
                }
            }
        });
    }

    // Select the first actual subject in the combo box on initial load
    private void selectInitialSubject() {
        if (subjectSelectorComboBox.getItemCount() > 1) { // More than just the placeholder
            subjectSelectorComboBox.setSelectedIndex(1); // Select the first actual subject
            // The ActionListener on the combo box will trigger loadAssignedStudents
        } else if (subjectSelectorComboBox.getItemCount() == 1 && subjectSelectorComboBox.getItemAt(0) != null) {
            // Only one subject assigned
            subjectSelectorComboBox.setSelectedIndex(0);
        }
        else {
            // No subjects assigned or only placeholder exists
            loadAssignedStudents(null); // Load empty table
            editGradesButton.setEnabled(false);
        }
    }


    // Modified to accept subjectCode parameter
    private void loadAssignedStudents(String subjectCode) {
        studentTableModel.setRowCount(0); // Clear table

        // If no subject code is provided (e.g., placeholder selected), do nothing further
        if (subjectCode == null || subjectCode.trim().isEmpty()) {
            System.out.println("No subject selected to load students for.");
            return;
        }

        System.out.println("Loading students for subject: " + subjectCode); // Debugging
        List<Object[]> rowData = new ArrayList<>();

        try {
            List<Student> allStudents = DataStorage.getAllStudents();
            List<Enrollment> allEnrollments = DataStorage.getAllEnrollments();

            for (Student student : allStudents) {
                for (Enrollment enrollment : allEnrollments) {
                    if (enrollment.getStudentId().equals(student.getId())) {
                        for (EnrolledSubject enrolledSub : enrollment.getSubjects()) {
                            if (enrolledSub.getCode().equals(subjectCode)) {
                                rowData.add(new Object[]{
                                        student.getId(),
                                        student.getLastName(),
                                        student.getFirstName(),
                                        enrollment.getYearLevel(),
                                        enrolledSub.getGrade() == 0.0 ? "NG" : String.format("%.1f", enrolledSub.getGrade())
                                });
                                break; // Found subject in this enrollment
                            }
                        }
                    }
                }
            }

            rowData.sort(Comparator.comparing(row -> (String) row[1])); // Sort by last name

            for (Object[] row : rowData) {
                studentTableModel.addRow(row);
            }
            System.out.println("Loaded " + rowData.size() + " students."); // Debugging

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading student list for " + subjectCode + ": " + e.getMessage(),
                    "Data Loading Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handleEditProfile() {
        if (currentTeacher == null) {
            showError("Error", "Cannot edit profile. Teacher data not loaded."); // Use a local showError helper if needed
            return;
        }
        // Create and show the EditTeacherProfileDialog
        EditTeacherProfileDialog profileDialog = new EditTeacherProfileDialog(this, currentTeacher); // Pass the current teacher
        profileDialog.setVisible(true);

        // After the dialog closes, refresh teacher data if changes were saved
        if (profileDialog.isSaved()) {
            // Reload teacher data to reflect potential changes
            if (loadCurrentTeacherDataAndSubjects()) {
                // Update display labels
                teacherNameLabel.setText("Prof. " + currentTeacher.getFirstName() + " " + currentTeacher.getLastName());
                teacherEmailLabel.setText("Email: " + currentTeacher.getEmail());
                teacherDepartmentLabel.setText("Department: " + currentTeacher.getDepartment());
                // Re-setting assigned subject label might be needed if more details are shown later
//                if (assignedSubject != null) {
//                    assignedSubjectLabel.setText("Assigned Subject: " + assignedSubject.getCode() + " - " + assignedSubject.getName());
//                } else {
//                    assignedSubjectLabel.setText("Assigned Subject: N/A");
//                }
                System.out.println("Teacher profile data refreshed after edit.");
            } else {
                showError("Error", "Could not reload teacher data after profile update.");
            }
        }
    }

    // Add a simple showError helper if you don't have one in this class
    private void showError(String title, String message){
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    // Modified to get subject code from the combo box
    private void handleEditGrades() {
        Subject selectedSubject = (Subject) subjectSelectorComboBox.getSelectedItem();

        if (selectedSubject == null) { // Check if the selected item is the placeholder or null
            JOptionPane.showMessageDialog(this, "Please select a subject from the list first.", "Subject Not Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subjectCodeToEdit = selectedSubject.getCode();

        // Create and show the EditGradesDialog
        EditGradesDialog gradesDialog = new EditGradesDialog(this, subjectCodeToEdit);
        gradesDialog.setVisible(true); // Blocks until closed

        // After the dialog is closed, refresh the student list for the *currently selected* subject
        loadAssignedStudents(subjectCodeToEdit);
    }

    private void updateStudentTableForSelectedSubject() {
        // Get the currently selected item from the ComboBox model
        Subject selectedSubject = (Subject) subjectSelectorComboBox.getSelectedItem();

        if (selectedSubject != null) {
            // If a valid subject is selected, load students for its code
            System.out.println("Updating student table for: " + selectedSubject.getCode()); // Debug
            loadAssignedStudents(selectedSubject.getCode());
            editGradesButton.setEnabled(true); // Enable grading button
            refreshButton.setEnabled(true);
        } else {
            // If the placeholder ("--- Select Subject ---") or null is selected
            studentTableModel.setRowCount(0); // Clear the table
            editGradesButton.setEnabled(false); // Disable grading button
            refreshButton.setEnabled(false);
            //System.out.println("No subject selected, clearing table."); // Debug porpurses
        }
    }

    // Handles logout confirmation and action (no changes needed here)
    private void handleLogout() {
        int confirmation = JOptionPane.showConfirmDialog(
                this, "Are you sure you want to logout?",
                "Logout Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            SessionManager.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}