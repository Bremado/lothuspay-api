package com.lothuspay.wallet.service.wallet;

import com.lothuspay.events.dto.deposit.DepositCompleted;
import com.lothuspay.events.dto.deposit.DepositCreated;
import com.lothuspay.events.dto.deposit.DepositUpdated;
import com.lothuspay.events.dto.email.EmailSend;
import com.lothuspay.events.dto.withdraw.WithdrawCompleted;
import com.lothuspay.events.dto.withdraw.WithdrawCreated;
import com.lothuspay.wallet.dto.response.DtoResponse;
import com.lothuspay.wallet.dto.response.object.GetDeposit;
import com.lothuspay.wallet.dto.response.object.GetWallet;
import com.lothuspay.wallet.dto.response.object.GetWithdraw;
import com.lothuspay.wallet.dto.response.status.DtoResponseStatus;
import com.lothuspay.wallet.event.publisher.EventPublisher;
import com.lothuspay.wallet.model.deposit.Deposit;
import com.lothuspay.wallet.model.deposit.method.DepositMethod;
import com.lothuspay.wallet.model.deposit.status.DepositStatus;
import com.lothuspay.wallet.model.ledger.Ledger;
import com.lothuspay.wallet.model.ledger.origin.OriginType;
import com.lothuspay.wallet.model.ledger.status.LedgerStatus;
import com.lothuspay.wallet.model.ledger.type.LedgerType;
import com.lothuspay.wallet.model.wallet.Wallet;
import com.lothuspay.wallet.model.wallet.tax.WalletTax;
import com.lothuspay.wallet.model.wallet.tax.pix.WalletTaxPix;
import com.lothuspay.wallet.model.withdraw.Withdraw;
import com.lothuspay.wallet.model.withdraw.destionation.WithdrawDestination;
import com.lothuspay.wallet.model.withdraw.destionation.document.WithdrawDocumentType;
import com.lothuspay.wallet.model.withdraw.destionation.subtype.WithdrawDestinationSubType;
import com.lothuspay.wallet.model.withdraw.destionation.type.WithdrawDestinationType;
import com.lothuspay.wallet.model.withdraw.status.WithdrawStatus;
import com.lothuspay.wallet.pojo.UserContext;
import com.lothuspay.wallet.repository.config.ConfigRepository;
import com.lothuspay.wallet.repository.deposit.DepositRepository;
import com.lothuspay.wallet.repository.ledger.LedgerRepository;
import com.lothuspay.wallet.repository.wallet.WalletRepository;
import com.lothuspay.wallet.repository.withdraw.WithdrawRepository;
import com.lothuspay.wallet.service.config.ConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final DepositRepository depositRepository;
    private final WithdrawRepository withdrawRepository;
    private final ConfigService configService;
    private final LedgerRepository ledgerRepository;
    private final EventPublisher publisher;

    private Mono<Wallet> createWallet(UserContext context) {
        return walletRepository.findByUserId(context.getUserId())
                .switchIfEmpty(Mono.defer(() -> configService.config().flatMap(config -> {
                    Wallet wallet = Wallet.builder()
                            .id(UUID.randomUUID().toString())
                            .userId(context.getUserId())
                            .email(context.getEmail())
                            .available(BigDecimal.ZERO)
                            .frozen(BigDecimal.ZERO)
                            .future(BigDecimal.ZERO)
                            .tax(new WalletTax(false, new WalletTaxPix(
                                    config.getWalletTaxPix().getPercent(), config.getWalletTaxPix().getFixed(),
                                    config.getWalletTaxPix().getBlackPercent(), config.getWalletTaxPix().getBlackFixed(),
                                    config.getWalletTaxPix().getWithdrawPercent(), config.getWalletTaxPix().getWithdrawFixed(),
                                    config.getWalletTaxPix().getBlackWithdrawPercent(), config.getWalletTaxPix().getBlackWithdrawFixed(
                            ))))
                            .updated(LocalDateTime.now())
                            .build();
                    return walletRepository.save(wallet);
                })))
                .flatMap(Mono::just);
    }

    public Mono<DtoResponse> wallet(UserContext context) {
        return walletRepository.findByUserId(context.getUserId())
                .switchIfEmpty(Mono.defer(() ->
                        createWallet(context)
                ))
                .flatMap(wallet -> configService.config().flatMap(config -> {
                            wallet.setEmail(context.getEmail());
                            if (!wallet.getTax().isCustom()) {
                                wallet.setTax(
                                        new WalletTax(false,
                                                new WalletTaxPix(
                                                        config.getWalletTaxPix().getPercent(), config.getWalletTaxPix().getFixed(),
                                                        config.getWalletTaxPix().getBlackPercent(), config.getWalletTaxPix().getBlackFixed(),
                                                        config.getWalletTaxPix().getWithdrawPercent(), config.getWalletTaxPix().getWithdrawFixed(),
                                                        config.getWalletTaxPix().getBlackWithdrawPercent(), config.getWalletTaxPix().getBlackWithdrawFixed()
                                                )
                                        )
                                );
                            }
                            return ledgerRepository.findAllByWalletIdAndStatus(wallet.getId(), LedgerStatus.APPROVED)
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

                                        return Mono.just(
                                                DtoResponse.builder()
                                                        .status(DtoResponseStatus.SUCCESS)
                                                        .data(new GetWallet(wallet, totalFee, totalTransaction, ticketMedium))
                                                        .message("Carteira recuperada com sucesso.")
                                                        .build()
                                        );
                                    });
                        })
                );
    }
    public Mono<DtoResponse> deposits(UserContext context, int page, int size) {
        return walletRepository.findByUserId(context.getUserId())
                .switchIfEmpty(Mono.defer(() -> createWallet(context)))
                .flatMap(wallet ->
                        depositRepository.findAllByWalletId(wallet.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created")))
                                .collectList()
                                .map(depositList ->
                                        DtoResponse.builder()
                                                .status(DtoResponseStatus.SUCCESS)
                                                .data(
                                                        depositList.stream()
                                                                .map(GetDeposit::new)
                                                                .toList()
                                                )
                                                .message(DtoResponseStatus.SUCCESS.getMessage())
                                                .build()
                                )
                );

    }
    public Mono<DtoResponse> withdrawals(UserContext context, int page, int size) {
        return walletRepository.findByUserId(context.getUserId())
                .switchIfEmpty(Mono.defer(() -> createWallet(context)))
                .flatMap(wallet ->
                        withdrawRepository.findAllByWalletId(wallet.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "created")))
                                .collectList()
                                .map(withdrawList ->
                                        DtoResponse.builder()
                                                .status(DtoResponseStatus.SUCCESS)
                                                .data(
                                                        withdrawList.stream()
                                                                .map(GetWithdraw::new)
                                                                .toList()
                                                )
                                                .message(DtoResponseStatus.SUCCESS.getMessage())
                                                .build()
                                )
                );
    }
    public Mono<DtoResponse> depositDetails(UserContext context, String id) {
        return walletRepository.findByUserId(context.getUserId())
                .switchIfEmpty(Mono.defer(() -> createWallet(context)))
                .flatMap(wallet ->
                        depositRepository.findById(id)
                                .flatMap(deposit ->
                                        Mono.just(
                                                DtoResponse.builder()
                                                        .status(DtoResponseStatus.SUCCESS)
                                                        .data(new GetDeposit(deposit))
                                                        .message(DtoResponseStatus.SUCCESS.getMessage())
                                                        .build()
                                        )
                                )
                                .switchIfEmpty(
                                        Mono.just(
                                                DtoResponse.builder()
                                                        .status(DtoResponseStatus.ERR_DEPOSIT_001)
                                                        .message("Depósito não encontrado.")
                                                        .build()
                                        )
                                )
                );
    }
    public Mono<DtoResponse> withdrawDetails(UserContext context, String id) {
        return walletRepository.findByUserId(context.getUserId())
                .switchIfEmpty(Mono.defer(() -> createWallet(context)))
                .flatMap(wallet ->
                        withdrawRepository.findById(id)
                                .flatMap(withdraw ->
                                        Mono.just(
                                                DtoResponse.builder()
                                                        .status(DtoResponseStatus.SUCCESS)
                                                        .data(new GetWithdraw(withdraw))
                                                        .message(DtoResponseStatus.SUCCESS.getMessage())
                                                        .build()
                                        )
                                )
                                .switchIfEmpty(
                                        Mono.just(
                                                DtoResponse.builder()
                                                        .status(DtoResponseStatus.ERR_WITHDRAWAL_001)
                                                        .message("Saque não encontrado.")
                                                        .build()
                                        )
                                )
                );
    }

    public Mono<Void> createDeposit(DepositCreated dto) {
        var ledger = Ledger.builder()
                .id(UUID.randomUUID().toString())
                .userId("")
                .walletId(dto.getWalletId())
                .description(dto.getDescription())
                .type(LedgerType.CREDIT)
                .amount(dto.getTotal())
                .fee(dto.getFee())
                .origin(OriginType.PAYMENT)
                .originId(dto.getId())
                .status(LedgerStatus.PENDING)
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();

        return ledgerRepository.save(ledger).flatMap(i -> {
            var deposit = Deposit.builder()
                    .id(dto.getId())
                    .walletId(dto.getWalletId())
                    .referenceId(dto.getReferenceId())
                    .description(dto.getDescription())
                    .method(DepositMethod.valueOf(dto.getMethod()))
                    .subTotal(dto.getSubTotal())
                    .fee(dto.getFee())
                    .total(dto.getTotal())
                    .brcode(dto.getBrcode())
                    .webhook(dto.getWebhook())
                    .status(DepositStatus.valueOf(dto.getStatus()))
                    .created(dto.getCreated())
                    .updated(dto.getUpdated())
                    .build();

            deposit.setLedgerId(ledger.getId());
            return depositRepository.save(deposit).then();
        });
    }
    public Mono<Void> updateDeposit(DepositUpdated dto) {
        return depositRepository.findById(dto.getId())
                .flatMap(existingDeposit -> {
                    existingDeposit.setReferenceId(dto.getReferenceId());
                    existingDeposit.setDescription(dto.getDescription());
                    existingDeposit.setMethod(DepositMethod.valueOf(dto.getMethod()));
                    existingDeposit.setSubTotal(dto.getSubTotal());
                    existingDeposit.setFee(dto.getFee());
                    existingDeposit.setTotal(dto.getTotal());
                    existingDeposit.setBrcode(dto.getBrcode());
                    existingDeposit.setStatus(DepositStatus.valueOf(dto.getStatus()));
                    existingDeposit.setUpdated(dto.getUpdated());
                    return depositRepository.save(existingDeposit);
                })
                .then();
    }
    public Mono<Void> completeDeposit(DepositCompleted dto) {
        return depositRepository.findById(dto.getId())
                .flatMap(existingDeposit ->
                        walletRepository.findById(dto.getWalletId()).flatMap(wallet -> {
                            existingDeposit.setStatus(DepositStatus.valueOf(dto.getStatus()));
                            existingDeposit.setUpdated(dto.getUpdated());
                            return depositRepository.save(existingDeposit).flatMap(saved -> {
                                if (saved.getStatus().equals(DepositStatus.SUCCESS)) {
                                    if (!(saved.getTotal().compareTo(BigDecimal.ZERO) < 0)) {
                                        wallet.setAvailable(wallet.getAvailable().add(saved.getTotal()));

                                    }
                                    wallet.setUpdated(LocalDateTime.now());
                                }

                                return walletRepository.save(wallet).flatMap((w -> ledgerRepository.findById(
                                        saved.getLedgerId()
                                ).flatMap(ledger -> {
                                    ledger.setUserId(w.getUserId());
                                    ledger.setStatus(
                                            saved.getStatus().equals(DepositStatus.SUCCESS) ? LedgerStatus.APPROVED : LedgerStatus.CANCELLED
                                    );
                                    ledger.setUpdated(LocalDateTime.now());

                                    publisher.publish("email.send", "TRANSACTIONAL_RECEIPT", EmailSend.builder()
                                            .from("noreply@lothuspay.com")
                                            .to(wallet.getEmail())
                                            .slug("TRANSACTIONAL_RECEIPT")
                                            .variables(new HashMap<>(Map.of(
                                                    "{date}", LocalDateTime.now().toString(),
                                                    "{value}", ledger.getAmount().toString(),
                                                    "{orderId}", saved.getId()
                                            )))
                                            .build());

                                    return ledgerRepository.save(ledger);
                                })));
                            });
                        })
                )
                .then();
    }
/*
    public Mono<Void> applyCustomWalletTax(String walletId, BigDecimal percent, BigDecimal fixed) {
        return walletRepository.findById(walletId)
                .flatMap(wallet -> {
                    if (wallet.getHistoryTaxPix() == null) {
                        wallet.setHistoryTaxPix(new ArrayList<>());
                    }

                    wallet.getHistoryTaxPix().add(wallet.getTax().getPix());

                    wallet.setTax(new WalletTax(true, new WalletTaxPix(percent, fixed, BigDecimal.ZERO, BigDecimal.ZERO)));
                    wallet.setUpdated(LocalDateTime.now());
                    return walletRepository.save(wallet).then();
                });
    }*/

    public Mono<Void> createWithdraw(WithdrawCreated dto) {
        var ledger = Ledger.builder()
                .id(UUID.randomUUID().toString())
                .userId("")
                .walletId(dto.getWalletId())
                .description(dto.getDescription())
                .type(LedgerType.DEBIT)
                .amount(dto.getTotal().negate())
                .fee(dto.getFee())
                .origin(OriginType.PAYMENT)
                .originId(dto.getId())
                .status(LedgerStatus.PENDING)
                .created(LocalDateTime.now())
                .updated(LocalDateTime.now())
                .build();

        return ledgerRepository.save(ledger).flatMap(i -> {
            var withdraw = Withdraw.builder()
                    .id(dto.getId())
                    .walletId(dto.getWalletId())
                    .referenceId(dto.getReferenceId())
                    .description(dto.getDescription())
                    .destination(new WithdrawDestination(
                            dto.getDestination().getName(),
                            WithdrawDocumentType.valueOf(dto.getDestination().getDocumentType()),
                            dto.getDestination().getDocument(),
                            WithdrawDestinationType.valueOf(dto.getDestination().getType()),
                            WithdrawDestinationSubType.valueOf(dto.getDestination().getSubType()),
                            dto.getDestination().getDestination()
                    ))
                    .subTotal(dto.getSubTotal())
                    .fee(dto.getFee())
                    .total(dto.getTotal())
                    .status(com.lothuspay.wallet.model.withdraw.status.WithdrawStatus.valueOf(dto.getStatus()))
                    .created(dto.getCreated())
                    .updated(dto.getUpdated())
                    .build();

            withdraw.setLedgerId(ledger.getId());

            return withdrawRepository.save(withdraw).flatMap((w) ->
                    walletRepository.findById(dto.getWalletId()).flatMap(wallet -> {
                        wallet.setFrozen(wallet.getFrozen().add(withdraw.getTotal().add(withdraw.getFee())));
                        wallet.setAvailable(wallet.getAvailable().subtract(withdraw.getTotal().add(withdraw.getFee())));
                        wallet.setUpdated(LocalDateTime.now());
                        return walletRepository.save(wallet).then();
                    }));
        });
    }

    public Mono<Void> completeWithdraw(WithdrawCompleted dto) {
        return withdrawRepository.findById(dto.getId())
                .flatMap(existingWithdraw ->
                        walletRepository.findById(dto.getWalletId()).flatMap(wallet -> {
                            existingWithdraw.setStatus(WithdrawStatus.valueOf(dto.getStatus()));
                            existingWithdraw.setUpdated(dto.getUpdated());
                            return withdrawRepository.save(existingWithdraw).flatMap(saved -> {
                                if (saved.getStatus().equals(WithdrawStatus.APPROVED)) {
                                    wallet.setFrozen(wallet.getFrozen().subtract(saved.getSubTotal().add(saved.getFee())));
                                    wallet.setUpdated(LocalDateTime.now());
                                }

                                return walletRepository.save(wallet).flatMap((w -> ledgerRepository.findById(
                                        saved.getLedgerId()
                                ).flatMap(ledger -> {
                                    ledger.setUserId(w.getUserId());
                                    ledger.setStatus(saved.getStatus().equals(
                                            WithdrawStatus.APPROVED) ? LedgerStatus.APPROVED : LedgerStatus.CANCELLED
                                    );
                                    ledger.setUpdated(LocalDateTime.now());

                                    publisher.publish("email.send", "TRANSACTIONAL_SEND", EmailSend.builder()
                                            .from("noreply@lothuspay.com")
                                            .to(wallet.getEmail())
                                            .slug("TRANSACTIONAL_SEND")
                                            .variables(new HashMap<>(Map.of(
                                                    "{date}", LocalDateTime.now().toString(),
                                                    "{value}", ledger.getAmount().toString(),
                                                    "{orderId}", saved.getId()
                                            )))
                                            .build());

                                    return ledgerRepository.save(ledger);
                                })));
                            });
                        })
                )
                .then();
    }
}
