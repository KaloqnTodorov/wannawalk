package com.wannawalk.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
/**
 * Represents the payload for a "presence_update" event.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresenceUpdatePayload {
    private String status; // "active" or "inactive"
    private String chatWith; // The ID of the user they are chatting with
}