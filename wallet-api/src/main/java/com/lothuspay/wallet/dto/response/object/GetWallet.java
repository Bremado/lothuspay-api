package com.lothuspay.wallet.dto.response.object;

import com.lothuspay.wallet.model.wallet.Wallet;
import com.lothuspay.wallet.model.wallet.tax.WalletTax;
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

    private BigDecimal totalFee;
    private BigDecimal totalTransaction;
    private BigDecimal totalTicketMedium;

    private WalletTax tax;

    public GetWallet(Wallet wallet, BigDecimal totalFee, BigDecimal totalTransaction, BigDecimal totalTicketMedium) {
        this.walletId = wallet.getId();
        this.available = wallet.getAvailable();
        this.frozen = wallet.getFrozen();
        this.future = wallet.getFuture();
        this.totalFee = totalFee;
        this.totalTransaction = totalTransaction;
        this.totalTicketMedium = totalTicketMedium;
        this.tax = wallet.getTax();
    }
}
