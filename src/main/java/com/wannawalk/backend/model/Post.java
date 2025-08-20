package com.wannawalk.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    @DBRef
    private User author;

    private String description;

    private String imageUrl; // Optional

    private String location; // Optional

    private List<String> taggedFriends = new ArrayList<>();

    @DBRef
    private Set<User> likes = new HashSet<>();

    @DBRef
    private List<Comment> comments = new ArrayList<>();

    private Instant createdAt = Instant.now();
}
