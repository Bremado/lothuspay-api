package com.lothuspay.wallet.model.wallet.tax;

import com.lothuspay.wallet.model.wallet.tax.pix.WalletTaxPix;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletTax {

    private boolean custom;

    private WalletTaxPix pix;

}
