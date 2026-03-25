package com.lothuspay.events.dto.withdraw;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawCompleted {

    private String id;
    private String walletId;

    private String status;

    private LocalDateTime updated;

}
