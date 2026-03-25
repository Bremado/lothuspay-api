package com.lothuspay.wallet.model.wallet.tax.pix;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletTaxPix {

    private BigDecimal percent;
    private BigDecimal fixed;

    private BigDecimal blackPercent;
    private BigDecimal blackFixed;

    private BigDecimal withdrawPercent;
    private BigDecimal withdrawFixed;

    private BigDecimal blackWithdrawPercent;
    private BigDecimal blackWithdrawFixed;

}
