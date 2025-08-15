package com.wannawalk.backend.dto;

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
    private List<String> personality; // New field
    private String matchPreferences; // New field
}
