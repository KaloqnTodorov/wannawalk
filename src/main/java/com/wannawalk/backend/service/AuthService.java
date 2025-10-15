package com.wannawalk.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wannawalk.backend.dto.JwtAuthenticationResponse; // --- NEW ---
import com.wannawalk.backend.dto.LoginRequest;
import com.wannawalk.backend.dto.SignUpRequest;
import com.wannawalk.backend.model.User;
import com.wannawalk.backend.repository.UserRepository;
import com.wannawalk.backend.security.JwtTokenProvider;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private EmailService emailService;

    @Value("${app.url}")
    private String appUrl;

    public void registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email Address already in use!");
        }

        // Create new user's account with all the new fields
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getYourName(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                signUpRequest.getProfilePicUrl(),
                signUpRequest.getDogName(),
                signUpRequest.getBreed(),
                signUpRequest.getBirthday()
        );

        String token = UUID.randomUUID().toString();
        user.setConfirmationToken(token);
        user.setConfirmationTokenExpires(Instant.now().plusSeconds(86400));

        userRepository.save(user);

        String confirmationUrl = appUrl + "/api/auth/confirm/" + token;
        String emailContent = "<div style='font-family: Arial, sans-serif; text-align: center; padding: 20px;'>"
                + "<h2>Welcome to the App!</h2>"
                + "<p>Click the button below to confirm your email address.</p>"
                + "<a href='" + confirmationUrl + "' style='background-color: #007bff; color: white; padding: 15px 25px; text-decoration: none; border-radius: 5px; display: inline-block; margin-top: 20px;'>Confirm Email</a>"
                + "</div>";

        emailService.sendConfirmationEmail(user.getEmail(), "Confirm Your Email Address", emailContent);
    }

    public void confirmUserRegistration(String token) {
        User user = userRepository.findByConfirmationToken(token)
                .orElseThrow(() -> new RuntimeException("Confirmation link is invalid."));

        if (user.getConfirmationTokenExpires().isBefore(Instant.now())) {
            throw new RuntimeException("Confirmation link has expired.");
        }
    }

    // --- MODIFIED: This method now returns the response object with both tokens ---
    public JwtAuthenticationResponse loginUser(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials."));

        if (!user.isVerified()) {
            throw new RuntimeException("Your account has not been verified. Please check your email.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials.");
        }

        // Generate both tokens
        String accessToken = tokenProvider.generateToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        // Return the response object
        return new JwtAuthenticationResponse(accessToken, refreshToken);
    }

    // --- NEW METHOD ---
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email."));

        // Generate a random 8-character temporary password
        String temporaryPassword = UUID.randomUUID().toString().substring(0, 8);

        // Update the user's password
        user.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);

        // Send the temporary password via email
        String emailContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2>Password Reset</h2>"
                + "<p>You have requested to reset your password. Your temporary password is:</p>"
                + "<p style='font-size: 24px; font-weight: bold; letter-spacing: 2px; background-color: #f0f0f0; padding: 10px; border-radius: 5px; display: inline-block;'>" + temporaryPassword + "</p>"
                + "<p>Please log in with this temporary password and change it immediately from your profile settings.</p>"
                + "</div>";

        emailService.sendPasswordResetEmail(user.getEmail(), "Your Temporary Password", emailContent);
    }
}
