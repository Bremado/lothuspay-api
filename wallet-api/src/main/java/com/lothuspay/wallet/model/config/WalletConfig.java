package com.lothuspay.wallet.model.config;

import com.lothuspay.wallet.model.wallet.tax.pix.WalletTaxPix;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Builder
@Getter @Setter
@AllArgsConstructor
@Document(collection = "config")
public class WalletConfig {

    @Id
    private String id = "wallet_config";

    private WalletTaxPix walletTaxPix;

    public WalletConfig() {
        this.walletTaxPix = new WalletTaxPix(
                BigDecimal.valueOf(0.018D), BigDecimal.valueOf(0.70D),
                BigDecimal.valueOf(0.05D), BigDecimal.valueOf(1.05D),
                BigDecimal.valueOf(0D), BigDecimal.valueOf(1D),
                BigDecimal.valueOf(0D), BigDecimal.valueOf(1D));
    }
}
