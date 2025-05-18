package com.evaluation.evaluationsystem.ui; // Corrected package

import com.evaluation.evaluationsystem.data.DataStorage;
import com.evaluation.evaluationsystem.models.EnrolledSubject;
import com.evaluation.evaluationsystem.models.Enrollment;
import com.evaluation.evaluationsystem.models.Student;
import com.evaluation.evaluationsystem.models.Subject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import javax.swing.border.LineBorder; // For highlighting mandatory items
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * EnrollmentDialog Class
 * Purpose: Creates a pop-up window (JDialog) where students can select eligible
 * subjects and are shown mandatory retake subjects for their next academic term.
 */
public class EnrollmentDialog extends JDialog {

    // --- Constants ---
    private static final int MAX_UNITS_PER_SEMESTER = 26;

    // --- Data Passed In ---
    private final Student currentStudent;
    private final String nextYear;
    private final String nextSemester;
    private final List<Subject> eligibleSubjects;   // Subjects student CAN choose
    private final List<Subject> mandatoryRetakeSubjects; // Subjects student MUST take
    private final List<Subject> ineligibleSubjects; // Subjects student CANNOT take (for info display)
    private final Set<String> completedSubjectCodes;

    // --- UI Components ---
    // Combined list for display, using renderer to differentiate
    private JList<Subject> subjectsDisplayList;
    private DefaultListModel<Subject> subjectsDisplayModel;
    private JTextArea detailsTextArea;
    private JLabel selectedUnitsLabel;
    private JLabel maxUnitsLabel;
    private JLabel statusLabel;
    private JButton enrollButton;
    private JButton cancelButton;

    // --- State ---
    private int currentSelectedUnits = 0;
    private int mandatoryUnits = 0; // Units from mandatory subjects
    private boolean enrollmentSuccessful = false;

    /**
     * Constructor for the EnrollmentDialog.
     */
    public EnrollmentDialog(Frame parent, Student student, String nextYear, String nextSemester,
                            List<Subject> eligible, List<Subject> ineligible, Set<String> completedCodes,
                            List<Subject> mandatoryRetakes) { // Added mandatoryRetakes parameter
        super(parent, "Enrollment for " + nextYear + " - " + nextSemester, true);
        this.currentStudent = student;
        this.nextYear = nextYear;
        this.nextSemester = nextSemester;
        // Store copies of the lists
        this.eligibleSubjects = eligible != null ? new ArrayList<>(eligible) : new ArrayList<>();
        this.ineligibleSubjects = ineligible != null ? new ArrayList<>(ineligible) : new ArrayList<>();
        this.completedSubjectCodes = completedCodes;
        this.mandatoryRetakeSubjects = mandatoryRetakes != null ? new ArrayList<>(mandatoryRetakes) : new ArrayList<>();

        // Sort lists for display consistency
        this.eligibleSubjects.sort(Comparator.comparing(Subject::getCode));
        this.mandatoryRetakeSubjects.sort(Comparator.comparing(Subject::getCode));
        // We might not display ineligible directly, but sorting is good practice
        this.ineligibleSubjects.sort(Comparator.comparing(Subject::getCode));

        setSize(700, 600); // Slightly taller for potentially more info
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        initComponents();
        layoutComponents();
        addListeners();
        populateSubjectList(); // Populate the combined list
        preselectMandatorySubjects(); // Select mandatory ones
        updateSelectedUnits(); // Calculate initial unit count including mandatory
        updateDetailsArea();   // Show initial message
    }

    /**
     * Initializes Swing components.
     */
    private void initComponents() {
        // Combined List Model and JList
        subjectsDisplayModel = new DefaultListModel<>();
        subjectsDisplayList = new JList<>(subjectsDisplayModel);
        subjectsDisplayList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        // Apply custom renderer to visually distinguish subject types
        subjectsDisplayList.setCellRenderer(new EnrollmentSubjectListRenderer(mandatoryRetakeSubjects));
        subjectsDisplayList.setVisibleRowCount(12); // Increased rows

        detailsTextArea = new JTextArea(10, 30);
        detailsTextArea.setEditable(false);
        detailsTextArea.setLineWrap(true);
        detailsTextArea.setWrapStyleWord(true);
        detailsTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailsTextArea.setBorder(BorderFactory.createEtchedBorder());

        selectedUnitsLabel = new JLabel("Selected Units: 0");
        selectedUnitsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        maxUnitsLabel = new JLabel("/ Max Units: " + MAX_UNITS_PER_SEMESTER);
        maxUnitsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        enrollButton = new JButton("Enroll in Selected (+ Mandatory) Subjects"); // Updated text
        cancelButton = new JButton("Cancel");

    }

    /**
     * Populates the combined JList with mandatory and eligible subjects.
     */
    private void populateSubjectList() {
        subjectsDisplayModel.clear();

        // Add mandatory subjects first
        if (!mandatoryRetakeSubjects.isEmpty()) {
            // Optionally add a separator or header visually later via renderer
            for (Subject subject : mandatoryRetakeSubjects) {
                subjectsDisplayModel.addElement(subject);
            }
        }

        // Add eligible subjects
        for (Subject subject : eligibleSubjects) {
            // Avoid adding if it somehow also ended up in mandatory list (shouldn't happen with correct logic)
            if (!mandatoryRetakeSubjects.contains(subject)) {
                subjectsDisplayModel.addElement(subject);
            }
        }
    }

    /**
     * Automatically selects the mandatory subjects in the list and calculates their units.
     */
    private void preselectMandatorySubjects() {
        List<Integer> indicesToSelect = new ArrayList<>();
        mandatoryUnits = 0; // Reset mandatory units count

        for (int i = 0; i < subjectsDisplayModel.getSize(); i++) {
            Subject subject = subjectsDisplayModel.getElementAt(i);
            if (mandatoryRetakeSubjects.contains(subject)) {
                indicesToSelect.add(i);
                mandatoryUnits += subject.getUnits(); // Add to mandatory unit count
            }
        }

        if (!indicesToSelect.isEmpty()) {
            // Convert List<Integer> to int[] for setSelectionInterval
            int[] indicesArray = indicesToSelect.stream().mapToInt(i -> i).toArray();
            // Select all mandatory subjects; setSelectionInterval selects a contiguous range,
            // so we use setSelectedIndices for potentially non-contiguous mandatory items.
            subjectsDisplayList.setSelectedIndices(indicesArray);
        }
        // Initial unit calculation will be done by updateSelectedUnits called after this
    }


    /**
     * Arranges components using layout managers.
     */
    private void layoutComponents() {
        // Layout remains largely the same as before, just ensure titles are clear
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel unitsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        unitsPanel.add(selectedUnitsLabel);
        unitsPanel.add(maxUnitsLabel);
        unitsPanel.add(Box.createHorizontalStrut(20));
        unitsPanel.add(statusLabel);
        mainPanel.add(unitsPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.6; gbc.weighty = 1.0;
        JScrollPane listScrollPane = new JScrollPane(subjectsDisplayList);
        listScrollPane.setBorder(new TitledBorder("Select Subjects (Mandatory are pre-selected)")); // Updated title
        centerPanel.add(listScrollPane, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.4; gbc.weighty = 1.0;
        JScrollPane detailsScrollPane = new JScrollPane(detailsTextArea);
        detailsScrollPane.setBorder(new TitledBorder("Subject Details"));
        centerPanel.add(detailsScrollPane, gbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(enrollButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    /**
     * Adds event listeners to interactive components.
     */
    private void addListeners() {
        // Listener for list selection changes
        subjectsDisplayList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                // Prevent deselection of mandatory subjects
                ensureMandatorySubjectsSelected();
                updateSelectedUnits();
                updateDetailsArea();
            }
        });
        enrollButton.addActionListener(e -> performEnrollment());
        cancelButton.addActionListener(e -> dispose());
    }

    /**
     * Ensures that mandatory subjects remain selected in the JList.
     */
    private void ensureMandatorySubjectsSelected() {
        ListSelectionModel selectionModel = subjectsDisplayList.getSelectionModel();
        int[] currentSelectedIndices = selectionModel.getSelectedIndices();

        // --- Calculate the list of indices that *should* be selected ---
        List<Integer> finalIndicesToSelect = new ArrayList<>();
        boolean mandatoryWasMissing = false; // Flag to track if we needed to force-select a mandatory item

        // 1. Add all mandatory indices
        List<Integer> mandatoryIndicesList = new ArrayList<>(); // Store mandatory indices separately
        for (int i = 0; i < subjectsDisplayModel.getSize(); i++) {
            Subject subject = subjectsDisplayModel.getElementAt(i);
            if (mandatoryRetakeSubjects.contains(subject)) {
                finalIndicesToSelect.add(i);
                mandatoryIndicesList.add(i); // Keep track of mandatory ones
            }
        }

        // 2. Add user's non-mandatory selections
        for (int index : currentSelectedIndices) {
            // Check if this index corresponds to a non-mandatory subject
            boolean isMandatory = false;
            if (index >= 0 && index < subjectsDisplayModel.getSize()) { // Bounds check
                isMandatory = mandatoryRetakeSubjects.contains(subjectsDisplayModel.getElementAt(index));
            }

            // Add if it's not mandatory AND not already in our final list
            if (!isMandatory && !finalIndicesToSelect.contains(index)) {
                finalIndicesToSelect.add(index);
            }
        }

        // 3. Check if any mandatory index was missing from the original selection
        for (int mandatoryIndex : mandatoryIndicesList) {
            boolean found = false;
            for (int currentIdx : currentSelectedIndices) {
                if (mandatoryIndex == currentIdx) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                mandatoryWasMissing = true;
                break;
            }
        }

        // --- Apply the correction ONLY if needed ---
        // Convert final list and current list to sorted arrays for comparison
        int[] finalSelectionArray = finalIndicesToSelect.stream().mapToInt(i -> i).sorted().toArray();
        int[] currentSorted = Arrays.stream(currentSelectedIndices).sorted().toArray();

        // Update if a mandatory item was missing OR if the overall selection differs
        if (mandatoryWasMissing || !Arrays.equals(finalSelectionArray, currentSorted)) {
            System.out.println("Correcting selection. Mandatory missing: " + mandatoryWasMissing); // Debugging line

            // Temporarily disable listener events during programmatic update
            selectionModel.setValueIsAdjusting(true);
            subjectsDisplayList.setSelectedIndices(finalSelectionArray);
            selectionModel.setValueIsAdjusting(false);

            // It's generally safer to manually call update methods after a programmatic
            // change, as listener behavior can sometimes be tricky.
            updateSelectedUnits();
            updateDetailsArea();
        }
    }


    /**
     * Updates unit count and validates against the maximum limit.
     */
    private void updateSelectedUnits() {
        List<Subject> currentlySelectedInList = subjectsDisplayList.getSelectedValuesList();
        currentSelectedUnits = currentlySelectedInList.stream()
                .mapToInt(Subject::getUnits)
                .sum();

        selectedUnitsLabel.setText("Selected Units: " + currentSelectedUnits);

        if (currentSelectedUnits > MAX_UNITS_PER_SEMESTER) {
            statusLabel.setText("Error: Exceeds maximum allowed units!");
            enrollButton.setEnabled(false);
            selectedUnitsLabel.setForeground(Color.RED);
        } else if (currentSelectedUnits < mandatoryUnits || currentlySelectedInList.isEmpty()) {
            // Cannot enroll if total is less than mandatory units (implies not all mandatory selected)
            // or if the list is empty (though mandatory should prevent this)
            statusLabel.setText("Mandatory subjects must be included.");
            enrollButton.setEnabled(false);
            selectedUnitsLabel.setForeground(currentSelectedUnits == 0 ? Color.BLACK : Color.ORANGE); // Orange if some selected but not enough
        }
        else {
            statusLabel.setText(" ");
            enrollButton.setEnabled(true);
            selectedUnitsLabel.setForeground(Color.BLACK);
        }
    }

    /**
     * Displays details of the first selected subject in the text area.
     */
    private void updateDetailsArea() {
        List<Subject> selectedSubjects = subjectsDisplayList.getSelectedValuesList();
        if (selectedSubjects.isEmpty()) {
            detailsTextArea.setText("Select a subject from the list to see its details.");
        } else {
            Subject subject = selectedSubjects.get(0);
            StringBuilder details = new StringBuilder();
            details.append("Code: ").append(subject.getCode()).append("\n");
            details.append("Name: ").append(subject.getName()).append("\n");
            details.append("Units: ").append(subject.getUnits()).append("\n");
            details.append("Term: ").append(subject.getYearLevel()).append(" - ").append(subject.getSemester()).append("\n\n");

            // Determine status based on which list it came from originally
            if (mandatoryRetakeSubjects.contains(subject)) {
                details.append("Status: Mandatory Retake (Failed Previously)\n");
            } else if (eligibleSubjects.contains(subject)) {
                details.append("Status: Eligible\n");
            } else {
                details.append("Status: Ineligible (Check Prerequisites)\n"); // Should not happen if list only has eligible/mandatory
            }


            List<String> prereqs = subject.getPrerequisites();
            details.append("Prerequisites: ")
                    .append(prereqs == null || prereqs.isEmpty() ? "None" : String.join(", ", prereqs))
                    .append("\n");

            if (prereqs != null && !prereqs.isEmpty()) {
                boolean met = completedSubjectCodes.containsAll(prereqs);
                details.append("Prerequisites Met: ").append(met ? "Yes" : "NO").append("\n");
                if(!met){
                    List<String> missing = prereqs.stream()
                            .filter(p -> !completedSubjectCodes.contains(p))
                            .collect(Collectors.toList());
                    details.append("Missing: ").append(String.join(", ", missing)).append("\n");
                }
            }

            detailsTextArea.setText(details.toString());
            detailsTextArea.setCaretPosition(0);
        }
    }

    /**
     * Performs final validation and saves the enrollment.
     */
    private void performEnrollment() {
        // Get the final list of selected subjects (includes mandatory ones)
        List<Subject> finalSelectedSubjects = subjectsDisplayList.getSelectedValuesList();

        // --- Final Validation ---
        if (finalSelectedSubjects.isEmpty() && !mandatoryRetakeSubjects.isEmpty()) {
            showError("Enrollment Error", "Mandatory subjects must be included.");
            return;
        }
        if (finalSelectedSubjects.isEmpty()) {
            showError("Enrollment Error", "No subjects selected.");
            return;
        }
        if (currentSelectedUnits > MAX_UNITS_PER_SEMESTER) {
            showError("Enrollment Error", "Selected units (" + currentSelectedUnits + ") exceed the maximum allowed (" + MAX_UNITS_PER_SEMESTER + ").");
            return;
        }
        // Ensure all mandatory subjects are still in the final list (should be guaranteed by ensureMandatorySubjectsSelected)
        for(Subject mandatory : mandatoryRetakeSubjects) {
            if (!finalSelectedSubjects.contains(mandatory)) {
                showError("Enrollment Error", "Mandatory subject " + mandatory.getCode() + " was deselected.");
                return;
            }
        }


        // Prepare list of subjects to enroll with initial grade 0.0
        List<EnrolledSubject> subjectsToEnroll = finalSelectedSubjects.stream()
                .map(subject -> new EnrolledSubject(
                        subject.getCode(), subject.getName(), subject.getUnits(), 0.0))
                .collect(Collectors.toList());

        // Create the new enrollment record
        Enrollment newEnrollment = new Enrollment(
                currentStudent.getId(), this.nextYear, this.nextSemester,
                "Enrolled", subjectsToEnroll);

        try {
            DataStorage.saveEnrollment(newEnrollment);

            currentStudent.setYearLevel(this.nextYear);
            currentStudent.setSemester(this.nextSemester);
            DataStorage.saveStudent(currentStudent);

            enrollmentSuccessful = true;
            JOptionPane.showMessageDialog(this,
                    "Successfully enrolled in " + subjectsToEnroll.size() + " subject(s) for " +
                            this.nextYear + " - " + this.nextSemester + ".",
                    "Enrollment Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (Exception ex) {
            showError("Enrollment Error", "An error occurred while saving enrollment: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Helper method to show error dialogs.
     */
    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Allows the calling frame to check if enrollment succeeded.
     */
    public boolean isEnrollmentSuccessful() {
        return enrollmentSuccessful;
    }

    // --- Custom ListCellRenderer to visually distinguish mandatory subjects ---
    private class EnrollmentSubjectListRenderer extends DefaultListCellRenderer {
        private final List<Subject> mandatorySubjects;
        private final Color mandatoryColor = new Color(255, 240, 240); // Light pinkish background
        private final Color mandatorySelectedColor = new Color(255, 180, 180); // Darker pinkish for selected mandatory
        private final Border mandatoryBorder = BorderFactory.createCompoundBorder(
                new LineBorder(Color.RED, 1), // Red border
                new EmptyBorder(2, 2, 2, 2)); // Padding inside border

        public EnrollmentSubjectListRenderer(List<Subject> mandatorySubjects) {
            this.mandatorySubjects = mandatorySubjects != null ? mandatorySubjects : new ArrayList<>();
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {

            // Get default component (JLabel)
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(new EmptyBorder(2, 5, 2, 5)); // Add some padding

            if (value instanceof Subject) {
                Subject subject = (Subject) value;
                label.setText(String.format("%s - %s (%d Units)",
                        subject.getCode(), subject.getName(), subject.getUnits()));

                boolean isMandatory = mandatorySubjects.contains(subject);

                // Apply visual cues for mandatory subjects
                if (isMandatory) {
                    label.setFont(label.getFont().deriveFont(Font.BOLD)); // Make mandatory bold
                    label.setToolTipText("Mandatory Retake - Cannot be deselected");
                    // Apply background color and border
                    label.setOpaque(true); // Required for background color to show
                    label.setBackground(isSelected ? mandatorySelectedColor : mandatoryColor);
                    label.setBorder(mandatoryBorder);
                    // Ensure foreground is readable on the pink background
                    label.setForeground(isSelected ? Color.WHITE : Color.BLACK);

                } else {
                    // Regular eligible subject
                    label.setFont(label.getFont().deriveFont(Font.PLAIN)); // Normal font
                    label.setToolTipText("Eligible to enroll");
                    label.setOpaque(true); // Must set opaque for default background too
                    // Reset to default colors/border if reusing component
                    label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                    label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                    label.setBorder(new EmptyBorder(2, 5, 2, 5)); // Reset border to padding only
                }

            } else {
                label.setText(value == null ? "" : value.toString());
                label.setOpaque(true);
                label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
                label.setBorder(new EmptyBorder(2, 5, 2, 5));
            }

            return label;
        }
    }
}