package com.evaluation.evaluationsystem.models;

public class EnrolledSubject {
    private String code;
    private String name;
    private int units;
    private double grade; // 0.0 initially or if ungraded

    public EnrolledSubject(String code, String name, int units, double grade) {
        this.code = code;
        this.name = name;
        this.units = units;
        this.grade = grade;
    }

    // --- Getters and Setters ---
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getUnits() { return units; }
    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    public String getStatus() {
        if (grade == 0.0) return "In Progress";
        return grade <= 3.0 ? "Passed" : "Failed";
    }
}