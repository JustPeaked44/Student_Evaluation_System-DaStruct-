package com.evaluation.evaluationsystem.ui; // Adjust package if needed

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.EnrolledSubject;
import com.evaluation.evaluationsystem.models.Enrollment;
import com.evaluation.evaluationsystem.models.Student;
import com.evaluation.evaluationsystem.models.Subject;
import com.evaluation.evaluationsystem.utils.SessionManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Optional;
import java.awt.Desktop; // For opening files
import java.io.File;     // For file handling
import java.io.InputStream;// To read from resources
import java.net.URI;      // For resource location
import java.nio.file.Files; // For copying file
import java.nio.file.Path;  // For file path
import java.nio.file.StandardCopyOption; // For copying option

public class StudentDashboardFrame extends JFrame {

    // --- Student Info ---
    private JLabel studentNameLabel;
    private JLabel studentIdLabel;
    private JLabel studentEmailLabel;
    private JLabel studentYearSemLabel; // Combines Year Level and Semester

    // --- Grades/Transcript Table ---
    private JTable gradesTable;
    private DefaultTableModel gradesTableModel;
    private JScrollPane gradesTableScrollPane;

    // --- Progress Summary ---
    private JLabel gpaLabel;
    private JLabel unitsCompletedLabel;
    private JLabel unitsRemainingLabel; // Calculated
    // private JProgressBar progressBar; // Optional visual progress

    // --- Actions/Controls ---
    private JButton enrollButton; // Button to launch enrollment process
    private JButton viewProspectusButton; // Button to view PDF
    private JButton refreshButton;
    private JButton logoutButton;
    private JButton editProfileButton;

    // --- Data ---
    private Student currentStudent;
    private List<Enrollment> studentEnrollments; // Store loaded enrollments
    private final int TOTAL_UNITS_REQUIRED = 176; // Example total units for BSIT (from PDF summary)

    public StudentDashboardFrame() {
        // --- Security Check ---
        if (!SessionManager.isLoggedIn() || !"Student".equalsIgnoreCase(SessionManager.getCurrentUserRole())) {
            handleInvalidAccess();
            SwingUtilities.invokeLater(this::dispose);
            return;
        }

        // --- Load Student Data ---
        if (!loadCurrentStudentData()) {
            handleInvalidAccess("Could not load student data.");
            SwingUtilities.invokeLater(this::dispose);
            return;
        }

        // --- Frame Setup ---
        setTitle("Student Dashboard - Welcome " + currentStudent.getFirstName());
        setSize(900, 700);
        setMinimumSize(new Dimension(700, 550));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Initialize UI ---
        initComponents();
        layoutComponents();
        addListeners();

        // --- Load Initial Data ---
        loadAcademicHistory();
        updateProgressSummary();
    }

    // Handles cases where access is denied or data is missing
    private void handleInvalidAccess(String message) {
        JOptionPane.showMessageDialog(null, message, "Access Error", JOptionPane.ERROR_MESSAGE);
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
    private void handleInvalidAccess() {
        handleInvalidAccess("Access Denied. Please login as Student.");
    }

    // Load data for the currently logged-in student
    private boolean loadCurrentStudentData() {
        String studentId = SessionManager.getCurrentUserId();
        Optional<Student> studentOpt = DataStorage.getStudentById(studentId);
        if (studentOpt.isPresent()) {
            currentStudent = studentOpt.get();
            return true;
        }
        return false;
    }

    private void initComponents() {
        // --- Labels for Student Info ---
        studentNameLabel = new JLabel("Name: " + currentStudent.getFirstName() + " " + currentStudent.getLastName());
        studentNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        studentIdLabel = new JLabel("ID: " + currentStudent.getId());
        studentEmailLabel = new JLabel("Email: " + currentStudent.getEmail());
        studentYearSemLabel = new JLabel("Current: " + currentStudent.getYearLevel() + " - " + currentStudent.getSemester());

        // --- Grades Table ---
        String[] columnNames = {"Term", "Subject Code", "Subject Name", "Units", "Grade", "Status"};
        gradesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Read-only table
            }
        };
        gradesTable = new JTable(gradesTableModel);
        gradesTable.setAutoCreateRowSorter(true);
        gradesTable.setFillsViewportHeight(true); // Table uses entire height of scroll pane
        gradesTableScrollPane = new JScrollPane(gradesTable);

        // Adjust column widths
        gradesTable.getColumnModel().getColumn(0).setPreferredWidth(120); // Term
        gradesTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Code
        gradesTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Name
        gradesTable.getColumnModel().getColumn(3).setPreferredWidth(50);  // Units
        gradesTable.getColumnModel().getColumn(4).setPreferredWidth(50);  // Grade
        gradesTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Status

        // --- Progress Summary Labels ---
        gpaLabel = new JLabel("GPA: --");
        unitsCompletedLabel = new JLabel("Units Completed: --");
        unitsRemainingLabel = new JLabel("Units Remaining: --");
        // progressBar = new JProgressBar(0, TOTAL_UNITS_REQUIRED);

        // --- Buttons ---
        enrollButton = new JButton("Enroll for Next Term");
        viewProspectusButton = new JButton("View Prospectus (PDF)");
        refreshButton = new JButton("Refresh Data");
        logoutButton = new JButton("Logout");
        editProfileButton = new JButton("Edit Profile");

    }

    private void layoutComponents() {
        // Use a main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Top: Student Info Panel ---
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5)); // Horizontal layout with spacing
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                new EtchedBorder(), new EmptyBorder(5, 10, 5, 10)));
        infoPanel.add(studentNameLabel);
        infoPanel.add(studentIdLabel);
        infoPanel.add(studentEmailLabel);
        infoPanel.add(studentYearSemLabel);
        mainPanel.add(infoPanel, BorderLayout.NORTH);

        // --- Center: Grades Table ---
        JPanel gradesPanel = new JPanel(new BorderLayout());
        gradesPanel.setBorder(new TitledBorder("Academic Record"));
        gradesPanel.add(gradesTableScrollPane, BorderLayout.CENTER);
        mainPanel.add(gradesPanel, BorderLayout.CENTER);

        // --- Right: Sidebar for Progress & Actions ---
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS)); // Vertical stack
        sidebarPanel.setBorder(new EmptyBorder(0, 10, 0, 0)); // Add left padding

        // Progress Summary Box
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Progress Summary"), new EmptyBorder(10, 10, 10, 10)));

        gpaLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        unitsCompletedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        unitsRemainingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        // progressBar.setStringPainted(true);

        progressPanel.add(gpaLabel);
        progressPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        progressPanel.add(unitsCompletedLabel);
        progressPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        progressPanel.add(unitsRemainingLabel);
        // progressPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        // progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalGlue()); // Pushes content up

        // Actions Box
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Actions"), new EmptyBorder(10, 10, 10, 10)));

        Dimension buttonSize = new Dimension(180, 30);

        editProfileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        editProfileButton.setMaximumSize(buttonSize);
        actionsPanel.add(editProfileButton);
        actionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Center buttons within the actions panel
        enrollButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewProspectusButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Set max size for buttons to prevent stretching
        enrollButton.setMaximumSize(buttonSize);
        viewProspectusButton.setMaximumSize(buttonSize);
        refreshButton.setMaximumSize(buttonSize);
        logoutButton.setMaximumSize(buttonSize);


        actionsPanel.add(enrollButton);
        actionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionsPanel.add(viewProspectusButton);
        actionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionsPanel.add(refreshButton);
        actionsPanel.add(Box.createVerticalGlue()); // Pushes buttons up
        actionsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionsPanel.add(logoutButton);


        sidebarPanel.add(progressPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Space between boxes
        sidebarPanel.add(actionsPanel);

        mainPanel.add(sidebarPanel, BorderLayout.EAST); // Add sidebar to the right

        setContentPane(mainPanel);
    }

    private void addListeners() {
        logoutButton.addActionListener(e -> handleLogout());
        refreshButton.addActionListener(e -> {
            loadAcademicHistory();
            updateProgressSummary();
        });
        viewProspectusButton.addActionListener(e -> handleViewProspectus());
        enrollButton.addActionListener(e -> handleEnroll());
        editProfileButton.addActionListener(e -> handleEditProfile());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleLogout();
            }
        });
    }

    // Load and display student's enrollment
    private void loadAcademicHistory() {
        gradesTableModel.setRowCount(0); // Clear existing data
        studentEnrollments = DataStorage.getEnrollmentsForStudent(currentStudent.getId());

        // Sort enrollments (e.g., by year then semester) - requires parsing year/sem
        // For simplicity, we'll add them as they come for now.

        if (studentEnrollments.isEmpty()) {
            System.out.println("No enrollment history found for student: " + currentStudent.getId());
            // Optionally display a message in the table area
            return;
        }

        for (Enrollment enrollment : studentEnrollments) {
            String term = enrollment.getYearLevel() + " - " + enrollment.getSemester();
            for (EnrolledSubject subject : enrollment.getSubjects()) {
                String gradeDisplay = subject.getGrade() == 0.0 ? "NG" : String.format("%.1f", subject.getGrade());
                String status = subject.getStatus(); // Get status from EnrolledSubject

                gradesTableModel.addRow(new Object[]{
                        term,
                        subject.getCode(),
                        subject.getName(),
                        subject.getUnits(),
                        gradeDisplay,
                        status
                });
            }
        }
    }

    // Calculate and display GPA and unit progress
    private void updateProgressSummary() {
        if (studentEnrollments == null || studentEnrollments.isEmpty()) {
            gpaLabel.setText("GPA: N/A");
            unitsCompletedLabel.setText("Units Completed: 0");
            unitsRemainingLabel.setText("Units Remaining: " + TOTAL_UNITS_REQUIRED);
            // progressBar.setValue(0);
            // progressBar.setString("0 / " + TOTAL_UNITS_REQUIRED);
            return;
        }

        double totalGradePoints = 0;
        int totalUnitsAttemptedWithGrade = 0; // Units for subjects that have a non-NG grade
        int unitsCompleted = 0;

        for (Enrollment enrollment : studentEnrollments) {
            for (EnrolledSubject subject : enrollment.getSubjects()) {
                double grade = subject.getGrade();
                int units = subject.getUnits();

                if (grade > 0.0) { // Only count subjects with actual grades (not NG) for GPA
                    totalGradePoints += grade * units;
                    totalUnitsAttemptedWithGrade += units;

                    if (grade <= 3.0) { // Assuming 3.0 is the passing threshold
                        unitsCompleted += units;
                    }
                }
            }
        }

        double gpa = (totalUnitsAttemptedWithGrade > 0) ? (totalGradePoints / totalUnitsAttemptedWithGrade) : 0.0;
        int unitsRemaining = TOTAL_UNITS_REQUIRED - unitsCompleted;

        gpaLabel.setText(String.format("GPA: %.2f", gpa));
        unitsCompletedLabel.setText("Units Completed: " + unitsCompleted);
        unitsRemainingLabel.setText("Units Remaining: " + Math.max(0, unitsRemaining)); // Don't show negative remaining

        // Update progress bar (optional)
        // progressBar.setValue(unitsCompleted);
        // progressBar.setString(unitsCompleted + " / " + TOTAL_UNITS_REQUIRED);
    }

    // --- Action Handlers ---

    private void handleViewProspectus() {
        String pdfResourcePath = "/docs/(BSIT)2023-2024_BSIT.pdf"; // Path within resources

        // Check if Desktop API is supported
        if (!Desktop.isDesktopSupported()) {
            showError("Unsupported Action", "Cannot open files automatically on this system.");
            return;
        }
        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            showError("Unsupported Action", "Opening files is not supported by the system.");
            return;
        }

        try (InputStream pdfStream = getClass().getResourceAsStream(pdfResourcePath)) {

            if (pdfStream == null) {
                showError("File Not Found", "Prospectus PDF not found at resource path: " + pdfResourcePath);
                return;
            }

            // Create a temporary file to avoid issues with running from JARs
            // Suffix is important for Desktop.open to recognize the file type
            Path tempFile = Files.createTempFile("BSIT_Prospectus_", ".pdf");
            tempFile.toFile().deleteOnExit(); // Ensure cleanup

            // Copy the resource stream to the temporary file
            Files.copy(pdfStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

            // Open the temporary file using the default PDF viewer
            desktop.open(tempFile.toFile());

        } catch (Exception ex) {
            showError("Error Opening Prospectus", "Could not open the PDF file: " + ex.getMessage());
            ex.printStackTrace(); // Log the full error for debugging
        }
    }

    // Helper method for showing errors (add if not already present)
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

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

    private String[] calculateNextTerm(String currentYear, String currentSemester) {
        String nextYear = currentYear;
        String nextSemester = currentSemester; // Start with current as fallback

        if (currentSemester == null || currentYear == null) {
            System.err.println("Cannot calculate next term: Current year or semester is null.");
            return null; // Cannot determine next term
        }

        // Logic based on common progression
        if (currentSemester.equals("1st Semester")) {
            nextSemester = "2nd Semester";
            // Year stays the same
        } else if (currentSemester.equals("2nd Semester")) {
            nextSemester = "1st Semester"; // Moving to next year's first sem
            switch (currentYear) {
                case "1st Year": nextYear = "2nd Year"; break;
                case "2nd Year": nextYear = "3rd Year"; break;
                case "3rd Year": nextYear = "4th Year"; break;
                case "4th Year":
                    // What happens after 4th year, 2nd sem? Graduation or stop?
                    System.out.println("Student has completed 4th Year, 2nd Semester.");
                    return null; // Or handle graduation state
                default:
                    System.err.println("Unknown current year level: " + currentYear);
                    return null; // Cannot determine next year
            }
        } else if (currentSemester.equals("Summer")) {
            // Assuming Summer comes after 2nd Sem, leads into next year's 1st Sem
            // This might need adjustment based on specific rules
            nextSemester = "1st Semester";
            switch (currentYear) {
                // Assuming summer might happen after 1st, 2nd, or 3rd year
                case "1st Year": nextYear = "2nd Year"; break;
                case "2nd Year": nextYear = "3rd Year"; break;
                case "3rd Year": nextYear = "4th Year"; break;
                case "4th Year": // Unlikely to have summer after 4th year 2nd sem
                    System.out.println("Cannot determine term after 4th Year Summer.");
                    return null;
                default:
                    System.err.println("Unknown current year level for Summer: " + currentYear);
                    return null;
            }
        } else {
            System.err.println("Unknown current semester: " + currentSemester);
            return null; // Cannot determine next term
        }

        return new String[]{nextYear, nextSemester};
    }

    /**
     * Gets a set of subject codes that the student has successfully passed.
     * Uses the pre-loaded studentEnrollments list.
     * @return A Set containing the codes of all passed subjects (grade <= 3.0).
     */
    private Set<String> getCompletedSubjectCodes() {
        Set<String> completedCodes = new HashSet<>();
        if (studentEnrollments == null) {
            System.err.println("Cannot get completed subjects: Enrollment history not loaded.");
            return completedCodes; // Return empty set
        }

        double passingGradeThreshold = 3.0; // Define passing grade

        for (Enrollment enrollment : studentEnrollments) {
            if (enrollment.getSubjects() != null) {
                for (EnrolledSubject subject : enrollment.getSubjects()) {
                    // Check if grade is valid (not NG) and passing
                    if (subject.getGrade() > 0.0 && subject.getGrade() <= passingGradeThreshold) {
                        completedCodes.add(subject.getCode());
                    }
                }
            }
        }
        return completedCodes;
    }

    /**
     * Retrieves a list of subjects offered for a specific academic term.
     * Filters the master list of subjects loaded from DataStorage.
     * @param targetYear The target year level (e.g., "2nd Year").
     * @param targetSemester The target semester (e.g., "1st Semester").
     * @return A List of Subject objects offered in that term.
     */
    private List<Subject> getSubjectsForTerm(String targetYear, String targetSemester) {
        try {
            List<Subject> allSubjects = DataStorage.getAllSubjects(); // Get all subjects
            // Filter subjects based on year and semester
            return allSubjects.stream()
                    .filter(subject -> targetYear.equalsIgnoreCase(subject.getYearLevel()) &&
                            targetSemester.equalsIgnoreCase(subject.getSemester()))
                    .collect(Collectors.toList()); // Return the filtered list
        } catch (Exception e) {
            System.err.println("Error fetching subjects for term " + targetYear + "/" + targetSemester + ": " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    /**
     * Checks if all required prerequisite codes are present in the set of completed codes.
     * @param completedCodes A Set of subject codes the student has passed.
     * @param requiredCodes A List of prerequisite codes for a specific target subject.
     * @return true if all prerequisites are met or if there are no prerequisites, false otherwise.
     */
    private boolean checkPrerequisites(Set<String> completedCodes, List<String> requiredCodes) {
        if (requiredCodes == null || requiredCodes.isEmpty()) {
            return true; // No prerequisites to meet
        }
        if (completedCodes == null) {
            return false; // Cannot fulfill prerequisites if completed list is null
        }

        // Check if ALL required codes are contained within the completed codes set
        return completedCodes.containsAll(requiredCodes);
    }

    private Set<String> getFailedSubjectCodes() {
        Set<String> failedCodes = new HashSet<>();
        // Ensure enrollment history is loaded before calling this
        if (studentEnrollments == null) {
            System.err.println("Cannot get failed subjects: Enrollment history not loaded.");
            // Attempt to load it if needed, or handle the error appropriately
            loadAcademicHistory(); // Try loading it now
            if (studentEnrollments == null) return failedCodes; // Return empty if still null
        }

        double failingGradeThreshold = 3.0; // Grades strictly greater than this fail

        for (Enrollment enrollment : studentEnrollments) {
            if (enrollment.getSubjects() != null) {
                for (EnrolledSubject subject : enrollment.getSubjects()) {
                    // Check if grade is valid (not NG) and failing
                    if (subject.getGrade() > failingGradeThreshold) {
                        failedCodes.add(subject.getCode());
                    }
                }
            }
        }
        return failedCodes;
    }

    // --- MODIFIED handleEnroll Method (for testing eligibility logic) ---

    private void handleEnroll() {
        // 1. Calculate the next term
        String[] nextTerm = calculateNextTerm(currentStudent.getYearLevel(), currentStudent.getSemester());
        if (nextTerm == null) {
            showError("Enrollment Error", "Cannot determine the next academic term for enrollment.");
            return;
        }
        String nextYear = nextTerm[0];
        String nextSemester = nextTerm[1];
        System.out.println("Attempting enrollment for next term: " + nextYear + " - " + nextSemester);

        // 2. Get completed AND failed subject codes
        Set<String> completedCodes = getCompletedSubjectCodes();
        Set<String> failedCodes = getFailedSubjectCodes(); // *** NEW CALL ***
        System.out.println("Student has completed codes: " + completedCodes);
        System.out.println("Student has failed codes: " + failedCodes);

        // 3. Get subjects scheduled for the next term
        List<Subject> subjectsForNextTerm = getSubjectsForTerm(nextYear, nextSemester);
        if (subjectsForNextTerm.isEmpty() && failedCodes.isEmpty()) { // Check if no scheduled and no failed
            showInfo("Enrollment Info", "No subjects found offered or needing retake for the next term (" + nextYear + " - " + nextSemester + ").");
            return;
        }
        System.out.println("Subjects scheduled in next term: " + subjectsForNextTerm.stream().map(Subject::getCode).collect(Collectors.toList()));

        // 4. Combine scheduled subjects and relevant failed subjects
        List<Subject> potentialSubjectsForEnrollment = new ArrayList<>(subjectsForNextTerm);
        Set<String> potentialCodes = subjectsForNextTerm.stream().map(Subject::getCode).collect(Collectors.toSet());
        List<Subject> failedSubjectsToConsider = new ArrayList<>(); // Store the actual failed Subject objects

        for (String failedCode : failedCodes) {
            Optional<Subject> failedSubjectOpt = DataStorage.getSubjectByCode(failedCode);
            if (failedSubjectOpt.isPresent()) {
                Subject failedSubject = failedSubjectOpt.get();
                failedSubjectsToConsider.add(failedSubject); // Keep track of the failed subject object
                // Add failed subject IF its semester matches the next semester type
                // AND it's not already in the list from the scheduled subjects
                if (failedSubject.getSemester().equalsIgnoreCase(nextSemester) && !potentialCodes.contains(failedCode)) {
                    potentialSubjectsForEnrollment.add(failedSubject);
                    potentialCodes.add(failedCode); // Add to set to prevent duplicates if scheduled AND failed
                    System.out.println("Adding failed subject for retake (matches semester): " + failedCode);
                } else {
                    System.out.println("Skipping failed subject retake this term (semester mismatch): " + failedCode);
                }
            } else {
                System.err.println("Warning: Failed subject code " + failedCode + " not found in master subject list.");
            }
        }
        System.out.println("Potential subjects for enrollment: " + potentialCodes);


        // 5. Determine eligibility for the combined list
        List<Subject> eligibleSubjectsForDialog = new ArrayList<>();
        List<Subject> ineligibleSubjectsForDialog = new ArrayList<>();
        // List to specifically track which failed subjects MUST be retaken this term
        List<Subject> mandatoryRetakeSubjects = new ArrayList<>();

        for (Subject subject : potentialSubjectsForEnrollment) {
            List<String> prereqs = subject.getPrerequisites();
            boolean prerequisitesMet = checkPrerequisites(completedCodes, prereqs);
            boolean alreadyPassed = completedCodes.contains(subject.getCode());
            boolean isFailed = failedCodes.contains(subject.getCode());

            // Determine if this failed subject should be a mandatory retake THIS term
            boolean mustRetakeThisTerm = isFailed && subject.getSemester().equalsIgnoreCase(nextSemester);

            if (mustRetakeThisTerm) {
                // Failed subjects matching the term are usually eligible (assuming prereqs were met originally)
                // We make them mandatory retakes.
                mandatoryRetakeSubjects.add(subject);
                System.out.println("  -> Mandatory Retake: " + subject.getCode() + " " + subject.getName());
            } else if (prerequisitesMet && !alreadyPassed && !isFailed) {
                // Standard eligible subject
                eligibleSubjectsForDialog.add(subject);
                System.out.println("  -> Eligible for: " + subject.getCode() + " " + subject.getName());
            } else if (!alreadyPassed) { // Don't list ineligible if already passed
                // Ineligible subject (either prereqs not met or failed but wrong semester)
                ineligibleSubjectsForDialog.add(subject);
                String reason = alreadyPassed ? " (Already Passed)" :
                        !prerequisitesMet ? " (Prerequisites not met: " + prereqs + ")" :
                                isFailed ? " (Failed - Retake in " + subject.getSemester() + ")" : "";
                System.out.println("  -> Ineligible for: " + subject.getCode() + " " + subject.getName() + reason);
            }
        }

        // --- Launch Enrollment Dialog ---
        // Pass the different categories of subjects to the dialog
        EnrollmentDialog enrollmentDialog = new EnrollmentDialog(
                this,                       // Parent frame
                currentStudent,             // The student
                nextYear,                   // Calculated next year
                nextSemester,               // Calculated next semester
                eligibleSubjectsForDialog,  // Subjects student can CHOOSE to take
                ineligibleSubjectsForDialog,// Subjects student CANNOT take (for info)
                completedCodes,             // Set of completed codes (dialog might use for display)
                mandatoryRetakeSubjects // Pass this new list
        );
        enrollmentDialog.setVisible(true);

        // Refresh dashboard if enrollment was successful
        if (enrollmentDialog.isEnrollmentSuccessful()) {
            loadCurrentStudentData(); // Refresh student's term display
            loadAcademicHistory();   // Refresh grade display
            updateProgressSummary(); // Refresh summary
        }
    }

    private void handleEditProfile() {
        // Create and show the EditStudentProfileDialog
        EditStudentProfileDialog profileDialog = new EditStudentProfileDialog(this, currentStudent); // Pass the current student object
        profileDialog.setVisible(true);

        // After the dialog closes, check if data was saved and refresh
        if (profileDialog.isSaved()) {
            // Reload student data to reflect potential changes (like email)
            // Note: We don't need to reload password here, just the student details
            String studentId = currentStudent.getId(); // Get ID before potentially reloading
            if (loadCurrentStudentData()) { // loadCurrentStudentData re-fetches from students.json
                // Update display labels if necessary
                studentEmailLabel.setText("Email: " + currentStudent.getEmail());
                // studentNameLabel might need update if name becomes editable later
                System.out.println("Student profile data refreshed after edit.");
            } else {
                // Handle error if student data couldn't be reloaded
                showError("Error", "Could not reload student data for ID " + studentId + " after profile update.");
                // Might need to force logout or close dashboard if data is inconsistent
            }
        }
    }

    // Helper method for showing info messages (add if not already present)
    private void showInfo(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}