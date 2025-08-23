package com.wannawalk.backend.config;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.wannawalk.backend.security.JwtTokenProvider;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final JwtTokenProvider tokenProvider;

    public JwtHandshakeInterceptor(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        try {
            String query = request.getURI().getQuery();
            logger.info("WebSocket handshake query: {}", query);

            if (query != null && query.startsWith("token=")) {
                String token = query.substring("token=".length());
                if (tokenProvider.validateToken(token)) {
                    String userId = tokenProvider.getUserIdFromJWT(token);
                    logger.info("WebSocket handshake authenticated user: {}", userId);
                    attributes.put("userId", userId);
                    return true;
                } else {
                    logger.warn("Invalid JWT token during WebSocket handshake.");
                }
            } else {
                logger.warn("No token in WebSocket query.");
            }
        } catch (JwtException e) {
            logger.error("JWT error in WebSocket handshake: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error in WebSocket handshake", e);
        }

        return false; // reject the handshake
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // No-op
    }
}
