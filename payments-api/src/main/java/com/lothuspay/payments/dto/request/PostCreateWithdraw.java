package com.lothuspay.payments.dto.request;

import com.lothuspay.payments.integration.wallet.dto.impl.GetWalletTax;
import com.lothuspay.payments.model.deposit.method.DepositRequestMethod;
import com.lothuspay.payments.model.withdraw.WithdrawRequest;
import com.lothuspay.payments.model.withdraw.destionation.WithdrawRequestDestination;
import com.lothuspay.payments.model.withdraw.destionation.document.WithdrawRequestDocumentType;
import com.lothuspay.payments.model.withdraw.destionation.type.WithdrawRequestDestinationSubType;
import com.lothuspay.payments.model.withdraw.destionation.type.WithdrawRequestDestinationType;
import com.lothuspay.payments.model.withdraw.status.WithdrawRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateWithdraw {

    private String referenceId;
    private String description;

    private Destination destination;

    private BigDecimal amount;

    private String webhook;

    public WithdrawRequest toWithdrawRequest(String walletId) {
        return WithdrawRequest.builder()
                .id(UUID.randomUUID().toString())
                .walletId(walletId)
                .referenceId(referenceId)
                .description(description)
                .destination(WithdrawRequestDestination.builder()
                        .name(destination.name)
                        .documentType(WithdrawRequestDocumentType.fromString(destination.documentType))
                        .document(destination.document)
                        .type(WithdrawRequestDestinationType.fromString(destination.type))
                        .subType(WithdrawRequestDestinationSubType.fromString(destination.subType))
                        .destination(destination.destination)
                        .build())
                .subTotal(amount)
                .fee(BigDecimal.ZERO)
                .webhook(webhook)
                .total(amount)
                .status(WithdrawRequestStatus.PENDING)
                .created(java.time.LocalDateTime.now())
                .updated(java.time.LocalDateTime.now())
                .build();
    }

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Destination {

        private String name;

        private String documentType;
        private String document;

        private String type;
        private String subType;
        private String destination;

    }
}
