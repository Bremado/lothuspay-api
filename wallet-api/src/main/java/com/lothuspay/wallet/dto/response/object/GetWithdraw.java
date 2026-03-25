package com.lothuspay.wallet.dto.response.object;

import com.lothuspay.wallet.model.withdraw.Withdraw;
import com.lothuspay.wallet.model.withdraw.destionation.WithdrawDestination;
import com.lothuspay.wallet.model.withdraw.status.WithdrawStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetWithdraw {

    private String id;
    private String walletId;

    private String description;

    private WithdrawDestination destination;

    private BigDecimal subTotal;
    private BigDecimal fee;

    private BigDecimal total;

    private WithdrawStatus status;

    private LocalDateTime created;
    private LocalDateTime updated;

    public GetWithdraw(Withdraw withdraw) {
        this.id = withdraw.getId();
        this.walletId = withdraw.getWalletId();
        this.description = withdraw.getDescription();
        this.destination = withdraw.getDestination();
        this.subTotal = withdraw.getSubTotal();
        this.fee = withdraw.getFee();
        this.total = withdraw.getTotal();
        this.status = withdraw.getStatus();
        this.created = withdraw.getCreated();
        this.updated = withdraw.getUpdated();
    }
}
