package com.wannawalk.backend.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.wannawalk.backend.model.NotificationSettings;
import com.wannawalk.backend.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final ProfileService profileService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    public NotificationService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public void sendMatchNotification(String recipientId, String senderId, String senderName) {
        User recipient = profileService.findUserById(recipientId);
        NotificationSettings settings = recipient.getNotificationSettings();
        // Send if settings are null (default on) or if newMatches is true
        if (settings == null || settings.isNewMatches()) {
            String title = "You have a new match! 🐾";
            String body = "You and " + senderName + " have both swiped right!";
            sendNotification(recipient, senderId, title, body);
        } else {
            logger.info("Skipping match notification for user {} due to their settings.", recipientId);
        }
    }

    public void sendChatMessageNotification(String recipientId, String senderId, String senderName, String messageBody) {
        User recipient = profileService.findUserById(recipientId);
        NotificationSettings settings = recipient.getNotificationSettings();
        if (settings == null || settings.isMessages()) {
            String title = "New message from " + senderName;
            sendNotification(recipient, senderId, title, messageBody);
        } else {
            logger.info("Skipping chat message notification for user {} due to their settings.", recipientId);
        }
    }

    public void sendPostLikeNotification(String recipientId, String senderId, String senderName) {
        User recipient = profileService.findUserById(recipientId);
        NotificationSettings settings = recipient.getNotificationSettings();
        if (settings == null || settings.isFeedActivity()) {
            String title = "Someone liked your post!";
            String body = senderName + " liked your post.";
            sendNotification(recipient, senderId, title, body);
        } else {
            logger.info("Skipping post like notification for user {} due to their settings.", recipientId);
        }
    }

    public void sendPostCommentNotification(String recipientId, String senderId, String senderName, String commentText) {
        User recipient = profileService.findUserById(recipientId);
        NotificationSettings settings = recipient.getNotificationSettings();
        if (settings == null || settings.isFeedActivity()) {
            String title = "New comment on your post";
            String body = senderName + " commented: \"" + commentText + "\"";
            sendNotification(recipient, senderId, title, body);
        } else {
            logger.info("Skipping post comment notification for user {} due to their settings.", recipientId);
        }
    }


    private void sendNotification(User recipient, String senderId, String title, String body) {
        if (recipient.getFcmTokens() == null || recipient.getFcmTokens().isEmpty()) {
            logger.warn("No FCM tokens found for user: {}", recipient.getId());
            return;
        }

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

        logger.info("Attempting to send notification titled '{}' to user {}", title, recipient.getId());

        for (String token : recipient.getFcmTokens()) {
            executorService.submit(() -> {
                Message message = Message.builder()
                        .setNotification(notification)
                        .putAllData(Map.of(
                                "title", title,
                                "body", body,
                                "senderId", senderId
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
            });
        }
    }
}
