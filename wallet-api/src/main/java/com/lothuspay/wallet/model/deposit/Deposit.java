package com.lothuspay.wallet.model.deposit;

import com.lothuspay.wallet.model.deposit.method.DepositMethod;
import com.lothuspay.wallet.model.deposit.status.DepositStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deposits")
public class Deposit {

    @Id
    private String id;
    private String walletId;
    private String referenceId;
    private String ledgerId;

    private String description;

    private DepositMethod method;

    private BigDecimal subTotal;
    private BigDecimal fee;

    private BigDecimal total;

    private String brcode;

    private String webhook;

    private DepositStatus status;

    private LocalDateTime created;
    private LocalDateTime updated;

}
