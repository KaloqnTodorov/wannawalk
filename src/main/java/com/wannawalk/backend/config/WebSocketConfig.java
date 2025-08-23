package com.wannawalk.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatHandler;
    private final JwtHandshakeInterceptor jwtInterceptor;

    public WebSocketConfig(ChatWebSocketHandler chatHandler, JwtHandshakeInterceptor jwtInterceptor) {
        this.chatHandler = chatHandler;
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatHandler, "/chat").addInterceptors(jwtInterceptor).setAllowedOrigins("*");
    }
}