package com.lothuspay.wallet.dto.response.object;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetSummaryReport {

    private BigDecimal totalProfit;
    private BigDecimal totalRevenue;
    private BigDecimal totalVolume;
    private Long totalUsers;
    private Long totalTransactions;
    private BigDecimal averageTicket;
    private Map<String, BigDecimal> profitByMethod;
    private Map<String, Long> transactionsByMethod;

}

