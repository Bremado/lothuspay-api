package com.lothuspay.wallet.dto.response.object;

import com.lothuspay.wallet.model.deposit.Deposit;
import com.lothuspay.wallet.model.deposit.method.DepositMethod;
import com.lothuspay.wallet.model.deposit.status.DepositStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetDeposit {

    private String walletId;

    private String description;

    private DepositMethod method;

    private BigDecimal subTotal;
    private BigDecimal fee;
    private BigDecimal total;

    private String brcode;

    private DepositStatus status;

    private LocalDateTime created;
    private LocalDateTime updated;

    public GetDeposit(Deposit deposit) {
        this.walletId = deposit.getWalletId();
        this.description = deposit.getDescription();
        this.method = deposit.getMethod();
        this.subTotal = deposit.getSubTotal();
        this.fee = deposit.getFee();
        this.total = deposit.getTotal();
        this.status = deposit.getStatus();
        this.brcode = deposit.getBrcode();
        this.created = deposit.getCreated();
        this.updated = deposit.getUpdated();
    }
}
