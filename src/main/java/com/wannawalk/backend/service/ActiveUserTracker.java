package com.wannawalk.backend.service;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class ActiveUserTracker {
    private final Set<String> activeUserIds = Collections.synchronizedSet(new HashSet<>());

    public void userConnected(String userId) {
        activeUserIds.add(userId);
        System.out.println("User connected: " + userId + ". Total active users: " + activeUserIds.size());
    }

    public void userDisconnected(String userId) {
        activeUserIds.remove(userId);
        System.out.println("User disconnected: " + userId + ". Total active users: " + activeUserIds.size());
    }

    public boolean isUserActive(String userId) {
        return activeUserIds.contains(userId);
    }
}