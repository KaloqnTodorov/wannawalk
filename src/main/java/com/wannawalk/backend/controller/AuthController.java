package com.wannawalk.backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.wannawalk.backend.dto.JwtAuthenticationResponse;
import com.wannawalk.backend.dto.LoginRequest;
import com.wannawalk.backend.dto.SignUpRequest;
import com.wannawalk.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService; // Inject the new service

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
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        
            // Delegate the login logic and JWT generation to the service
            String jwt = authService.loginUser(loginRequest);
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
       
    }
}
