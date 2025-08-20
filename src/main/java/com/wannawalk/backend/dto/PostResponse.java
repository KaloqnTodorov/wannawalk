package com.wannawalk.backend.dto;

import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.wannawalk.backend.model.Comment;

// A DTO to represent a post in API responses, including likes and comments
@Data
public class PostResponse {
    private String id;
    private String authorUsername;
    private String authorProfilePicUrl;
    private String description;
    private String imageUrl;
    private String location;
    private List<String> taggedFriends;
    private Set<String> likes; // User IDs of who liked the post
    private List<Comment> comments;
    private Instant createdAt;
}