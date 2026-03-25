package com.lothuspay.wallet.service.internal;

import com.lothuspay.wallet.dto.response.DtoResponse;
import com.lothuspay.wallet.dto.response.object.GetWallet;
import com.lothuspay.wallet.dto.response.status.DtoResponseStatus;
import com.lothuspay.wallet.model.ledger.status.LedgerStatus;
import com.lothuspay.wallet.model.wallet.Wallet;
import com.lothuspay.wallet.model.wallet.tax.WalletTax;
import com.lothuspay.wallet.model.wallet.tax.pix.WalletTaxPix;
import com.lothuspay.wallet.pojo.UserContext;
import com.lothuspay.wallet.repository.deposit.DepositRepository;
import com.lothuspay.wallet.repository.ledger.LedgerRepository;
import com.lothuspay.wallet.repository.wallet.WalletRepository;
import com.lothuspay.wallet.repository.withdraw.WithdrawRepository;
import com.lothuspay.wallet.service.config.ConfigService;
import com.lothuspay.wallet.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalService {

    private final ConfigService configService;
    private final WalletRepository walletRepository;
    private final LedgerRepository ledgerRepository;

    public Mono<GetWallet> wallet(String userId) {
        return walletRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() ->
                        createWallet(userId)
                ))
                .flatMap(wallet -> ledgerRepository.findAllByWalletIdAndStatus(wallet.getId(), LedgerStatus.APPROVED)
                                .collectList()
                                .flatMap(ledgers -> {
                                    var totalFee = ledgers.stream()
                                            .map(ledger -> ledger.getFee() != null ? ledger.getFee() : BigDecimal.ZERO)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    var totalTransaction = ledgers.stream()
                                            .map(ledger -> ledger.getAmount() != null ? ledger.getAmount() : BigDecimal.ZERO)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    var totalTicketMedium = ledgers.stream()
                                            .map(ledger -> ledger.getAmount() != null ? ledger.getAmount() : BigDecimal.ZERO)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                                    var count = ledgers.size();

                                    var ticketMedium = count == 0
                                            ? BigDecimal.ZERO
                                            : totalTicketMedium.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

                                    return configService.config().flatMap(config -> {
                                        if (wallet.getTax().getPix().getBlackPercent() == null) {
                                            wallet.getTax().getPix().setBlackPercent(config.getWalletTaxPix().getBlackPercent());
                                        }
                                        if (wallet.getTax().getPix().getBlackFixed() == null) {
                                            wallet.getTax().getPix().setBlackFixed(config.getWalletTaxPix().getBlackFixed());
                                        }
                                        if (wallet.getTax().getPix().getBlackWithdrawFixed() == null) {
                                            wallet.getTax().getPix().setBlackWithdrawFixed(config.getWalletTaxPix().getBlackWithdrawFixed());
                                        }
                                        if (wallet.getTax().getPix().getBlackWithdrawPercent() == null) {
                                            wallet.getTax().getPix().setBlackWithdrawPercent(config.getWalletTaxPix().getBlackWithdrawPercent());
                                        }

                                        return Mono.just(
                                                new GetWallet(wallet, totalFee, totalTransaction, ticketMedium)
                                        );
                                    });
                                })
                );
    }

    private Mono<Wallet> createWallet(String userId) {
        return walletRepository.findByUserId(userId)
                .switchIfEmpty(Mono.defer(() -> configService.config().flatMap(config -> {
                    Wallet wallet = Wallet.builder()
                            .id(UUID.randomUUID().toString())
                            .userId(userId)
                            .available(BigDecimal.ZERO)
                            .frozen(BigDecimal.ZERO)
                            .future(BigDecimal.ZERO)
                            .tax(new WalletTax(false, new WalletTaxPix(
                                    config.getWalletTaxPix().getPercent(), config.getWalletTaxPix().getFixed(),
                                    config.getWalletTaxPix().getBlackPercent(), config.getWalletTaxPix().getBlackFixed(),
                                    config.getWalletTaxPix().getWithdrawPercent(), config.getWalletTaxPix().getWithdrawFixed(),
                                    config.getWalletTaxPix().getBlackWithdrawPercent(), config.getWalletTaxPix().getBlackWithdrawFixed()
                            )))
                            .updated(LocalDateTime.now())
                            .build();
                    return walletRepository.save(wallet);
                })))
                .flatMap(Mono::just);
    }
}
