package com.wannawalk.backend.service;

import com.wannawalk.backend.model.User.MatchFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public void addFcmToken(String userEmail, String token) {
        try {
            logger.info("Adding FCM token for userEmail: {}", userEmail);
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

            if (!user.getFcmTokens().contains(token)) {
                user.getFcmTokens().add(token);
                userRepository.save(user);
                logger.info("Successfully added FCM token for userId: {}", user.getId());
            } else {
                logger.info("FCM token already exists for userId: {}", user.getId());
            }
        } catch (Exception e) {
            logger.error("Error adding FCM token for userEmail: {}", userEmail, e);
        }
    }

    public void removeFcmToken(String userEmail, String token) {
        try {
            logger.info("Removing FCM token for userEmail: {}", userEmail);
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

            boolean removed = user.getFcmTokens().remove(token);
            if (removed) {
                userRepository.save(user);
                logger.info("Successfully removed FCM token for userId: {}", user.getId());
            } else {
                logger.warn("Attempted to remove a non-existent FCM token for userId: {}", user.getId());
            }
        } catch (Exception e) {
            logger.error("Error removing FCM token for userEmail: {}", userEmail, e);
        }
    }

    public ProfileResponse getUserProfile(String userId) {
        User user = findUserById(userId);
        return mapUserToProfileResponse(user, true); // true for private profile
    }

    public ProfileResponse getPublicUserProfile(String userId) {
        User user = findUserById(userId);
        return mapUserToProfileResponse(user, false); // false for public profile
    }

    public ProfileResponse updateUserProfile(String userId, ProfileUpdateRequest updateRequest) {
        User user = findUserById(userId);

        user.setPersonality(updateRequest.getPersonality());
        user.setMatchPreferences(updateRequest.getMatchPreferences());

        User updatedUser = userRepository.save(user);
        return mapUserToProfileResponse(updatedUser, true);
    }

    /**
     * --- NEW METHOD ---
     * Updates the user's saved match filter preferences.
     * @param userId The ID of the user to update.
     * @param filtersRequest The DTO containing the new filter settings.
     */
    public void updateMatchFilters(String userId, MatchFilters filtersRequest) {
        User user = findUserById(userId);

        MatchFilters matchFilters = new MatchFilters();
        matchFilters.setRadius(filtersRequest.getRadius());
        matchFilters.setBreeds(filtersRequest.getBreeds());
        matchFilters.setMinAge(filtersRequest.getMinAge());
        matchFilters.setMaxAge(filtersRequest.getMaxAge());
        matchFilters.setPersonality(filtersRequest.getPersonality());

        user.setMatchFilters(matchFilters);
        userRepository.save(user);
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

    public User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    /**
     * Finds a user and returns a list of their friends as User objects.
     * This is used by the WebSocket handler to know who to broadcast status updates to.
     * @param userId The ID of the user whose friends we want to find.
     * @return A list of friend User objects.
     */
    public List<User> findFriendsByUserId(String userId) {
        User user = findUserById(userId);
        if (user.getFriends() == null) {
            return new ArrayList<>(); // Return an empty list if there are no friends
        }

        return user.getFriends().stream()
                .map(friendIdString -> {
                    // This regex extracts the 24-character hexadecimal MongoDB ObjectId from the string
                    Pattern pattern = Pattern.compile("([a-f0-9]{24})");
                    Matcher matcher = pattern.matcher(friendIdString);
                    if (matcher.find()) {
                        try {
                            return findUserById(matcher.group(1));
                        } catch (RuntimeException e) {
                            // This can happen if a friend's account was deleted
                            logger.warn("Could not find user for friend entry: {}", friendIdString);
                            return null;
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull) // Filter out any nulls in case a friend was not found
                .collect(Collectors.toList());
    }

    private ProfileResponse mapUserToProfileResponse(User user, boolean isPrivate) {
        List<FriendResponse> friends = new ArrayList<>();
        if (isPrivate && user.getFriends() != null) {
            friends = user.getFriends().stream()
                    .map(friendIdString -> {
                        Pattern pattern = Pattern.compile("([a-f0-9]{24})");
                        Matcher matcher = pattern.matcher(friendIdString);
                        if (matcher.find()) {
                            try {
                                return findUserById(matcher.group(1));
                            } catch (RuntimeException e) {
                                // This can happen if a friend's account was deleted
                                return null;
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .map(friendUser -> new FriendResponse(
                            friendUser.getId(),
                            friendUser.getDogName(),
                            friendUser.getProfilePicUrl()
                    ))
                    .collect(Collectors.toList());
        }

        return new ProfileResponse(
                user.getId(),
                isPrivate ? user.getYourName() : null, // Only show owner's name on private view
                user.getDogName(),
                isPrivate ? user.getEmail() : null, // Only show email on private view
                user.getBreed(),
                user.getBirthday(),
                user.getProfilePicUrl(),
                user.getPersonality(),
                user.getMatchPreferences(),
                friends, // Friends list will be empty for public profiles
                isPrivate ? user.getMatchFilters() : null // Add match filters for private view
        );
    }
}
