package com.lothuspay.payments.model.withdraw;

import com.lothuspay.payments.integration.wallet.dto.impl.GetWalletTax;
import com.lothuspay.payments.model.withdraw.destionation.WithdrawRequestDestination;
import com.lothuspay.payments.model.withdraw.status.WithdrawRequestStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "withdraw_requests")
public class WithdrawRequest {

    @Id
    private String id;
    private String walletId;
    private String referenceId;

    private String description;

    private WithdrawRequestDestination destination;

    private BigDecimal subTotal;
    private BigDecimal fee;

    private BigDecimal total;

    private String webhook;

    private WithdrawRequestStatus status;

    private LocalDateTime created;
    private LocalDateTime updated;

    public BigDecimal calculateFee(String segment, GetWalletTax tax) {
        if (tax == null) {
            return BigDecimal.ZERO;
        }

        if (tax.getPix() == null) {
            return BigDecimal.ZERO;
        }

        var percent = (segment.equals("NONE") || segment.equalsIgnoreCase("WHITELABEL") ? tax.getPix().getWithdrawPercent() : tax.getPix().getBlackWithdrawPercent());
        var percentAmount = subTotal.multiply(percent);


        return percentAmount;
    }
}
