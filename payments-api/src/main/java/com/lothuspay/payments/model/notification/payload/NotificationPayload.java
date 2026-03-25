package com.lothuspay.payments.model.notification.payload;

import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPayload {

    private String transactionId;
    private String status;
    private double amount;
    private long timestamp;

}
