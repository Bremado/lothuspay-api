package com.lothuspay.wallet.model.withdraw;

import com.lothuspay.wallet.model.withdraw.destionation.WithdrawDestination;
import com.lothuspay.wallet.model.withdraw.status.WithdrawStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "withdraws")
public class Withdraw {

    @Id
    private String id;
    private String walletId;
    private String referenceId;
    private String ledgerId;

    private String description;

    private WithdrawDestination destination;

    private BigDecimal subTotal;
    private BigDecimal fee;

    private BigDecimal total;

    private WithdrawStatus status;

    private LocalDateTime created;
    private LocalDateTime updated;

}
