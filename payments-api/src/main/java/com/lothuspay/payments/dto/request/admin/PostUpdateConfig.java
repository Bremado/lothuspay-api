package com.lothuspay.payments.dto.request.admin;

import com.lothuspay.payments.model.config.PaymentConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateConfig {

    private boolean allowDeposits;
    private boolean allowWithdrawals;

    private double minDepositAmount;
    private double minWithdrawalAmount;

    public PaymentConfig toPaymentConfig() {
        return new PaymentConfig(
                "payment_config",
                allowDeposits,
                allowWithdrawals,
                minDepositAmount,
                minWithdrawalAmount
        );
    }
}
