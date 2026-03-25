package com.lothuspay.payments.dto.response.object.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetDepositStats {

    private int totalDeposits;
    private int pendingDeposits;
    private int completedDeposits;
    private int failedDeposits;

    private BigDecimal totalAmountDeposited;
    private BigDecimal totalPendingAmount;
    private BigDecimal totalFailedAmount;
    private BigDecimal totalCompletedAmount;

    private BigDecimal totalFeesCollected;
    private BigDecimal averageDepositAmount;

}
