package com.lothuspay.wallet.dto.response.object;

import com.lothuspay.wallet.model.ledger.Ledger;
import com.lothuspay.wallet.model.ledger.status.LedgerStatus;
import com.lothuspay.wallet.model.ledger.type.LedgerType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetLedger {

    private String id;

    private String description;

    private Object transaction;

    private LedgerType type;
    private BigDecimal amount;

    private LedgerStatus status;

    private LocalDateTime created;

    public GetLedger(Ledger ledger) {
        this.id = ledger.getId();
        this.description = ledger.getDescription();
        this.type = ledger.getType();
        this.amount = ledger.getAmount();
        this.status = ledger.getStatus();
        this.created = ledger.getCreated();
    }

}
