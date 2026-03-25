package com.lothuspay.payments.integration.subadquirer.dto;

import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubacquirerResponseDto {

    private String transactionId;

    private Payer payer;

    private double transactionFee;
    private String transactionType;
    private String transactionMethod;
    private double transactionAmount;

    private String transactionState;

    private String qrcodeUrl;
    private String copyPaste;

    public String toString() {
        return "SubacquirerResponseDto{transactionId='" + transactionId + "', payer={" + payer.toString() +
                "}, transactionFee=" + transactionFee +
                ", transactionType='" + transactionType + '\'' +
                ", transactionMethod='" + transactionMethod + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", transactionState='" + transactionState + '\'' +
                ", qrcodeUrl='" + qrcodeUrl + '\'' +
                ", copyPaste='" + copyPaste + '\'' +
                '}';
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Payer {
        private String name;
        private String document;
    }
}
