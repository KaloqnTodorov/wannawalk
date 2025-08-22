package com.wannawalk.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.wannawalk.backend.dto.FriendResponse;
import com.wannawalk.backend.dto.ProfileResponse;
import com.wannawalk.backend.dto.ProfileUpdateRequest;
import com.wannawalk.backend.model.User;
import com.wannawalk.backend.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        if (user.getProfilePicUrl() != null && !user.getProfilePicUrl().isBlank()) {
            String oldFileName = user.getProfilePicUrl().substring(user.getProfilePicUrl().lastIndexOf("/") + 1);
            fileStorageService.deleteFile(oldFileName);
        }

        String newFileName = fileStorageService.storeFile(file);
        String newFileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(newFileName)
                .toUriString();

        user.setProfilePicUrl(newFileUrl);
        userRepository.save(user);

        return newFileUrl;
    }

    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    private ProfileResponse mapUserToProfileResponse(User user) {
        // --- EDITED: Added logic to parse friend IDs from DBRef strings ---
        List<FriendResponse> friends = new ArrayList<>();
        if (user.getFriends() != null) {
            friends = user.getFriends().stream()
                .map(friendIdString -> {
                    // This regex extracts the 24-character hex ObjectId from the DBRef string
                    Pattern pattern = Pattern.compile("([a-f0-9]{24})");
                    Matcher matcher = pattern.matcher(friendIdString);
                    if (matcher.find()) {
                        return findUserById(matcher.group(1));
                    }
                    return null; // Return null if no valid ID is found
                })
                .filter(Objects::nonNull) // Remove any entries that couldn't be resolved
                .map(friendUser -> new FriendResponse(
                    friendUser.getId(),
                    friendUser.getDogName(),
                    friendUser.getProfilePicUrl()
                ))
                .collect(Collectors.toList());
        }

        return new ProfileResponse(
            user.getId(),
            user.getYourName(),
            user.getDogName(),
            user.getEmail(),
            user.getBreed(),
            user.getBirthday(),
            user.getProfilePicUrl(),
            user.getPersonality(),
            user.getMatchPreferences(),
            friends 
        );
    }
}
