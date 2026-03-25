package com.lothuspay.payments.integration.subadquirer.dto;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubadquirerWithdrawRequestDto {

    private String description;
    private String pixKey;
    private String pixKeyType;
    private String projectWebhook;
    private Double amount;

}
