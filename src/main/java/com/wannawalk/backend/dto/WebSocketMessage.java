package com.wannawalk.backend.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

/**
 * A generic container for all messages sent over WebSocket.
 * The 'event' field will be used to route the message to the correct handler.
 */
@Getter
@Setter
public class WebSocketMessage {
    private String event;
    private JsonNode payload; // Using JsonNode for flexible payload handling
}