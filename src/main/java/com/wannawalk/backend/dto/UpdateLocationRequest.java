package com.wannawalk.backend.dto;

import lombok.Data;

@Data
public class UpdateLocationRequest {
    private String userId;
    private double longitude;
    private double latitude;
}

