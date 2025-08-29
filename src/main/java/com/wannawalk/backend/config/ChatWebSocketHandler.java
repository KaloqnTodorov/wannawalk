package com.wannawalk.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wannawalk.backend.model.ChatMessage;
import com.wannawalk.backend.model.User;
import com.wannawalk.backend.repository.ChatMessageRepository;
import com.wannawalk.backend.service.ActiveUserTracker;
import com.wannawalk.backend.service.NotificationService;
import com.wannawalk.backend.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
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
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.put(userId, session);
            activeUserTracker.userConnected(userId);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage msg) {
        try {
            String json = msg.getPayload();
            ChatMessage parsed = mapper.readValue(json, ChatMessage.class);
            parsed.setTimestamp(java.time.Instant.now());
            
            logger.info("Saving message to DB: {}", parsed);
            repo.save(parsed);

            WebSocketSession recipientSession = sessions.get(parsed.getTo());
            if (recipientSession != null && recipientSession.isOpen()) {
                recipientSession.sendMessage(new TextMessage(mapper.writeValueAsString(parsed)));
            } else {
                User sender = profileService.findUserById(parsed.getFrom());
                String senderName = (sender != null) ? sender.getYourName() : "Someone";
                
                String title = "New message from " + senderName;
                String body = parsed.getMessage();
                
                notificationService.sendNotificationIfUserIsOffline(parsed.getTo(), title, body);
            }
        } catch (Exception e) {
            logger.error("Failed to handle incoming WebSocket message: {}", e.getMessage(), e);
        }
    }

    // --- NEW: Added method to handle transport errors ---
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        logger.error("WebSocket transport error for user {}: {}", userId, exception.getMessage());
        // It's good practice to ensure the user is disconnected in case of a transport error
        if (userId != null) {
            sessions.remove(userId);
            activeUserTracker.userDisconnected(userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = (String) session.getAttributes().get("userId");
        if (userId != null) {
            sessions.remove(userId);
            activeUserTracker.userDisconnected(userId);
        }
    }
}
