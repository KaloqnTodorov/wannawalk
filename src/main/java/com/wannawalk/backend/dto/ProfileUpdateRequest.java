package com.wannawalk.backend.dto;


import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class ProfileUpdateRequest {
    private List<String> personality;
    private String matchPreferences;
}