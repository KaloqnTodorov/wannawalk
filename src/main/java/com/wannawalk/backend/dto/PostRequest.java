package com.wannawalk.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostRequest {
    private String description;
    private String imageUrl;
    private String location;
    private List<String> taggedFriends;
}
