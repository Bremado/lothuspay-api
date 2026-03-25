package com.lothuspay.wallet.model.withdraw.destionation;

import com.lothuspay.wallet.model.withdraw.destionation.document.WithdrawDocumentType;
import com.lothuspay.wallet.model.withdraw.destionation.subtype.WithdrawDestinationSubType;
import com.lothuspay.wallet.model.withdraw.destionation.type.WithdrawDestinationType;
import lombok.*;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawDestination {

    private String name;

    private WithdrawDocumentType documentType;
    private String document;

    private WithdrawDestinationType type;
    private WithdrawDestinationSubType subType;
    private String destination;


}
