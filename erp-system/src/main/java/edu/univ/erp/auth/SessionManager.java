package edu.univ.erp.auth;

import edu.univ.erp.domain.User;

public final class SessionManager {

    private static User currentUser;

    private SessionManager() {
        // Utility class
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
