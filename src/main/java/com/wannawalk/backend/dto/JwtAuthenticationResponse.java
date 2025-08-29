package com.wannawalk.backend.dto;

import lombok.Data;

@Data
public class JwtAuthenticationResponse {
    private String accessToken;
    private String refreshToken; // --- NEW ---
    private String tokenType = "Bearer";

    // --- MODIFIED: Constructor now accepts both tokens ---
    public JwtAuthenticationResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}