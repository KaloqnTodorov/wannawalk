package com.wannawalk.backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wannawalk.backend.dto.ProfileResponse;
import com.wannawalk.backend.dto.ProfileUpdateRequest;
import com.wannawalk.backend.security.UserPrincipal;
import com.wannawalk.backend.service.ProfileService;

import java.util.Map;


@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getCurrentUserProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        // The UserPrincipal object is automatically populated by Spring Security
        // thanks to our JwtAuthenticationFilter.
        ProfileResponse profileResponse = profileService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profileResponse);
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateCurrentUserProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody ProfileUpdateRequest updateRequest) {
        ProfileResponse updatedProfile = profileService.updateUserProfile(currentUser.getId(), updateRequest);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/me/picture")
    public ResponseEntity<?> updateCurrentUserProfilePicture(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam("file") MultipartFile file) {
        try {
            String newFileUrl = profileService.updateUserProfilePicture(currentUser.getId(), file);
            return ResponseEntity.ok(Map.of("url", newFileUrl));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Could not update profile picture: " + e.getMessage());
        }
    }
}

