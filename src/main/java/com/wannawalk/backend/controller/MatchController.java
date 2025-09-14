package com.wannawalk.backend.controller;

import com.wannawalk.backend.dto.SwipeRequest;
import com.wannawalk.backend.dto.UpdateLocationRequest;
import com.wannawalk.backend.dto.UserDto;
import com.wannawalk.backend.security.UserPrincipal;
import com.wannawalk.backend.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@CrossOrigin(origins = "*") // In a real app, configure this more securely
public class MatchController {

    @Autowired
    private MatchService matchService;

    /**
     * Updates the location for the currently authenticated user.
     * User ID is retrieved from the security principal to ensure security.
     */
    @PostMapping("/update-location")
    public ResponseEntity<String> updateLocation(
            @RequestBody UpdateLocationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        // Get user ID from the security context to ensure users can only update their own location.
        matchService.updateLocation(currentUser.getId(), request.getLongitude(), request.getLatitude());
        return ResponseEntity.ok("Location updated");
    }

    /**
     * Retrieves a list of potential matches for the currently authenticated user.
     */
    @GetMapping("/matches") // Removed userId from path
    public ResponseEntity<List<UserDto>> getMatches(
            @AuthenticationPrincipal UserPrincipal currentUser, // Get user from security context
            @RequestParam(defaultValue = "10") double radius) {
        // Fetch matches for the currently authenticated user
        List<UserDto> matches = matchService.getMatches(currentUser.getId(), radius);
        return ResponseEntity.ok(matches);
    }

    /**
     * Handles a swipe action (like/dislike) from the currently authenticated user.
     */
    @PostMapping("/swipe")
    public ResponseEntity<Map<String, Object>> swipe(
            @RequestBody SwipeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) { // Get user from security context
        // The "swiper" is the authenticated user, identified by the principal.
        Map<String, Object> response = matchService.handleSwipe(
                currentUser.getId(),
                request.getSwipedUserId(),
                request.getDirection());
        return ResponseEntity.ok(response);
    }
}
