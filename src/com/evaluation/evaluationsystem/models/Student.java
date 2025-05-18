package com.evaluation.evaluationsystem.models;

public class Student {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String yearLevel;
    private String semester;
    // Add other relevant fields if needed

    public Student(String id, String firstName, String lastName, String email, String yearLevel, String semester) {
        // Add validation if desired
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.yearLevel = yearLevel;
        this.semester = semester;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getYearLevel() { return yearLevel; }
    public String getSemester() { return semester; }

    // --- Setters --- (Optional, depending on if you allow modification)
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }
    public void setSemester(String semester) { this.semester = semester; }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + id + ")";
    }
}