package com.lothuspay.payments.model.notification;

import com.google.gson.Gson;
import com.lothuspay.payments.model.notification.payload.NotificationPayload;
import com.lothuspay.payments.model.notification.status.NotificationStatus;
import com.lothuspay.payments.model.notification.type.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    private NotificationType type;
    private String transactionId;

    private String url;
    private NotificationPayload payload;

    private NotificationStatus status;

    public String body() {
        return new Gson().toJson(payload);
    }
}
