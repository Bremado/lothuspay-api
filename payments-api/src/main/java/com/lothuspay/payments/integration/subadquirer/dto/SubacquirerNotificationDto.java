package com.lothuspay.payments.integration.subadquirer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubacquirerNotificationDto {

    private Status status;
    private String transactionId;

    private Double value;

    private long timestamp;

    public static enum Status {
        APPROVED,
        DECLINED,
        PENDING
    }
}
