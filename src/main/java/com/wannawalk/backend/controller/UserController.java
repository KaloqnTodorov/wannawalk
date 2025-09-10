package com.wannawalk.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wannawalk.backend.dto.ProfileResponse;
import com.wannawalk.backend.service.ProfileService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private ProfileService profileService;

    /**
     * Gets the public profile information for a specific user.
     * This endpoint is intended to be called by other authenticated users.
     * @param userId The ID of the user whose profile is being requested.
     * @return A ResponseEntity containing the public profile data.
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<ProfileResponse> getUserProfile(@PathVariable String userId) {
        ProfileResponse profileResponse = profileService.getPublicUserProfile(userId);
        return ResponseEntity.ok(profileResponse);
    }
}
