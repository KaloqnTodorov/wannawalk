package com.wannawalk.backend.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wannawalk.backend.model.ChatMessage;
import com.wannawalk.backend.repository.ChatMessageRepository;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageRepository repo;
    private final ObjectMapper mapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatMessageRepository repo) {
        this.repo = repo;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = (String) session.getAttributes().get("userId");
        sessions.put(userId, session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage msg) throws Exception {
        String json = msg.getPayload();
        ChatMessage parsed = mapper.readValue(json, ChatMessage.class);
        parsed.setTimestamp(java.time.Instant.now());
        repo.save(parsed);

        WebSocketSession recipientSession = sessions.get(parsed.getTo());
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new TextMessage(mapper.writeValueAsString(parsed)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.values().remove(session);
    }
}