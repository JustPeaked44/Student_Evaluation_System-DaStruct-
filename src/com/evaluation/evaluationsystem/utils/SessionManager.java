package com.evaluation.evaluationsystem.utils;

public class SessionManager {
    private static String currentUserId;
    private static String currentUserRole;

    public static void loginUser(String userId, String role) {
        currentUserId = userId;
        currentUserRole = role;
        System.out.println("Session started for User ID: " + userId + ", Role: " + role);
    }

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUserRole() {
        return currentUserRole;
    }

    public static void logout() {
        System.out.println("Session ended for User ID: " + currentUserId);
        currentUserId = null;
        currentUserRole = null;
    }

    public static boolean isLoggedIn() {
        return currentUserId != null && currentUserRole != null;
    }
}