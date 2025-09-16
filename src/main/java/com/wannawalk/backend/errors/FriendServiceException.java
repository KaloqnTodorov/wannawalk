package com.wannawalk.backend.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for handling friend-related business logic errors.
 * Responds with a 400 Bad Request status by default.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FriendServiceException extends RuntimeException {
    public FriendServiceException(String message) {
        super(message);
    }
}
