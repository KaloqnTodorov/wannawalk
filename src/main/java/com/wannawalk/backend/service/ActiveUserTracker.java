package com.wannawalk.backend.service;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveUserTracker {

    /**
     * A nested static class to hold the detailed presence state for each user.
     */
    @Getter
    @Setter
    private static class UserPresenceState {
        private boolean isActiveInChat; // Is the user actively viewing a chat screen?
        private String activeChatWith;  // The ID of the user they are currently chatting with.

        UserPresenceState() {
            this.isActiveInChat = false;
            this.activeChatWith = null;
        }
    }

    // Switched from a Set to a Map to store the detailed presence state for each user.
    private final Map<String, UserPresenceState> activeUsers = new ConcurrentHashMap<>();

    public void userConnected(String userId) {
        // When a user connects, we add them with a default 'inactive' state.
        activeUsers.put(userId, new UserPresenceState());
        System.out.println("User connected: " + userId + ". Total active users: " + activeUsers.size());
    }

    public void userDisconnected(String userId) {
        // When a user disconnects, we remove their entire presence record.
        activeUsers.remove(userId);
        System.out.println("User disconnected: " + userId + ". Total active users: " + activeUsers.size());
    }

    /**
     * NEW METHOD: Updates the user's detailed presence state based on messages from the client.
     * @param userId The ID of the user updating their status.
     * @param isActive True if the user is in the foreground on a chat screen.
     * @param chatWith The ID of the user they are chatting with.
     */
    public void updateUserPresence(String userId, boolean isActive, String chatWith) {
        UserPresenceState state = activeUsers.get(userId);
        if (state != null) {
            state.setActiveInChat(isActive);
            // If they are becoming active, store who they are chatting with.
            // If they are becoming inactive, clear it.
            state.setActiveChatWith(isActive ? chatWith : null);
        }
    }

    /**
     * NEW METHOD: Checks if a recipient is actively viewing a chat with a specific sender.
     * This is the core logic for deciding whether to send a push notification.
     * @param recipientId The user receiving the message.
     * @param senderId The user sending the message.
     * @return True if the recipient is active in the chat with the sender.
     */
    public boolean isUserActiveInChat(String recipientId, String senderId) {
        UserPresenceState recipientState = activeUsers.get(recipientId);

        // The user is considered "active in this chat" if:
        // 1. We have a presence record for them.
        // 2. Their status is 'isActiveInChat'.
        // 3. The chat screen they are viewing is with the sender.
        return recipientState != null &&
               recipientState.isActiveInChat() &&
               senderId.equals(recipientState.getActiveChatWith());
    }

    /**
     * MODIFIED METHOD: Now just checks if the user has an active WebSocket session.
     * @param userId The ID of the user to check.
     * @return True if the user is connected.
     */
    public boolean isUserActive(String userId) {
        return activeUsers.containsKey(userId);
    }
}
