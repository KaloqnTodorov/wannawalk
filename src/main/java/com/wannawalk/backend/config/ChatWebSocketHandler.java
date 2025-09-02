package com.wannawalk.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wannawalk.backend.model.ChatMessage;
import com.wannawalk.backend.model.User;
import com.wannawalk.backend.dto.PresenceUpdatePayload;
import com.wannawalk.backend.dto.WebSocketMessage;
import com.wannawalk.backend.repository.ChatMessageRepository;
import com.wannawalk.backend.service.ActiveUserTracker;
import com.wannawalk.backend.service.NotificationService;
import com.wannawalk.backend.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private final ChatMessageRepository repo;
    private final ObjectMapper mapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ActiveUserTracker activeUserTracker;
    private final NotificationService notificationService;
    private final ProfileService profileService;

    public ChatWebSocketHandler(ChatMessageRepository repo, ActiveUserTracker activeUserTracker, NotificationService notificationService, ProfileService profileService) {
        this.repo = repo;
        this.activeUserTracker = activeUserTracker;
        this.notificationService = notificationService;
        this.profileService = profileService;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.put(userId, session);
            // NOTE: We now handle the 'active' state via presence updates,
            // but we can still track the general 'online' status here.
            activeUserTracker.userConnected(userId);
            logger.info("User {} connected. Total sessions: {}", userId, sessions.size());
            // Optional: Notify friends that this user is now online (but not necessarily 'active')
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) return;

        try {
            // First, parse into the generic wrapper to determine the event type
            WebSocketMessage webSocketMessage = mapper.readValue(msg.getPayload(), WebSocketMessage.class);

            // Route the message based on the event name
            switch (webSocketMessage.getEvent()) {
                case "presence_update":
                    handlePresenceUpdate(userId, webSocketMessage);
                    break;
                // Default case can be treated as a chat message for backward compatibility
                default:
                    handleChatMessage(userId, webSocketMessage);
                    break;
            }
        } catch (Exception e) {
            logger.error("Failed to handle incoming WebSocket message from user {}: {}", userId, e.getMessage(), e);
        }
    }

    private void handlePresenceUpdate(String userId, WebSocketMessage wsMessage) throws IOException {
        PresenceUpdatePayload payload = mapper.treeToValue(wsMessage.getPayload(), PresenceUpdatePayload.class);
        boolean isActive = "active".equals(payload.getStatus());
        
        logger.info("Presence update for user {}: status={}, chatWith={}", userId, payload.getStatus(), payload.getChatWith());

        // Update the central tracker with the detailed presence info
        activeUserTracker.updateUserPresence(userId, isActive, payload.getChatWith());

        // Notify friends about the status change so their UI updates
        broadcastStatusChangeToFriends(userId, isActive);
    }

    private void handleChatMessage(String senderId, WebSocketMessage wsMessage) throws IOException {
         // Convert the payload to a ChatMessage object
        ChatMessage chatMessage = mapper.treeToValue(wsMessage.getPayload(), ChatMessage.class);
        chatMessage.setFrom(senderId); // Ensure 'from' is the authenticated user
        chatMessage.setTimestamp(java.time.Instant.now());

        logger.info("Saving message to DB: {}", chatMessage);
        repo.save(chatMessage);

        String recipientId = chatMessage.getTo();
        WebSocketSession recipientSession = sessions.get(recipientId);

        // Get the detailed presence state of the recipient
        boolean isRecipientActiveInThisChat = activeUserTracker.isUserActiveInChat(recipientId, senderId);
        
        // Always try to send via WebSocket if the user is connected
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new TextMessage(mapper.writeValueAsString(chatMessage)));
        }

        // Send a push notification if the recipient is not actively viewing this specific chat
        if (!isRecipientActiveInThisChat) {
             User sender = profileService.findUserById(senderId);
             String senderName = (sender != null) ? sender.getDogName() : "Someone";
             String body = chatMessage.getMessage();
             
             // --- MODIFIED: Call the updated notification service method ---
             notificationService.sendNotification(recipientId, senderId, senderName, body);
        }
    }

    private void broadcastStatusChangeToFriends(String userId, boolean isActive) throws IOException {
        List<User> friends = profileService.findFriendsByUserId(userId);
        if (friends == null) return;
        
        var statusUpdate = Map.of(
            "event", "friend_status_update",
            "userId", userId,
            "isActive", isActive
        );
        String payload = mapper.writeValueAsString(statusUpdate);

        for (User friend : friends) {
            WebSocketSession friendSession = sessions.get(friend.getId().toString());
            if (friendSession != null && friendSession.isOpen()) {
                friendSession.sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            activeUserTracker.userDisconnected(userId);
            broadcastStatusChangeToFriends(userId, false); // Notify friends the user is now inactive/offline
            logger.info("User {} disconnected. Reason: {}. Total sessions: {}", userId, status, sessions.size());
        }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        logger.error("WebSocket transport error for user {}: {}", userId, exception.getMessage());
        // Ensure cleanup happens on transport error as well
        if (userId != null && sessions.containsKey(userId)) {
             afterConnectionClosed(session, CloseStatus.PROTOCOL_ERROR);
        }
    }
}

