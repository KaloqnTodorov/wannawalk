package com.wannawalk.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.wannawalk.backend.security.UserPrincipal;
import com.wannawalk.backend.service.FriendService;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @PostMapping("/{friendId}")
    public ResponseEntity<?> addFriend(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String friendId) {
        friendService.addFriend(currentUser.getId(), friendId);
        return ResponseEntity.ok().body("Friend added successfully.");
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<?> removeFriend(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String friendId) {
        friendService.removeFriend(currentUser.getId(), friendId);
        return ResponseEntity.ok().body("Friend removed successfully.");
    }
}
