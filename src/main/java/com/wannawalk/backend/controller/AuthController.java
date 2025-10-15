package com.wannawalk.backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.wannawalk.backend.dto.JwtAuthenticationResponse;
import com.wannawalk.backend.dto.LoginRequest;
import com.wannawalk.backend.dto.SignUpRequest;
import com.wannawalk.backend.service.AuthService;
import com.wannawalk.backend.service.ProfileService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // --- NEW: Inject ProfileService for password changes ---
    @Autowired
    private ProfileService profileService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            // Delegate the entire registration process to the service
            authService.registerUser(signUpRequest);
            return new ResponseEntity<>("User registered successfully! Please check your email for confirmation.", HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Catch exceptions from the service layer and return an appropriate error response
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/confirm/{token}")
    public ResponseEntity<String> confirmRegistration(@PathVariable String token) {
        try {
            // Delegate the confirmation logic to the service
            authService.confirmUserRegistration(token);
            return new ResponseEntity<>("<h1>Success!</h1><p>Your email has been confirmed. You can now log in.</p>", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("<h1>Error</h1><p>" + e.getMessage() + "</p>", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        // --- MODIFIED: The service now returns the full response object ---
        JwtAuthenticationResponse response = authService.loginUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    // --- NEW ENDPOINT ---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            authService.forgotPassword(email);
            return ResponseEntity.ok("A temporary password has been sent to your email address.");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // --- NEW ENDPOINT ---
    // Note: This endpoint should be protected by your security configuration to ensure only authenticated users can access it.
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> payload) {
        try {
            String oldPassword = payload.get("oldPassword");
            String newPassword = payload.get("newPassword");

            // userDetails.getUsername() is typically the email in Spring Security configuration
            String userEmail = userDetails.getUsername();

            profileService.changePassword(userEmail, oldPassword, newPassword);

            return ResponseEntity.ok("Password changed successfully.");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
