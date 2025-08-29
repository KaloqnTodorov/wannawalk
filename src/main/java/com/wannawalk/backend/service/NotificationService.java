package com.wannawalk.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.wannawalk.backend.model.User;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final ActiveUserTracker activeUserTracker;
    // --- MODIFIED: Injected ProfileService ---
    private final ProfileService profileService;

    public NotificationService(ActiveUserTracker activeUserTracker, ProfileService profileService) {
        this.activeUserTracker = activeUserTracker;
        this.profileService = profileService;
    }

    public void sendNotificationIfUserIsOffline(String recipientId, String title, String body) {
        if (activeUserTracker.isUserActive(recipientId)) {
            System.out.println("User " + recipientId + " is online. Skipping push notification.");
            return;
        }

        System.out.println("User " + recipientId + " is offline. Attempting to send push notification.");
        
        // --- MODIFIED: This now needs to get the User object from the ProfileService ---
        // Note: Your ProfileService doesn't have a direct findById, so we'll assume
        // you add one or call it through the repository. For this example, I'll use a placeholder.
        // You would replace this with: User recipient = profileService.findUserById(recipientId);
        User recipient = profileService.findUserById(recipientId); // You need to make findUserById public or create a new method.

        if (recipient == null || recipient.getFcmTokens() == null || recipient.getFcmTokens().isEmpty()) {
            System.out.println("No FCM tokens found for user: " + recipientId);
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        for (String token : recipient.getFcmTokens()) {
            Message message = Message.builder()
                    .setNotification(notification)
                    .setToken(token)
                    .build();
            try {
                String response = FirebaseMessaging.getInstance().send(message);
                System.out.println("Successfully sent message to token " + token + ": " + response);
            } catch (Exception e) {
                System.err.println("Error sending message to token " + token + ": " + e.getMessage());
            }
        }
    }
}

