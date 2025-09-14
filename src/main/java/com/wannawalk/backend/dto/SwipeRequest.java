package com.wannawalk.backend.dto;
import lombok.Data;

@Data
public class SwipeRequest {
    private String userId;
    private String swipedUserId;
    private String direction;
}