package com.lothuspay.payments.dto.response.object.general;

import com.lothuspay.payments.model.deposit.DepositRequest;
import com.lothuspay.payments.model.deposit.status.DepositRequestStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetDeposit {

    private String id;
    private String description;

    private BigDecimal subTotal;
    private BigDecimal fee;

    private BigDecimal total;

    private String brcode;

    private DepositRequestStatus status;

    private LocalDateTime created;
    private LocalDateTime updated;

    public GetDeposit(DepositRequest request) {
        this.id = request.getId();
        this.description = request.getDescription();
        this.subTotal = request.getSubTotal();
        this.fee = request.getFee();
        this.total = request.getTotal();
        this.brcode = request.getBrcode();
        this.status = request.getStatus();
        this.created = request.getCreated();
        this.updated = request.getUpdated();
    }
}
