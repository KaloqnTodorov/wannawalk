package com.wannawalk.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.wannawalk.backend.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final ProfileService profileService;

    public NotificationService(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Sends a push notification to a specific user.
     * Includes sender's information in the data payload for client-side navigation.
     *
     * @param recipientId The ID of the user to send the notification to.
     * @param senderId    The ID of the user who sent the message.
     * @param senderName  The name of the sender to display in the notification.
     * @param body        The body/content of the push notification.
     */
    public void sendNotification(String recipientId, String senderId, String senderName, String body) {
        try {
            User recipient = profileService.findUserById(recipientId);

            if (recipient.getFcmTokens() == null || recipient.getFcmTokens().isEmpty()) {
                logger.warn("No FCM tokens found for user: {}", recipientId);
                return;
            }

            String title = "New message from " + senderName;

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();

            ApnsConfig apnsConfig = ApnsConfig.builder()
                .setAps(Aps.builder().setContentAvailable(true).build())
                .build();

            logger.info("Attempting to send notification titled '{}' to user {}", title, recipientId);

            for (String token : recipient.getFcmTokens()) {
                Message message = Message.builder()
                        .setNotification(notification)
                        .putAllData(Map.of(
                            "title", title,
                            "body", body,
                            "senderId", senderId,
                            "senderName", senderName
                            // You could also add senderImageUrl here
                        ))
                        .setToken(token)
                        .setAndroidConfig(androidConfig)
                        .setApnsConfig(apnsConfig)
                        .build();
                try {
                    String response = FirebaseMessaging.getInstance().send(message);
                    logger.info("Successfully sent message to token {}: {}", token, response);
                } catch (Exception e) {
                    logger.error("Error sending message to token {}: {}", token, e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", recipientId, e.getMessage(), e);
        }
    }
}

