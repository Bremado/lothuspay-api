package com.lothuspay.payments.integration.wallet.dto.impl;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class GetWalletTax {

    private boolean custom;

    private GetWalletTaxPix pix;

    @Getter @Setter
    public static class GetWalletTaxPix {

        private BigDecimal fixed;
        private BigDecimal percent;

        private BigDecimal blackPercent;
        private BigDecimal blackFixed;

        private BigDecimal withdrawPercent;
        private BigDecimal withdrawFixed;

        private BigDecimal blackWithdrawPercent;
        private BigDecimal blackWithdrawFixed;

    }
}
