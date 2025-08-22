package com.wannawalk.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wannawalk.backend.model.User;
import com.wannawalk.backend.repository.UserRepository;

@Service
public class FriendService {

    @Autowired
    private UserRepository userRepository;

    public void addFriend(String currentUserId, String friendId) {
        if (currentUserId.equals(friendId)) {
            throw new RuntimeException("You cannot add yourself as a friend.");
        }
        
        User currentUser = findUserById(currentUserId);
        User friendUser = findUserById(friendId);

        // Add each other to their respective friends lists
        currentUser.getFriends().add(friendId);
        friendUser.getFriends().add(currentUserId);

        userRepository.save(currentUser);
        userRepository.save(friendUser);
    }

    public void removeFriend(String currentUserId, String friendId) {
        User currentUser = findUserById(currentUserId);
        User friendUser = findUserById(friendId);

        // Remove each other from their friends lists
        currentUser.getFriends().remove(friendId);
        friendUser.getFriends().remove(currentUserId);

        userRepository.save(currentUser);
        userRepository.save(friendUser);
    }
    
    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
}
