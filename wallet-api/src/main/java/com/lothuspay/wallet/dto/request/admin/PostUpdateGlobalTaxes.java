package com.lothuspay.wallet.dto.request.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateGlobalTaxes {

    private BigDecimal percent;
    private BigDecimal fixed;

    private BigDecimal blackPercent;
    private BigDecimal blackFixed;

    private BigDecimal withdrawPercent;
    private BigDecimal withdrawFixed;

    private BigDecimal blackWithdrawPercent;
    private BigDecimal blackWithdrawFixed;


}

