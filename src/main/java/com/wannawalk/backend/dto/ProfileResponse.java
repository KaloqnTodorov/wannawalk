package com.wannawalk.backend.dto;

import com.wannawalk.backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private String id;
    private String ownerName;
    private String dogName;
    private String email;
    private String breed;
    private Date birthday;
    private String profilePicUrl;
    private List<String> personality;
    private String matchPreferences;
    // --- EDITED: Changed to a list of FriendResponse objects ---
    private List<FriendResponse> friends;
    private User.MatchFilters matchFilters; // Field for returning saved filters
}
