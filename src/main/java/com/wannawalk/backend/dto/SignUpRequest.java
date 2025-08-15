package com.wannawalk.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.Date;

@Data
public class SignUpRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String yourName;
    
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    
    // Dog's info

    private String profilePicUrl; // URL of the uploaded profile picture

    @NotBlank
    private String dogName;

    @NotBlank
    private String breed;
    
    private Date birthday;
}
