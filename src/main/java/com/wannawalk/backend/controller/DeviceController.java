package com.wannawalk.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.wannawalk.backend.security.JwtTokenProvider;
import com.wannawalk.backend.service.ProfileService;

import java.security.Principal;

@RestController
@RequestMapping("/api")
public class DeviceController {
    // --- NEW: Added logger ---
    private static final Logger logger = LoggerFactory.getLogger(DeviceController.class);


    // --- MODIFIED: Injected ProfileService instead of a generic UserService ---
    private final ProfileService profileService;
    private final JwtTokenProvider tokenProvider; // Your JWT service

    public DeviceController(ProfileService profileService, JwtTokenProvider tokenProvider) {
        this.profileService = profileService;
        this.tokenProvider = tokenProvider;
    }

    // DTO for the request body
    static class FcmTokenRequest {
        public String fcmToken;
    }
    static class RefreshTokenRequest {
        public String refreshToken;
    }
    static class AuthResponse {
        public String accessToken;
        public AuthResponse(String accessToken) { this.accessToken = accessToken; }
    }


    @PostMapping("/users/register-device")
    public ResponseEntity<?> registerDevice(@RequestBody FcmTokenRequest request, Principal principal) {
        String userId = principal.getName(); // Get user ID from authenticated principal
        logger.info("Attempting to register device for principal: {}", principal.getName());

        profileService.addFcmToken(userId, request.fcmToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/unregister-device")
    public ResponseEntity<?> unregisterDevice(@RequestBody FcmTokenRequest request, Principal principal) {
        String userId = principal.getName();
        logger.info("Attempting to unregister device for principal: {}", principal.getName());

        profileService.removeFcmToken(userId, request.fcmToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        // Your logic to validate the refresh token and issue a new access token
        if (tokenProvider.validateRefreshToken(request.refreshToken)) {
            String newAccessToken = tokenProvider.createAccessTokenFromRefreshToken(request.refreshToken);
            return ResponseEntity.ok(new AuthResponse(newAccessToken));
        } else {
            return ResponseEntity.status(401).body("Invalid Refresh Token");
        }
    }
}