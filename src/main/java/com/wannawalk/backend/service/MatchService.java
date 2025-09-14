package com.wannawalk.backend.service;

import com.wannawalk.backend.model.User;
import com.wannawalk.backend.dto.UserDto;
import com.wannawalk.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchService {

    private static final String USER_GEO_KEY = "user_locations";
    private static final String SWIPES_KEY_PREFIX = "swipes:";
    private static final String MATCHES_KEY_PREFIX = "matches:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendService friendService;

    @Autowired
    private NotificationService notificationService;

    private int calculateAge(Date birthDate) {
        if (birthDate == null) return 0;
        LocalDate birthLocalDate = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birthLocalDate, LocalDate.now()).getYears();
    }

    private UserDto convertToUserDto(User user) {
        if (user == null) return null;
        return new UserDto(
                user.getId(),
                user.getDogName(),
                calculateAge(user.getBirthday()),
                user.getBreed(),
                user.getProfilePicUrl(),
                user.getPersonality(),
                user.getMatchPreferences()
        );
    }

    public void updateLocation(String userId, double longitude, double latitude) {
        redisTemplate.opsForGeo().add(USER_GEO_KEY, new Point(longitude, latitude), userId);
        System.out.println("Updated location for " + userId + ": " + longitude + ", " + latitude);
    }

    public List<UserDto> getMatches(String userId, double radiusKm) {
        List<Point> userPositionList = redisTemplate.opsForGeo().position(USER_GEO_KEY, userId);
        if (userPositionList == null || userPositionList.isEmpty() || userPositionList.get(0) == null) {
            return Collections.emptyList();
        }
        Point userPosition = userPositionList.get(0);

        Circle within = new Circle(userPosition, new Distance(radiusKm, Metrics.KILOMETERS));
        var geoResults = redisTemplate.opsForGeo().radius(USER_GEO_KEY, within);
        if (geoResults == null) return Collections.emptyList();

        Set<String> swipedRight = redisTemplate.opsForSet().members(SWIPES_KEY_PREFIX + userId + ":right");
        Set<String> swipedLeft = redisTemplate.opsForSet().members(SWIPES_KEY_PREFIX + userId + ":left");
        Set<String> alreadySwiped = new HashSet<>();
        if (swipedRight != null) alreadySwiped.addAll(swipedRight);
        if (swipedLeft != null) alreadySwiped.addAll(swipedLeft);

        List<String> potentialMatchIds = geoResults.getContent().stream()
                .map(result -> result.getContent().getName())
                .filter(memberId -> !memberId.equals(userId) && !alreadySwiped.contains(memberId))
                .collect(Collectors.toList());

        if (potentialMatchIds.isEmpty()) return Collections.emptyList();

        return userRepository.findAllById(potentialMatchIds).stream()
                .map(this::convertToUserDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Map<String, Object> handleSwipe(String userId, String swipedUserId, String direction) {
        String swipeKey = SWIPES_KEY_PREFIX + userId + ":" + direction.toLowerCase();
        redisTemplate.opsForSet().add(swipeKey, swipedUserId);
        System.out.println(userId + " swiped " + direction + " on " + swipedUserId);

        Map<String, Object> response = new HashMap<>();
        response.put("isMatch", false);

        if ("right".equalsIgnoreCase(direction)) {
            String otherUserSwipesKey = SWIPES_KEY_PREFIX + swipedUserId + ":right";
            Boolean hasMutualLike = redisTemplate.opsForSet().isMember(otherUserSwipesKey, userId);

            if (Boolean.TRUE.equals(hasMutualLike)) {
                System.out.println("It's a match between " + userId + " and " + swipedUserId + "!");
                redisTemplate.opsForSet().add(MATCHES_KEY_PREFIX + userId, swipedUserId);
                redisTemplate.opsForSet().add(MATCHES_KEY_PREFIX + swipedUserId, userId);
                friendService.addFriend(userId, swipedUserId);

                Optional<User> userA_Opt = userRepository.findById(userId);
                Optional<User> userB_Opt = userRepository.findById(swipedUserId);

                if (userA_Opt.isPresent() && userB_Opt.isPresent()) {
                    User userA = userA_Opt.get(); // The user who just swiped
                    User userB = userB_Opt.get(); // The user who was swiped upon

                    notificationService.sendNotification(
                            swipedUserId,
                            userId,
                            userA.getDogName(),
                            "You have a new match with " + userA.getDogName() + "!"
                    );

                    response.put("isMatch", true);
                    response.put("message", "It's a match with " + userB.getDogName() + "!");
                } else {
                    response.put("isMatch", true);
                    response.put("message", "It's a match!");
                }
            }
        }
        return response;
    }
}

