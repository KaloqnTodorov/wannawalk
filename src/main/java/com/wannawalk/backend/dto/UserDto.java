package com.wannawalk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * A Data Transfer Object for returning public user information for matching cards.
 * This prevents exposing the full User entity from your database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String name;
    private int age;
    private String breed;
    private String imageUrl;
    private List<String> personality;
    private String matchPreferences;
}
