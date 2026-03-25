package com.lothuspay.payments.model.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@AllArgsConstructor
@Document(collection = "payment_config")
public class PaymentConfig {

    @Id
    private String id = "payment_config";

    private boolean allowDeposits;
    private boolean allowWithdrawals;

    private double minDepositAmount;
    private double minWithdrawalAmount;

    public PaymentConfig() {
        this.allowDeposits = true;
        this.allowWithdrawals = true;
        this.minDepositAmount = 1.0;
        this.minWithdrawalAmount = 5.0;
    }
}
