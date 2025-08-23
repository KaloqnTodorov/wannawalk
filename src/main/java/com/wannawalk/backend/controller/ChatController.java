package com.wannawalk.backend.controller;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import com.wannawalk.backend.model.ChatMessage;
import com.wannawalk.backend.repository.ChatMessageRepository;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatMessageRepository repo;
    private final String jwtSecret;

    public ChatController(ChatMessageRepository repo, @Value("${app.jwtSecret}") String jwtSecret) {
        this.repo = repo;
        this.jwtSecret = jwtSecret;
    }

    @GetMapping("/history/{friendId}")
    public List<ChatMessage> getHistory(@RequestHeader("Authorization") String auth, @PathVariable String friendId) {
        String token = auth.replace("Bearer ", "");
        String userId = Jwts.parser()
                .setSigningKey(jwtSecret.getBytes())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return repo.findConversationBetween(userId, friendId, Sort.by(Sort.Direction.ASC, "timestamp"));
    }
}