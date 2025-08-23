package com.wannawalk.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Document("messages")
@Getter
@Setter
public class ChatMessage {
    @Id
    private String id;
    private String from;
    private String to;
    private String message;
    private Instant timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String from, String to, String message) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.timestamp = Instant.now();
    }

    // Getters and setters omitted for brevity (use Lombok if you're smart)
}