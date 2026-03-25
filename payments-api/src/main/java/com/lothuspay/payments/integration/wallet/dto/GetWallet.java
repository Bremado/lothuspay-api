package com.lothuspay.payments.integration.wallet.dto;

import com.lothuspay.payments.integration.wallet.dto.impl.GetWalletTax;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetWallet {

    private String walletId;

    private BigDecimal available;
    private BigDecimal frozen;
    private BigDecimal future;

    private GetWalletTax tax;

}
