// src/main/java/com/wannawalk/backend/controller/FriendController.java

package com.wannawalk.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.wannawalk.backend.model.User; // --- NEW: Import User model ---
import com.wannawalk.backend.security.JwtTokenProvider; // --- NEW: Hypothetical token provider ---
import com.wannawalk.backend.security.UserPrincipal;
import com.wannawalk.backend.service.FriendService;

import java.util.Map; // --- NEW: Import Map ---

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    // --- NEW: Inject the token provider ---
    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * --- NEW: Generates a short-lived token for QR code friend invites. ---
     * The token contains the current user's ID and has a 60-second validity.
     */
    @GetMapping("/qr-token")
    public ResponseEntity<?> generateQrToken(@AuthenticationPrincipal UserPrincipal currentUser) {
        // Generate a token with a 60-second expiration
        String token = tokenProvider.generateTokenWithUserId(currentUser.getId(), 60000);
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * --- NEW: Adds a friend by validating a scanned QR code token. ---
     * Responds with the new friend's user object for optimistic UI updates.
     */
    @PostMapping("/add-by-token")
    public ResponseEntity<?> addFriendByToken(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody Map<String, String> body) {
        String token = body.get("token");
        User newFriend = friendService.addFriendByToken(currentUser.getId(), token);
        return ResponseEntity.ok(newFriend);
    }


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