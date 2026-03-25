package com.lothuspay.payments.dto.response.object.admin;

import com.lothuspay.payments.model.config.PaymentConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetConfig {

    private boolean allowDeposits;
    private boolean allowWithdrawals;

    private double minDepositAmount;
    private double minWithdrawalAmount;

    public GetConfig(PaymentConfig config) {
        this.allowDeposits = config.isAllowDeposits();
        this.allowWithdrawals = config.isAllowWithdrawals();
        this.minDepositAmount = config.getMinDepositAmount();
        this.minWithdrawalAmount = config.getMinWithdrawalAmount();
    }
}
