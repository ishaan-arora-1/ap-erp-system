package edu.univ.erp.service;

import edu.univ.erp.domain.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationService {
    
    // In-memory storage: UserId -> List of Messages
    private static final Map<Integer, List<String>> userNotifications = new HashMap<>();

    public static void addNotification(User user, String message) {
        if (user == null) return;
        
        int userId = user.getUserId();
        userNotifications.putIfAbsent(userId, new ArrayList<>());
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String fullMessage = "[" + timestamp + "] " + message;
        
        userNotifications.get(userId).add(0, fullMessage); // Add to top
    }

    public static List<String> getNotifications(User user) {
        if (user == null) return Collections.emptyList();
        return userNotifications.getOrDefault(user.getUserId(), Collections.emptyList());
    }
    
    public static void clearNotifications(User user) {
        if (user != null) {
            userNotifications.remove(user.getUserId());
        }
    }
}

