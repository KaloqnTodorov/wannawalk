package com.wannawalk.backend.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Getter
@NoArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;
    
    private String yourName;

    @Indexed(unique = true)
    private String email;

    private String password;
    
    private String profilePicUrl;

    // Dog's info
    private String dogName;
    private String breed;
    private Date birthday;
    private List<String> personality; // New field
    private String matchPreferences; // New field

       // --- NEW: Field to store IDs of friends ---
    private Set<String> friends = new HashSet<>();
    // --- NEW: Add this field to store device tokens for push notifications ---
    private List<String> fcmTokens = new ArrayList<>();

    private boolean isVerified = false;
    private String confirmationToken;
    private Instant confirmationTokenExpires;

    public User(String username, String yourName, String email, String password, String profilePicUrl, String dogName, String breed, Date birthday) {
        this.username = username;
        this.yourName = yourName;
        this.email = email;
        this.password = password;
        this.profilePicUrl = profilePicUrl;
        this.dogName = dogName;
        this.breed = breed;
        this.birthday = birthday;
    }
}
