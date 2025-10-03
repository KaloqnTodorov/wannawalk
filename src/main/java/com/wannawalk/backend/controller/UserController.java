package com.wannawalk.backend.controller;

import com.wannawalk.backend.dto.NotificationSettingsDto;
import com.wannawalk.backend.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    /**
     * --- NEW ENDPOINT ---
     * Gets the notification settings for the currently authenticated user.
     * @param currentUser The principal of the logged-in user.
     * @return A ResponseEntity with the user's notification settings.
     */
    @GetMapping("/settings/notifications")
    public ResponseEntity<NotificationSettingsDto> getNotificationSettings(@AuthenticationPrincipal UserPrincipal currentUser) {
        NotificationSettingsDto settings = profileService.getNotificationSettings(currentUser.getId());
        return ResponseEntity.ok(settings);
    }

    /**
     * --- NEW ENDPOINT ---
     * Updates the notification settings for the currently authenticated user.
     * @param currentUser The principal of the logged-in user.
     * @param settings The new settings to save.
     * @return A ResponseEntity indicating success.
     */
    @PutMapping("/settings/notifications")
    public ResponseEntity<Void> updateNotificationSettings(@AuthenticationPrincipal UserPrincipal currentUser, @RequestBody NotificationSettingsDto settings) {
        profileService.updateNotificationSettings(currentUser.getId(), settings);
        return ResponseEntity.ok().build();
    }
}
