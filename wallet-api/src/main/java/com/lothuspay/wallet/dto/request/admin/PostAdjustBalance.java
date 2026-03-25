package com.lothuspay.wallet.dto.request.admin;

import com.lothuspay.wallet.model.ledger.type.LedgerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostAdjustBalance {

    private BigDecimal amount;
    private LedgerType type;
    private String description;
    private String reason;

}

