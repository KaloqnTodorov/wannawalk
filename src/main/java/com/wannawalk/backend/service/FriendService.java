// src/main/java/com/wannawalk/backend/service/FriendService.java

package com.wannawalk.backend.service;

import com.wannawalk.backend.errors.FriendServiceException;
import com.wannawalk.backend.security.JwtTokenProvider; // --- NEW: Hypothetical token provider ---
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wannawalk.backend.model.User;
import com.wannawalk.backend.repository.UserRepository;

import java.util.Objects;

@Service
public class FriendService {

    @Autowired
    private UserRepository userRepository;

    // --- NEW: Inject the token provider for validation ---
    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * --- NEW: Adds a friend using a token from a QR code. ---
     * Validates the token, extracts the friend's ID, and calls addFriend.
     * @return The User object of the newly added friend.
     */
    public User addFriendByToken(String currentUserId, String token) {
        if (!tokenProvider.validateToken(token)) {
            throw new FriendServiceException("QR code is invalid or has expired.");
        }

        String friendId = tokenProvider.getUserIdFromJWT(token);
        addFriend(currentUserId, friendId);

        // Return the full User object of the new friend for the client
        return findUserById(friendId);
    }

    public void addFriend(String currentUserId, String friendId) {
        if (Objects.equals(currentUserId, friendId)) {
            throw new FriendServiceException("You cannot add yourself as a friend.");
        }

        User currentUser = findUserById(currentUserId);
        User friendUser = findUserById(friendId);

        if (currentUser.getFriends().contains(friendId)) {
            throw new FriendServiceException("You are already friends.");
        }

        currentUser.getFriends().add(friendId);
        friendUser.getFriends().add(currentUserId);

        userRepository.save(currentUser);
        userRepository.save(friendUser);
    }

    public void removeFriend(String currentUserId, String friendId) {
        User currentUser = findUserById(currentUserId);
        User friendUser = findUserById(friendId);

        if (!currentUser.getFriends().contains(friendId)) {
            throw new FriendServiceException("You are not friends with this user.");
        }

        currentUser.getFriends().remove(friendId);
        friendUser.getFriends().remove(currentUserId);

        userRepository.save(currentUser);
        userRepository.save(friendUser);
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new FriendServiceException("User not found with id: " + userId));
    }
}