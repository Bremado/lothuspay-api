package com.lothuspay.payments.dto.request;

import com.lothuspay.payments.model.deposit.DepositRequest;
import com.lothuspay.payments.model.deposit.method.DepositRequestMethod;
import com.lothuspay.payments.model.deposit.payer.DepositPayer;
import com.lothuspay.payments.model.deposit.status.DepositRequestStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDeposit {

    private String referenceId;

    private String description;

    private Payer payer;

    private BigDecimal amount;

    private String webhook;

    public DepositRequest toDepositRequest(String walletId) {
        return DepositRequest.builder()
                .id(UUID.randomUUID().toString())
                .walletId(walletId)
                .referenceId(referenceId)
                .description(description)
                .method(DepositRequestMethod.PIX)
                .subTotal(amount)
                .payer(DepositPayer.builder().name(payer.name)
                        .document(payer.document)
                        .email(payer.email)
                        .build())
                .fee(BigDecimal.ZERO)
                .total(amount)
                .brcode("")
                .webhook(webhook)
                .status(DepositRequestStatus.PENDING)
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();
    }

    @Getter @Setter
    public static class Payer {

        private String name;
        private String document;
        private String email;

    }
}