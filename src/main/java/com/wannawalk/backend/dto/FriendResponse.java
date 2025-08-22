package com.wannawalk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {
    private String id;
    private String name; // This will be the dog's name
    private String imageUrl;
}
