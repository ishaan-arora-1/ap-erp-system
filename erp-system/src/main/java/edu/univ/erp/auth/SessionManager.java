package edu.univ.erp.auth;

import edu.univ.erp.domain.User;

/**
 * SessionManager - Manages the current user's session in memory
 * 
 * Design: Simple in-memory session storage using static variable
 * - Single user per application instance (desktop application)
 * - Session lost when application closes (by design)
 * - Thread-safe not required (Swing is single-threaded)
 * 
 * Note: In a web application, this would use HTTP sessions or JWT tokens
 */
public final class SessionManager {

    // Currently logged-in user, null if no one is logged in
    private static User currentUser;

    /**
     * Private constructor prevents instantiation
     * This is a utility class with only static methods
     */
    private SessionManager() {
        // Utility class
    }

    /**
     * Store user in session after successful login
     * Called by LoginScreen after authentication succeeds
     * 
     * @param user The authenticated user object with role and ID
     */
    public static void login(User user) {
        currentUser = user;
    }

    /**
     * Clear the session (user logs out)
     * Called when user clicks logout button
     */
    public static void logout() {
        currentUser = null;
    }

    /**
     * Get the currently logged-in user
     * Used throughout the application to access user info (role, ID, username)
     * 
     * @return Current user object, or null if not logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if anyone is currently logged in
     * 
     * @return true if user is logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
