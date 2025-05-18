package com.evaluation.evaluationsystem.models; // Correct package

import java.util.ArrayList;
import java.util.List;

public class Teacher {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String position;
    // Changed from String to List<String>
    private List<String> assignedSubjectCodes;

    // Constructor updated to accept a List
    public Teacher(String id, String firstName, String lastName, String email,
                   String department, String position, List<String> assignedSubjectCodes) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.position = position;
        // Initialize with a new list to ensure it's mutable and prevent external modification issues
        this.assignedSubjectCodes = (assignedSubjectCodes != null) ? new ArrayList<>(assignedSubjectCodes) : new ArrayList<>();
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    // Getter returns the list
    public List<String> getAssignedSubjectCodes() { return assignedSubjectCodes; }

    // --- Setters (Optional - Add if needed for modification outside constructor) ---
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setDepartment(String department) { this.department = department; }
    public void setPosition(String position) { this.position = position; }
    // Setter for the list
    public void setAssignedSubjectCodes(List<String> assignedSubjectCodes) {
        this.assignedSubjectCodes = (assignedSubjectCodes != null) ? new ArrayList<>(assignedSubjectCodes) : new ArrayList<>();
    }


    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + id + ")";
    }
}