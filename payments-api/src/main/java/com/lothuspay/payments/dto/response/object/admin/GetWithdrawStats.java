package com.lothuspay.payments.dto.response.object.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetWithdrawStats {

    private int totalWithdrawals;
    private int completedWithdrawals;
    private int failedWithdrawals;
    private int pendingWithdrawals;

    private BigDecimal totalAmountWithdrawn;
    private BigDecimal totalAmountPending;

    private BigDecimal totalFeesCollected;
    private BigDecimal averageWithdrawalAmount;

}
