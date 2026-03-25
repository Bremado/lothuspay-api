package com.lothuspay.payments.model.deposit;

import com.lothuspay.payments.integration.wallet.dto.impl.GetWalletTax;
import com.lothuspay.payments.model.deposit.method.DepositRequestMethod;
import com.lothuspay.payments.model.deposit.payer.DepositPayer;
import com.lothuspay.payments.model.deposit.status.DepositRequestStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deposit_requests")
public class DepositRequest {

    @Id
    private String id;
    private String walletId;
    private String referenceId;

    private String description;

    private DepositPayer payer;

    private DepositRequestMethod method;

    private BigDecimal subTotal;
    private BigDecimal fee;

    private BigDecimal total;

    private String brcode;

    private String webhook;

    private DepositRequestStatus status;

    private LocalDateTime created;
    private LocalDateTime updated;

    public BigDecimal calculateFee(String segment, GetWalletTax tax) {
        if (tax == null) {
            return BigDecimal.ZERO;
        }

        if (method.equals(DepositRequestMethod.PIX)) {
            if (tax.getPix() == null) {
                return BigDecimal.ZERO;
            }

            var percentAmount = subTotal.multiply(
                    (segment.equals("NONE") || segment.equalsIgnoreCase("WHITELABEL") ? tax.getPix().getPercent() : tax.getPix().getBlackPercent())
            );
            return percentAmount.add(
                    (segment.equals("NONE") || segment.equalsIgnoreCase("WHITELABEL") ? tax.getPix().getFixed() : tax.getPix().getBlackFixed())
            );
        }

        return BigDecimal.ZERO;
    }
}
