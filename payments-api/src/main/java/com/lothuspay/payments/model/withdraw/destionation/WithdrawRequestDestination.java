package com.lothuspay.payments.model.withdraw.destionation;

import com.lothuspay.payments.model.withdraw.destionation.document.WithdrawRequestDocumentType;
import com.lothuspay.payments.model.withdraw.destionation.type.WithdrawRequestDestinationSubType;
import com.lothuspay.payments.model.withdraw.destionation.type.WithdrawRequestDestinationType;
import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequestDestination {

    private String name;

    private WithdrawRequestDocumentType documentType;
    private String document;

    private WithdrawRequestDestinationType type;
    private WithdrawRequestDestinationSubType subType;
    private String destination;

}
