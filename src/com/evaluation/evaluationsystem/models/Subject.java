package com.evaluation.evaluationsystem.models;

import java.util.ArrayList;
import java.util.List;

public class Subject {
    private String code;
    private String name;
    private int units; // Simplified to total units for now
    private String department;
    private String yearLevel;
    private String semester;
    private List<String> prerequisites;

    public Subject(String code, String name, int units, String department, String yearLevel, String semester, List<String> prerequisites) {
        this.code = code;
        this.name = name;
        this.units = units;
        this.department = department;
        this.yearLevel = yearLevel;
        this.semester = semester;
        this.prerequisites = prerequisites != null ? new ArrayList<>(prerequisites) : new ArrayList<>();
    }

    // --- Getters ---
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getUnits() { return units; }
    public String getDepartment() { return department; }
    public String getYearLevel() { return yearLevel; }
    public String getSemester() { return semester; }
    public List<String> getPrerequisites() { return prerequisites; }

    // --- Setters --- (If needed)
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites != null ? new ArrayList<>(prerequisites) : new ArrayList<>(); }


    @Override
    public String toString() {
        return code + " - " + name;
    }
}