package com.lothuspay.wallet.model.wallet;

import com.lothuspay.wallet.model.wallet.tax.WalletTax;
import com.lothuspay.wallet.model.wallet.tax.pix.WalletTaxPix;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "wallets")
public class Wallet {

    @Id
    private String id;
    private String userId;
    private String email;

    private BigDecimal available;
    private BigDecimal frozen;
    private BigDecimal future;

    private WalletTax tax;

    private List<WalletTaxPix> historyTaxPix;

    private LocalDateTime updated;

}
