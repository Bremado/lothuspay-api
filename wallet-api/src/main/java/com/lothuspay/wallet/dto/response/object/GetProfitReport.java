package com.lothuspay.wallet.dto.response.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetProfitReport {

    private BigDecimal totalProfit;
    private BigDecimal totalRevenue;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalFees;
    private BigDecimal depositFees;
    private BigDecimal withdrawalFees;
    private Long totalTransactions;
    private Long totalDepositCount;
    private Long totalWithdrawalCount;
    private BigDecimal averageTicket;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

}

