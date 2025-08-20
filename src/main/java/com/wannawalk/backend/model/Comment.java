package com.wannawalk.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;

    @DBRef
    private User author;

    private String text;

    private Instant createdAt = Instant.now();

    public Comment(User author, String text) {
        this.author = author;
        this.text = text;
    }
}
