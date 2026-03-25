package com.lothuspay.events.dto.withdraw;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawCreated {

    private String id;
    private String walletId;
    private String referenceId;

    private String description;

    private Destination destination;

    private BigDecimal subTotal;
    private BigDecimal fee;

    private BigDecimal total;

    private String status;

    private LocalDateTime created;
    private LocalDateTime updated;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Destination {

        private String name;

        private String documentType;
        private String document;

        private String type;
        private String subType;
        private String destination;

    }
}
