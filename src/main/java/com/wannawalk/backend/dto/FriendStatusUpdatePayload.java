package com.wannawalk.backend.dto;

import lombok.Getter;
import lombok.Setter;
/**
 * Represents the payload for a "friend_status_update" event sent from server to client.
 */
@Getter
@Setter
public class FriendStatusUpdatePayload {
    private String event = "friend_status_update"; // Event name is fixed
    private String userId;
    private boolean isActive;

    public FriendStatusUpdatePayload(String userId, boolean isActive) {
        this.userId = userId;
        this.isActive = isActive;
    }
}