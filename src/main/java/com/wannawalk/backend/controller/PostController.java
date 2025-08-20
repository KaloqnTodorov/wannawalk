package com.wannawalk.backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.wannawalk.backend.dto.CommentRequest;
import com.wannawalk.backend.dto.PostRequest;
import com.wannawalk.backend.dto.PostResponse;
import com.wannawalk.backend.model.Comment;
import com.wannawalk.backend.security.UserPrincipal;
import com.wannawalk.backend.service.PostService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@AuthenticationPrincipal UserPrincipal currentUser, @RequestBody PostRequest postRequest) {
        PostResponse newPost = postService.createPost(currentUser.getId(), postRequest);
        return ResponseEntity.ok(newPost);
    }

    @GetMapping("/me")
    public ResponseEntity<List<PostResponse>> getMyPosts(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<PostResponse> posts = postService.getPostsForUser(currentUser.getId());
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(@AuthenticationPrincipal UserPrincipal currentUser, @PathVariable String postId) {
        boolean isLiked = postService.toggleLike(currentUser.getId(), postId);
        return ResponseEntity.ok(Map.of("liked", isLiked));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@AuthenticationPrincipal UserPrincipal currentUser, @PathVariable String postId, @Valid @RequestBody CommentRequest commentRequest) {
        Comment newComment = postService.addComment(currentUser.getId(), postId, commentRequest);
        return ResponseEntity.ok(newComment);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@AuthenticationPrincipal UserPrincipal currentUser, @PathVariable String postId, @PathVariable String commentId) {
        try {
            postService.deleteComment(currentUser.getId(), postId, commentId);
            return ResponseEntity.noContent().build(); // Success, no content to return
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage())); // Forbidden
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage())); // Not Found
        }
    }
}
