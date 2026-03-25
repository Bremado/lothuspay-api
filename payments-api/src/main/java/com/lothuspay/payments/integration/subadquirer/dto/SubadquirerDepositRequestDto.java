package com.lothuspay.payments.integration.subadquirer.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubadquirerDepositRequestDto {

    private BigDecimal amount;

    private String payerName;
    private String payerDocument;

    private String transactionId;

    private String projectWebhook;

    private String description;
}
