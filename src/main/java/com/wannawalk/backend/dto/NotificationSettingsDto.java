package com.wannawalk.backend.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettingsDto {
    private boolean newMatches = true;
    private boolean messages = true;
    private boolean feedActivity = false;
}
