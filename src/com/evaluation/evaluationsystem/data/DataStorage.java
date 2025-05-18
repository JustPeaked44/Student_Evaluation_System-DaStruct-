package com.evaluation.evaluationsystem.data;

import com.evaluation.evaluationsystem.models.*;
import com.evaluation.evaluationsystem.utils.JsonUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class DataStorage {

    // --- User Management ---

    public static Optional<User> authenticateUser(String username, String password, String role) {
        JSONObject usersData = JsonUtils.loadUsers();
        JSONArray usersArray = JsonUtils.getJSONArray(usersData, "users");

        for (Object obj : usersArray) {
            JSONObject userJson = (JSONObject) obj;
            String storedUsername = JsonUtils.getString(userJson, "username", "");
            String storedPassword = JsonUtils.getString(userJson, "password", "");
            String storedRole = JsonUtils.getString(userJson, "role", "");

            if (storedUsername.equals(username) && storedPassword.equals(password) && storedRole.equalsIgnoreCase(role)) {
                return Optional.of(new User(storedUsername, storedPassword, storedRole));
            }
        }
        return Optional.empty();
    }

    public static void addUser(User user) {
        JSONObject usersData = JsonUtils.loadUsers();
        JSONArray usersArray = JsonUtils.getJSONArray(usersData, "users");

        // Check if user already exists
        boolean exists = usersArray.stream().anyMatch(obj ->
                JsonUtils.getString((JSONObject)obj, "username", "").equals(user.getUsername()));

        if (!exists) {
            JSONObject newUserJson = new JSONObject();
            newUserJson.put("username", user.getUsername());
            newUserJson.put("password", user.getPassword()); // In real app, hash the password!
            newUserJson.put("role", user.getRole());
            usersArray.add(newUserJson);
            usersData.put("users", usersArray);
            JsonUtils.saveUsers(usersData);
        } else {
            System.out.println("User already exists: " + user.getUsername());
            // Handle appropriately - maybe throw exception or return false
        }
    }

    // Add methods for updating/deleting users if needed

    // --- Student Management ---

    public static List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        JSONObject studentsData = JsonUtils.loadStudents();
        JSONArray studentsArray = JsonUtils.getJSONArray(studentsData, "students");

        for (Object obj : studentsArray) {
            JSONObject studentJson = (JSONObject) obj;
            students.add(new Student(
                    JsonUtils.getString(studentJson, "id", ""),
                    JsonUtils.getString(studentJson, "firstName", ""),
                    JsonUtils.getString(studentJson, "lastName", ""),
                    JsonUtils.getString(studentJson, "email", ""),
                    JsonUtils.getString(studentJson, "yearLevel", ""),
                    JsonUtils.getString(studentJson, "semester", "")
            ));
        }
        return students;
    }

    public static Optional<Student> getStudentById(String id) {
        return getAllStudents().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public static void saveStudent(Student student) {
        JSONObject studentsData = JsonUtils.loadStudents();
        JSONArray studentsArray = JsonUtils.getJSONArray(studentsData, "students");
        JSONObject studentJson = findJsonObjectById(studentsArray, "id", student.getId());

        if (studentJson != null) { // Update existing
            studentJson.put("firstName", student.getFirstName());
            studentJson.put("lastName", student.getLastName());
            studentJson.put("email", student.getEmail());
            studentJson.put("yearLevel", student.getYearLevel());
            studentJson.put("semester", student.getSemester());
        } else { // Add new
            studentJson = new JSONObject();
            studentJson.put("id", student.getId());
            studentJson.put("firstName", student.getFirstName());
            studentJson.put("lastName", student.getLastName());
            studentJson.put("email", student.getEmail());
            studentJson.put("yearLevel", student.getYearLevel());
            studentJson.put("semester", student.getSemester());
            studentsArray.add(studentJson);
        }
        studentsData.put("students", studentsArray);
        JsonUtils.saveStudents(studentsData);
    }

    public static void deleteStudent(String id) {
        JSONObject studentsData = JsonUtils.loadStudents();
        JSONArray studentsArray = JsonUtils.getJSONArray(studentsData, "students");
        studentsArray.removeIf(obj -> JsonUtils.getString((JSONObject) obj, "id", "").equals(id));
        studentsData.put("students", studentsArray);
        JsonUtils.saveStudents(studentsData);

        // Also delete associated user account and enrollments
        deleteUser(id);
        deleteEnrollmentsForStudent(id);
    }

    // Helper to delete user account
    private static void deleteUser(String username) {
        JSONObject usersData = JsonUtils.loadUsers();
        JSONArray usersArray = JsonUtils.getJSONArray(usersData, "users");
        usersArray.removeIf(obj -> JsonUtils.getString((JSONObject) obj, "username", "").equals(username));
        usersData.put("users", usersArray);
        JsonUtils.saveUsers(usersData);
    }

    // Helper to delete enrollments
    private static void deleteEnrollmentsForStudent(String studentId) {
        JSONObject enrollmentsData = JsonUtils.loadEnrollments();
        JSONArray enrollmentsArray = JsonUtils.getJSONArray(enrollmentsData, "enrollments");
        enrollmentsArray.removeIf(obj -> JsonUtils.getString((JSONObject) obj, "studentId", "").equals(studentId));
        enrollmentsData.put("enrollments", enrollmentsArray);
        JsonUtils.saveEnrollments(enrollmentsData);
    }

    public static List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        JSONObject teachersData = JsonUtils.loadTeachers();
        JSONArray teachersArray = JsonUtils.getJSONArray(teachersData, "teachers");

        for (Object obj : teachersArray) {
            JSONObject teacherJson = (JSONObject) obj;

            // --- MODIFIED PART: Handle list of subject codes ---
            JSONArray subjectsJson = JsonUtils.getJSONArray(teacherJson, "assignedSubjects"); // Use new key "assignedSubjects"
            List<String> subjectCodes = new ArrayList<>();
            if (subjectsJson != null) {
                for (Object codeObj : subjectsJson) {
                    if (codeObj instanceof String) { // Ensure it's a string
                        subjectCodes.add((String) codeObj);
                    }
                }
            }
            // --- END MODIFIED PART ---

            teachers.add(new Teacher(
                    JsonUtils.getString(teacherJson, "id", ""),
                    JsonUtils.getString(teacherJson, "firstName", ""),
                    JsonUtils.getString(teacherJson, "lastName", ""),
                    JsonUtils.getString(teacherJson, "email", ""),
                    JsonUtils.getString(teacherJson, "department", ""),
                    JsonUtils.getString(teacherJson, "position", ""),
                    subjectCodes // Pass the list of codes
            ));
        }
        return teachers;
    }

    public static Optional<Teacher> getTeacherById(String id) {
        return getAllTeachers().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst();
    }

    public static void saveTeacher(Teacher teacher) {
        JSONObject teachersData = JsonUtils.loadTeachers();
        JSONArray teachersArray = JsonUtils.getJSONArray(teachersData, "teachers");
        JSONObject teacherJson = findJsonObjectById(teachersArray, "id", teacher.getId());

        // --- MODIFIED PART: Convert List<String> to JSONArray ---
        JSONArray subjectsJson = new JSONArray();
        if (teacher.getAssignedSubjectCodes() != null) {
            subjectsJson.addAll(teacher.getAssignedSubjectCodes()); // Add all codes from the list
        }
        // --- END MODIFIED PART ---

        if (teacherJson != null) { // Update existing teacher
            teacherJson.put("firstName", teacher.getFirstName());
            teacherJson.put("lastName", teacher.getLastName());
            teacherJson.put("email", teacher.getEmail());
            teacherJson.put("department", teacher.getDepartment());
            teacherJson.put("position", teacher.getPosition());
            // Save the JSONArray under the key "assignedSubjects"
            teacherJson.put("assignedSubjects", subjectsJson); // Use new key
        } else { // Add new teacher
            teacherJson = new JSONObject();
            teacherJson.put("id", teacher.getId());
            teacherJson.put("firstName", teacher.getFirstName());
            teacherJson.put("lastName", teacher.getLastName());
            teacherJson.put("email", teacher.getEmail());
            teacherJson.put("department", teacher.getDepartment());
            teacherJson.put("position", teacher.getPosition());
            // Save the JSONArray under the key "assignedSubjects"
            teacherJson.put("assignedSubjects", subjectsJson); // Use new key
            teachersArray.add(teacherJson);
        }
        teachersData.put("teachers", teachersArray);
        JsonUtils.saveTeachers(teachersData);
    }

    public static void deleteTeacher(String id) {
        JSONObject teachersData = JsonUtils.loadTeachers();
        JSONArray teachersArray = JsonUtils.getJSONArray(teachersData, "teachers");
        teachersArray.removeIf(obj -> JsonUtils.getString((JSONObject) obj, "id", "").equals(id));
        teachersData.put("teachers", teachersArray);
        JsonUtils.saveTeachers(teachersData);
        // Also delete associated user account
        deleteUser(id);
    }

    // --- Subject Management --- (Implement similarly: getAll, getByCode, save, delete)
    public static List<Subject> getAllSubjects() {
        List<Subject> subjects = new ArrayList<>();
        JSONObject subjectsData = JsonUtils.loadSubjects();
        JSONArray subjectsArray = JsonUtils.getJSONArray(subjectsData, "subjects");

        for (Object obj : subjectsArray) {
            JSONObject subjectJson = (JSONObject) obj;
            JSONArray prereqJson = JsonUtils.getJSONArray(subjectJson, "prerequisites");
            List<String> prereqs = new ArrayList<>();
            for(Object prereq : prereqJson) {
                prereqs.add((String) prereq);
            }

            subjects.add(new Subject(
                    JsonUtils.getString(subjectJson, "code", ""),
                    JsonUtils.getString(subjectJson, "name", ""),
                    JsonUtils.getInt(subjectJson,"units", 0), // Assuming 'units' stores total units as a number or string
                    JsonUtils.getString(subjectJson, "department", ""),
                    JsonUtils.getString(subjectJson, "yearLevel", ""),
                    JsonUtils.getString(subjectJson, "semester", ""),
                    prereqs
            ));
        }
        return subjects;
    }

    public static Optional<Subject> getSubjectByCode(String code) {
        return getAllSubjects().stream()
                .filter(s -> s.getCode().equalsIgnoreCase(code)) // Case-insensitive compare for code
                .findFirst();
    }

    public static void saveSubject(Subject subject) {
        JSONObject subjectsData = JsonUtils.loadSubjects();
        JSONArray subjectsArray = JsonUtils.getJSONArray(subjectsData, "subjects");
        JSONObject subjectJson = findJsonObjectById(subjectsArray, "code", subject.getCode());

        // Create the JSON array for prerequisites
        JSONArray prereqsJson = new JSONArray();
        if (subject.getPrerequisites() != null) { // Check if the list exists
            for (String prereq : subject.getPrerequisites()) {
                prereqsJson.add(prereq); // Add each prerequisite string
            }
        }

        if (subjectJson != null) { // Update existing subject
            subjectJson.put("name", subject.getName());
            subjectJson.put("units", subject.getUnits());
            subjectJson.put("department", subject.getDepartment());
            subjectJson.put("yearLevel", subject.getYearLevel());
            subjectJson.put("semester", subject.getSemester());
            // Put the correctly created JSON array
            subjectJson.put("prerequisites", prereqsJson); // <--- CORRECTED LINE
        } else { // Add new subject
            subjectJson = new JSONObject();
            subjectJson.put("code", subject.getCode());
            subjectJson.put("name", subject.getName());
            subjectJson.put("units", subject.getUnits());
            subjectJson.put("department", subject.getDepartment());
            subjectJson.put("yearLevel", subject.getYearLevel());
            subjectJson.put("semester", subject.getSemester());
            // Put the correctly created JSON array
            subjectJson.put("prerequisites", prereqsJson); // <--- CORRECTED LINE
            subjectsArray.add(subjectJson);
        }
        subjectsData.put("subjects", subjectsArray);
        JsonUtils.saveSubjects(subjectsData);
    }

    public static void deleteSubject(String code) {
        JSONObject subjectsData = JsonUtils.loadSubjects();
        JSONArray subjectsArray = JsonUtils.getJSONArray(subjectsData, "subjects");
        subjectsArray.removeIf(obj -> JsonUtils.getString((JSONObject) obj, "code", "").equalsIgnoreCase(code));
        subjectsData.put("subjects", subjectsArray);
        JsonUtils.saveSubjects(subjectsData);
        // Consider removing this subject as a prerequisite from other subjects if needed
    }


    // --- Enrollment Management ---
    public static List<Enrollment> getAllEnrollments() {
        List<Enrollment> enrollments = new ArrayList<>();
        JSONObject enrollmentsData = JsonUtils.loadEnrollments();
        JSONArray enrollmentsArray = JsonUtils.getJSONArray(enrollmentsData, "enrollments");

        for (Object obj : enrollmentsArray) {
            JSONObject enrollJson = (JSONObject) obj;
            JSONArray subjectsJson = JsonUtils.getJSONArray(enrollJson, "subjects");
            List<EnrolledSubject> enrolledSubjects = new ArrayList<>();

            for (Object subjObj : subjectsJson) {
                JSONObject subjJson = (JSONObject) subjObj;
                enrolledSubjects.add(new EnrolledSubject(
                        JsonUtils.getString(subjJson, "code", ""),
                        JsonUtils.getString(subjJson, "name", ""),
                        JsonUtils.getInt(subjJson, "units", 0), // Get units as int
                        JsonUtils.getDouble(subjJson, "grade", 0.0)
                ));
            }

            enrollments.add(new Enrollment(
                    JsonUtils.getString(enrollJson, "studentId", ""),
                    JsonUtils.getString(enrollJson, "yearLevel", ""),
                    JsonUtils.getString(enrollJson, "semester", ""),
                    JsonUtils.getString(enrollJson, "status", "Enrolled"),
                    enrolledSubjects
            ));
        }
        return enrollments;
    }

    public static List<Enrollment> getEnrollmentsForStudent(String studentId) {
        return getAllEnrollments().stream()
                .filter(e -> e.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    public static void saveEnrollment(Enrollment enrollment) {
        JSONObject enrollmentsData = JsonUtils.loadEnrollments();
        JSONArray enrollmentsArray = JsonUtils.getJSONArray(enrollmentsData, "enrollments");

        // Find existing enrollment based on studentId, year, and semester
        JSONObject enrollJson = findEnrollmentJson(enrollmentsArray, enrollment.getStudentId(), enrollment.getYearLevel(), enrollment.getSemester());

        if (enrollJson != null) { // Update
            enrollJson.put("status", enrollment.getStatus());
            enrollJson.put("subjects", convertEnrolledSubjectsToJson(enrollment.getSubjects()));
        } else { // Add new
            enrollJson = new JSONObject();
            enrollJson.put("studentId", enrollment.getStudentId());
            enrollJson.put("yearLevel", enrollment.getYearLevel());
            enrollJson.put("semester", enrollment.getSemester());
            enrollJson.put("status", enrollment.getStatus());
            enrollJson.put("subjects", convertEnrolledSubjectsToJson(enrollment.getSubjects()));
            enrollmentsArray.add(enrollJson);
        }
        enrollmentsData.put("enrollments", enrollmentsArray);
        JsonUtils.saveEnrollments(enrollmentsData);
    }

    public static void updateSubjectGrade(String studentId, String subjectCode, double newGrade) {
        JSONObject enrollmentsData = JsonUtils.loadEnrollments();
        JSONArray enrollmentsArray = JsonUtils.getJSONArray(enrollmentsData, "enrollments");
        boolean updated = false;

        for (Object enrollObj : enrollmentsArray) {
            JSONObject enrollment = (JSONObject) enrollObj;
            if (JsonUtils.getString(enrollment, "studentId", "").equals(studentId)) {
                JSONArray subjectsArray = JsonUtils.getJSONArray(enrollment, "subjects");
                for (Object subjObj : subjectsArray) {
                    JSONObject subject = (JSONObject) subjObj;
                    if (JsonUtils.getString(subject, "code", "").equals(subjectCode)) {
                        subject.put("grade", newGrade);
                        updated = true;
                        break; // Found and updated the subject
                    }
                }
            }
            if(updated) break; // Found the student's enrollment
        }

        if (updated) {
            enrollmentsData.put("enrollments", enrollmentsArray);
            JsonUtils.saveEnrollments(enrollmentsData);
            System.out.println("Grade updated for student " + studentId + ", subject " + subjectCode);
        } else {
            System.out.println("Enrollment or subject not found for student " + studentId + ", subject " + subjectCode);
        }
    }


    // --- Helper Methods ---
    private static JSONObject findJsonObjectById(JSONArray array, String idKey, String idValue) {
        for (Object obj : array) {
            JSONObject jsonObj = (JSONObject) obj;
            if (idValue.equals(JsonUtils.getString(jsonObj, idKey, ""))) {
                return jsonObj;
            }
        }
        return null;
    }

    private static JSONObject findEnrollmentJson(JSONArray array, String studentId, String year, String semester) {
        for (Object obj : array) {
            JSONObject jsonObj = (JSONObject) obj;
            if (studentId.equals(JsonUtils.getString(jsonObj, "studentId", "")) &&
                    year.equals(JsonUtils.getString(jsonObj, "yearLevel", "")) &&
                    semester.equals(JsonUtils.getString(jsonObj, "semester", ""))) {
                return jsonObj;
            }
        }
        return null;
    }


    private static JSONArray convertEnrolledSubjectsToJson(List<EnrolledSubject> subjects) {
        JSONArray subjectsJson = new JSONArray();
        for (EnrolledSubject subject : subjects) {
            JSONObject subjJson = new JSONObject();
            subjJson.put("code", subject.getCode());
            subjJson.put("name", subject.getName());
            subjJson.put("units", subject.getUnits());
            subjJson.put("grade", subject.getGrade());
            subjectsJson.add(subjJson);
            
        }
        return subjectsJson;
    }

}