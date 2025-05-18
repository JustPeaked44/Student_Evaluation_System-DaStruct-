package com.evaluation.evaluationsystem.models;

import java.util.ArrayList;
import java.util.List;

public class Enrollment {
    private String studentId;
    private String yearLevel;
    private String semester;
    private String status; // "Enrolled", "Completed", "Failed", "In Progress"
    private List<EnrolledSubject> subjects;

    public Enrollment(String studentId, String yearLevel, String semester, String status, List<EnrolledSubject> subjects) {
        this.studentId = studentId;
        this.yearLevel = yearLevel;
        this.semester = semester;
        this.status = status;
        this.subjects = subjects != null ? new ArrayList<>(subjects) : new ArrayList<>();
    }

    // --- Getters and Setters ---
    public String getStudentId() { return studentId; }
    public String getYearLevel() { return yearLevel; }
    public String getSemester() { return semester; }
    public String getStatus() { return status; }
    public List<EnrolledSubject> getSubjects() { return subjects; }
    public void setStatus(String status) { this.status = status; }
    public void setSubjects(List<EnrolledSubject> subjects) { this.subjects = subjects; }
}