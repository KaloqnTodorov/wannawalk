package com.wannawalk.backend.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSettings {
    private boolean newMatches = true;
    private boolean messages = true;
    private boolean feedActivity = false;
}
