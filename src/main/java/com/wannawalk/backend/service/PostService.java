package com.wannawalk.backend.service;

import com.wannawalk.backend.dto.CommentRequest;
import com.wannawalk.backend.dto.PostRequest;
import com.wannawalk.backend.dto.PostResponse;
import com.wannawalk.backend.model.Comment;
import com.wannawalk.backend.model.Post;
import com.wannawalk.backend.model.User;
import com.wannawalk.backend.repository.CommentRepository;
import com.wannawalk.backend.repository.PostRepository;
import com.wannawalk.backend.repository.UserRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;

    public PostResponse createPost(String userId, PostRequest postRequest) {
        User author = findUserById(userId);
        Post post = new Post();
        post.setAuthor(author);
        post.setDescription(postRequest.getDescription());
        post.setImageUrl(postRequest.getImageUrl());
        post.setLocation(postRequest.getLocation());
        post.setTaggedFriends(postRequest.getTaggedFriends());
        Post savedPost = postRepository.save(post);
        return mapPostToResponse(savedPost);
    }

    public List<PostResponse> getPostsForUser(String userId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    public List<PostResponse> getFeedForUser(String userId) {
        User currentUser = findUserById(userId);
        List<String> userIds = new ArrayList<>();
        userIds.add(currentUser.getId());

        if (currentUser.getFriends() != null) {
            Pattern pattern = Pattern.compile("([a-f0-9]{24})");
            currentUser.getFriends().forEach(friendIdString -> {
                Matcher matcher = pattern.matcher(friendIdString);
                if (matcher.find()) {
                    userIds.add(matcher.group(1));
                }
            });
        }

        // TODO: This is inefficient. Refactor to make a single database call to fetch all posts.
        List<Post> allPosts = new ArrayList<>();
        for (String id : userIds) {
            allPosts.addAll(postRepository.findByAuthorIdOrderByCreatedAtDesc(id));
        }

        allPosts.sort(Comparator.comparing(Post::getCreatedAt).reversed());

        return allPosts.stream()
                .map(this::mapPostToResponse)
                .collect(Collectors.toList());
    }

    // --- NEW: Service method to get a single post ---
    public PostResponse getPostById(String postId) {
        Post post = findPostById(postId);
        return mapPostToResponse(post);
    }

    public boolean toggleLike(String userId, String postId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);
        boolean isLiked;
        // Check if the user is already in the likes list
        boolean userExists = post.getLikes().stream().anyMatch(u -> u.getId().equals(userId));

        if (userExists) {
            post.getLikes().removeIf(u -> u.getId().equals(userId));
            isLiked = false;
        } else {
            post.getLikes().add(user);
            isLiked = true;
        }
        postRepository.save(post);
        return isLiked;
    }

    public Comment addComment(String userId, String postId, CommentRequest commentRequest) {
        User author = findUserById(userId);
        Post post = findPostById(postId);
        Comment comment = new Comment(author, commentRequest.getText());
        Comment savedComment = commentRepository.save(comment);
        post.getComments().add(savedComment);
        postRepository.save(post);
        return savedComment;
    }

    public void deleteComment(String userId, String postId, String commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new SecurityException("User is not authorized to delete this comment.");
        }

        Post post = findPostById(postId);
        post.getComments().removeIf(c -> c.getId().equals(commentId));
        postRepository.save(post);

        commentRepository.delete(comment);
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private Post findPostById(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
    }

    private PostResponse mapPostToResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setAuthorId(post.getAuthor().getId());
        response.setAuthorUsername(post.getAuthor().getDogName());
        response.setAuthorProfilePicUrl(post.getAuthor().getProfilePicUrl());
        response.setDescription(post.getDescription());
        response.setImageUrl(post.getImageUrl());
        response.setLocation(post.getLocation());

        if (post.getTaggedFriends() != null) {
            response.setTaggedFriends(new ArrayList<>(post.getTaggedFriends()));
        }

        response.setLikes(post.getLikes().stream().map(User::getId).collect(Collectors.toSet()));
        response.setComments(post.getComments());

        if (post.getCreatedAt() != null) {
            response.setCreatedAt(post.getCreatedAt());
        }

        return response;
    }
}

