package com.lothuspay.events.dto.deposit;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositCreated {

    private String id;
    private String walletId;
    private String referenceId;

    private String description;

    private String method;

    private BigDecimal subTotal;
    private BigDecimal fee;

    private BigDecimal total;

    private String brcode;

    private String webhook;

    private String status;

    private LocalDateTime created;
    private LocalDateTime updated;
}
