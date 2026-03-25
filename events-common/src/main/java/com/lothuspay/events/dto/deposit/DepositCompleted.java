package com.lothuspay.events.dto.deposit;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositCompleted {

    private String id;
    private String walletId;

    private String status;

    private LocalDateTime updated;

}
