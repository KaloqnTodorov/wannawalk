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
import java.time.Instant;
import java.util.LinkedHashMap;
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

    public ChatWebSocketHandler(ChatMessageRepository repo,
                                ActiveUserTracker activeUserTracker,
                                NotificationService notificationService,
                                ProfileService profileService) {
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
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Missing userId"));
            return;
        }
        sessions.put(userId, session);
        // Track general online status; "active in chat" is handled via presence updates.
        activeUserTracker.userConnected(userId);
        logger.info("User {} connected. Total sessions: {}", userId, sessions.size());
        // NOTE: Do NOT broadcast here (keeps behavior identical to your working version).
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) return;

        try {
            WebSocketMessage webSocketMessage = mapper.readValue(msg.getPayload(), WebSocketMessage.class);

            switch (webSocketMessage.getEvent()) {
                case "presence_update":
                    handlePresenceUpdate(userId, webSocketMessage);
                    break;
                // Default (and any other event) → treat as chat message, like your original
                default:
                    handleChatMessage(userId, webSocketMessage);
                    break;
            }
        } catch (Exception e) {
            logger.error("Failed to handle WS message from user {}: {}", userId, e.getMessage(), e);
        }
    }

    private void handlePresenceUpdate(String userId, WebSocketMessage wsMessage) throws IOException {
        PresenceUpdatePayload payload = mapper.treeToValue(wsMessage.getPayload(), PresenceUpdatePayload.class);
        boolean isActive = "active".equalsIgnoreCase(payload.getStatus());
        String chatWith = payload.getChatWith(); // may be null

        logger.info("Presence update for user {}: status={}, chatWith={}", userId, payload.getStatus(), chatWith);

        // Update tracker with detailed presence (active + which chat)
        activeUserTracker.updateUserPresence(userId, isActive, chatWith);

        // Broadcast to FRIENDS (not global), including chatWith for in-chat cues
        broadcastStatusChangeToFriends(userId, isActive, chatWith);

        // One-off snapshot back to the entrant so they know if peer was already here
        if (isActive && chatWith != null) {
            boolean peerInThisChat = activeUserTracker.isUserActiveInChat(chatWith, userId);

            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("event", "peer_presence");
            snapshot.put("userId", chatWith);
            snapshot.put("isActive", peerInThisChat);             // true if peer is active AND in this chat with me
            snapshot.put("chatWith", peerInThisChat ? userId : null);
            sendJson(userId, snapshot);
        }
    }

    private void handleChatMessage(String senderId, WebSocketMessage wsMessage) throws IOException {
        // Convert payload to your entity and persist
        ChatMessage chatMessage = mapper.treeToValue(wsMessage.getPayload(), ChatMessage.class);
        chatMessage.setFrom(senderId);
        chatMessage.setTimestamp(Instant.now());
        logger.info("Saving message to DB: {}", chatMessage);
        repo.save(chatMessage);

        String recipientId = chatMessage.getTo();
        WebSocketSession recipientSession = sessions.get(recipientId);

        // Try to deliver via WebSocket (raw ChatMessage for backward compatibility)
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new TextMessage(mapper.writeValueAsString(chatMessage)));
        }

        // Push notifications if recipient is not actively viewing THIS chat
        boolean recipientWatchingThisChat = activeUserTracker.isUserActiveInChat(recipientId, senderId);
        if (!recipientWatchingThisChat) {
            User sender = profileService.findUserById(senderId);
            String senderName = (sender != null) ? sender.getDogName() : "Someone";
            String body = chatMessage.getMessage();
            notificationService.sendNotification(recipientId, senderId, senderName, body);
        }
    }

    /**
     * Broadcast presence to FRIENDS only (same behavior as your working code),
     * now including 'chatWith' for in-chat cues. Uses LinkedHashMap so nulls are allowed.
     */
    private void broadcastStatusChangeToFriends(String userId, boolean isActive, String chatWith) throws IOException {
        List<User> friends = profileService.findFriendsByUserId(userId);
        if (friends == null) return;

        Map<String, Object> statusUpdate = new LinkedHashMap<>();
        statusUpdate.put("event", "friend_status_update");
        statusUpdate.put("userId", userId);
        statusUpdate.put("isActive", isActive);             // keeps your original semantic: active == foreground chat
        statusUpdate.put("chatWith", chatWith);             // may be null

        String payload = mapper.writeValueAsString(statusUpdate);

        for (User friend : friends) {
            WebSocketSession friendSession = sessions.get(friend.getId().toString());
            if (friendSession != null && friendSession.isOpen()) {
                friendSession.sendMessage(new TextMessage(payload));
            }
        }
    }

    private void sendJson(String toUserId, Object obj) throws IOException {
        WebSocketSession s = sessions.get(toUserId);
        if (s == null || !s.isOpen()) return;
        s.sendMessage(new TextMessage(mapper.writeValueAsString(obj)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            activeUserTracker.userDisconnected(userId);
            // Let friends know this user is no longer active/in this chat
            try {
                broadcastStatusChangeToFriends(userId, false, null);
            } catch (IOException ignored) {}
            logger.info("User {} disconnected. Reason: {}. Total sessions: {}", userId, status, sessions.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        logger.error("WebSocket transport error for user {}: {}", userId, exception.getMessage());
        if (userId != null && sessions.containsKey(userId)) {
            afterConnectionClosed(session, CloseStatus.PROTOCOL_ERROR);
        }
    }
}
