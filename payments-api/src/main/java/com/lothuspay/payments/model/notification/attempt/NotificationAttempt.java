package com.lothuspay.payments.model.notification.attempt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_attempts")
public class NotificationAttempt {

    @Id
    private String id;
    private String notificationId;

    private LocalDateTime timestamp;
    private String status;
    private String responseCode;
    private String responseBody;

}
