package com.evaluation.evaluationsystem.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JsonUtils {

    // Method to get the absolute path to the data file
    private static String getDataFilePath(String filename) {
        // Assumes the 'data' folder is in the project root
        Path path = Paths.get("data", filename);
        return path.toAbsolutePath().toString();
    }

    // Load JSON data from file
    public static JSONObject loadJSON(String filename) {
        JSONParser parser = new JSONParser();
        String filePath = getDataFilePath(filename);
        try (FileReader reader = new FileReader(filePath)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONObject) {
                return (JSONObject) obj;
            } else {
                System.err.println("Warning: Root of " + filename + " is not a JSON object. Returning empty object.");
                return new JSONObject(); // Return empty if not an object or file is empty/invalid format
            }
        } catch (IOException e) {
            System.err.println("IOException reading " + filePath + ": " + e.getMessage() + ". Returning empty JSON object.");
            return new JSONObject(); // Return empty JSON if file is missing or other IO error
        } catch (ParseException e) {
            System.err.println("ParseException parsing " + filePath + ": " + e.getMessage() + ". Returning empty JSON object.");
            return new JSONObject(); // Return empty JSON on parsing error
        }
    }

    // Save JSON data to file with basic indentation (json-simple doesn't have built-in pretty print)
    public static void saveJSON(String filename, JSONObject jsonData) {
        String filePath = getDataFilePath(filename);
        try (FileWriter file = new FileWriter(filePath)) {
            // Basic pretty printing (can be improved)
            file.write(toPrettyFormat(jsonData.toJSONString(), 4));
            file.flush();
        } catch (IOException e) {
            System.err.println("IOException writing to " + filePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Simple pretty print helper
    private static String toPrettyFormat(String jsonString, int indent) {
        // This is a very basic implementation. For robust pretty printing,
        // consider using a library like Gson or Jackson if allowed.
        StringBuilder prettyJson = new StringBuilder();
        String indentStr = " ".repeat(indent);
        int level = 0;
        boolean inQuote = false;

        for (char c : jsonString.toCharArray()) {
            switch (c) {
                case '{':
                case '[':
                    prettyJson.append(c).append("\n");
                    level++;
                    appendIndent(prettyJson, level, indentStr);
                    break;
                case '}':
                case ']':
                    prettyJson.append("\n");
                    level--;
                    appendIndent(prettyJson, level, indentStr);
                    prettyJson.append(c);
                    break;
                case ',':
                    prettyJson.append(c).append("\n");
                    appendIndent(prettyJson, level, indentStr);
                    break;
                case ':':
                    prettyJson.append(c).append(" ");
                    break;
                case '"':
                    prettyJson.append(c);
                    inQuote = !inQuote;
                    break;
                default:
                    prettyJson.append(c);
                    break;
            }
        }
        return prettyJson.toString();
    }

    private static void appendIndent(StringBuilder sb, int level, String indentStr) {
        for (int i = 0; i < level; i++) {
            sb.append(indentStr);
        }
    }

    // --- Convenience methods for specific files ---
    public static JSONObject loadUsers() { return loadJSON("users.json"); }
    public static void saveUsers(JSONObject data) { saveJSON("users.json", data); }

    public static JSONObject loadStudents() { return loadJSON("students.json"); }
    public static void saveStudents(JSONObject data) { saveJSON("students.json", data); }

    public static JSONObject loadTeachers() { return loadJSON("teachers.json"); }
    public static void saveTeachers(JSONObject data) { saveJSON("teachers.json", data); }

    public static JSONObject loadSubjects() { return loadJSON("subjects.json"); }
    public static void saveSubjects(JSONObject data) { saveJSON("subjects.json", data); }

    public static JSONObject loadEnrollments() { return loadJSON("enrollments.json"); }
    public static void saveEnrollments(JSONObject data) { saveJSON("enrollments.json", data); }

    // --- Safe extraction methods ---
    public static JSONArray getJSONArray(JSONObject obj, String key) {
        Object arrayObj = obj.get(key);
        if (arrayObj instanceof JSONArray) {
            return (JSONArray) arrayObj;
        }
        return new JSONArray(); // Return empty array if key not found or not an array
    }

    public static String getString(JSONObject obj, String key, String defaultValue) {
        Object value = obj.get(key);
        return (value instanceof String) ? (String) value : defaultValue;
    }

    public static int getInt(JSONObject obj, String key, int defaultValue) {
        Object value = obj.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        // Try parsing if it's a string representation of a number
        if (value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (NumberFormatException e) {
                // Ignore parse error, return default
            }
        }
        return defaultValue;
    }

    public static double getDouble(JSONObject obj, String key, double defaultValue) {
        Object value = obj.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        // Try parsing if it's a string representation of a number
        if (value instanceof String) {
            try {
                return Double.parseDouble((String)value);
            } catch (NumberFormatException e) {
                // Ignore parse error, return default
            }
        }
        return defaultValue;
    }
}