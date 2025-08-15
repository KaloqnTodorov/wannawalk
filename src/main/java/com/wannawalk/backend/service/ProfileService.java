package com.wannawalk.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.wannawalk.backend.dto.ProfileResponse;
import com.wannawalk.backend.dto.ProfileUpdateRequest;
import com.wannawalk.backend.model.User;
import com.wannawalk.backend.repository.UserRepository;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public ProfileResponse getUserProfile(String userId) {
        User user = findUserById(userId);
        return mapUserToProfileResponse(user);
    }

    public ProfileResponse updateUserProfile(String userId, ProfileUpdateRequest updateRequest) {
        User user = findUserById(userId);

        user.setPersonality(updateRequest.getPersonality());
        user.setMatchPreferences(updateRequest.getMatchPreferences());

        User updatedUser = userRepository.save(user);
        return mapUserToProfileResponse(updatedUser);
    }

    public String updateUserProfilePicture(String userId, MultipartFile file) {
        User user = findUserById(userId);

        // Delete the old profile picture if it exists
        if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isBlank()) {
            String oldFileName = user.getProfilePicUrl().substring(user.getProfilePicUrl().lastIndexOf("/") + 1);
            fileStorageService.deleteFile(oldFileName);
        }

        // Store the new profile picture
        String newFileName = fileStorageService.storeFile(file);
        String newFileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(newFileName)
                .toUriString();

        // Update the user's profile picture URL
        user.setProfilePicUrl(newFileUrl);
        userRepository.save(user);

        return newFileUrl;
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private ProfileResponse mapUserToProfileResponse(User user) {
        return new ProfileResponse(
            user.getId(),
            user.getYourName(),
            user.getDogName(),
            user.getEmail(),
            user.getBreed(),
            user.getBirthday(),
            user.getProfilePicUrl(),
            user.getPersonality(),
            user.getMatchPreferences()
        );
    }
}
