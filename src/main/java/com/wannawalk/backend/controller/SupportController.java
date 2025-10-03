package com.wannawalk.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wannawalk.backend.dto.SupportRequestDto;
import com.wannawalk.backend.security.UserPrincipal;
import com.wannawalk.backend.service.EmailService;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendSupportEmail(@AuthenticationPrincipal UserPrincipal currentUser, @RequestBody SupportRequestDto supportRequest) {
        String userEmail = currentUser.getEmail();
        String subject = "Support Request: " + supportRequest.getSubject();
        String content = "<p><b>From:</b> " + userEmail + "</p>" +
                "<p><b>Message:</b></p>" +
                "<p>" + supportRequest.getMessage().replace("\n", "<br>") + "</p>";

        emailService.sendSupportEmail(subject, content);

        return ResponseEntity.ok().build();
    }
}
