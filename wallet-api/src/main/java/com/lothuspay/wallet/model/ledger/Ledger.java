package com.lothuspay.wallet.model.ledger;

import com.lothuspay.wallet.model.ledger.origin.OriginType;
import com.lothuspay.wallet.model.ledger.status.LedgerStatus;
import com.lothuspay.wallet.model.ledger.type.LedgerType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ledgers")
public class Ledger {

    @Id
    private String id;
    private String userId;

    private String walletId;

    private String description;

    private LedgerType type;
    private BigDecimal amount;

    private BigDecimal fee;

    private OriginType origin;
    private String originId;

    private LedgerStatus status;

    private LocalDateTime created;
    private LocalDateTime updated;

}
