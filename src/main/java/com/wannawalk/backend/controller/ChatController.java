package com.wannawalk.backend.controller;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.PageRequest; // Import PageRequest
import org.springframework.data.domain.Pageable; // Import Pageable
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
    public Page<ChatMessage> getHistory(
            @RequestHeader("Authorization") String auth,
            @PathVariable String friendId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) { // Accept page and size

        String token = auth.replace("Bearer ", "");
        String userId = Jwts.parser()
                .setSigningKey(jwtSecret.getBytes())
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        // Create a Pageable object, sorting by timestamp DESC to get the newest messages first
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        // Call the updated repository method
        return repo.findConversationBetween(userId, friendId, pageable);
    }
}