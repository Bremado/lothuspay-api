package com.lothuspay.wallet.service.admin;

import com.lothuspay.wallet.dto.request.admin.GetWallets;
import com.lothuspay.wallet.dto.request.admin.PostUpdateGlobalTaxes;
import com.lothuspay.wallet.dto.request.admin.PostUpdateUserTaxes;
import com.lothuspay.wallet.dto.response.DtoResponse;
import com.lothuspay.wallet.dto.response.object.GetProfitReport;
import com.lothuspay.wallet.dto.response.object.GetSummaryReport;
import com.lothuspay.wallet.dto.response.object.GetWallet;
import com.lothuspay.wallet.dto.response.status.DtoResponseStatus;
import com.lothuspay.wallet.model.config.WalletConfig;
import com.lothuspay.wallet.model.deposit.Deposit;
import com.lothuspay.wallet.model.deposit.status.DepositStatus;
import com.lothuspay.wallet.model.ledger.Ledger;
import com.lothuspay.wallet.model.ledger.status.LedgerStatus;
import com.lothuspay.wallet.model.wallet.Wallet;
import com.lothuspay.wallet.model.wallet.tax.WalletTax;
import com.lothuspay.wallet.model.wallet.tax.pix.WalletTaxPix;
import com.lothuspay.wallet.model.withdraw.Withdraw;
import com.lothuspay.wallet.model.withdraw.status.WithdrawStatus;
import com.lothuspay.wallet.repository.config.ConfigRepository;
import com.lothuspay.wallet.repository.deposit.DepositRepository;
import com.lothuspay.wallet.repository.ledger.LedgerRepository;
import com.lothuspay.wallet.repository.wallet.WalletRepository;
import com.lothuspay.wallet.repository.withdraw.WithdrawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final DepositRepository depositRepository;
    private final WithdrawRepository withdrawRepository;
    private final LedgerRepository ledgerRepository;
    private final WalletRepository walletRepository;
    private final ConfigRepository configRepository;

    public Mono<DtoResponse> getProfitReport(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        LocalDateTime finalStartDate = startDate;
        LocalDateTime finalEndDate = endDate;
        Mono<GetProfitReport> reportMono = Mono.zip(
                depositRepository.findAllByStatusAndCreatedBetween(DepositStatus.SUCCESS, startDate, endDate).collectList(),
                withdrawRepository.findAllByStatusAndCreatedBetween(WithdrawStatus.APPROVED, startDate, endDate).collectList(),
                ledgerRepository.findAllByStatusAndCreatedBetween(LedgerStatus.APPROVED, startDate, endDate).collectList()
        ).map(tuple -> {
            var deposits = tuple.getT1();
            var withdrawals = tuple.getT2();
            var ledgers = tuple.getT3();

            // Calcular taxas de depósitos
            BigDecimal depositFees = deposits.stream()
                    .map(d -> d.getFee() != null ? d.getFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calcular taxas de saques
            BigDecimal withdrawalFees = withdrawals.stream()
                    .map(w -> w.getFee() != null ? w.getFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Total de taxas (receitas)
            BigDecimal totalFees = depositFees.add(withdrawalFees);

            // Total de depósitos
            BigDecimal totalDeposits = deposits.stream()
                    .map(d -> d.getTotal() != null ? d.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Total de saques
            BigDecimal totalWithdrawals = withdrawals.stream()
                    .map(w -> w.getTotal() != null ? w.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Lucro = receitas de taxas (não há custos explícitos no modelo atual)
            BigDecimal totalProfit = totalFees;

            // Total de transações
            long totalTransactions = deposits.size() + withdrawals.size();

            // Ticket médio
            BigDecimal averageTicket = totalTransactions > 0
                    ? totalDeposits.add(totalWithdrawals).divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return GetProfitReport.builder()
                    .totalProfit(totalProfit)
                    .totalRevenue(totalFees)
                    .totalDeposits(totalDeposits)
                    .totalWithdrawals(totalWithdrawals)
                    .totalFees(totalFees)
                    .depositFees(depositFees)
                    .withdrawalFees(withdrawalFees)
                    .totalTransactions(totalTransactions)
                    .totalDepositCount((long) deposits.size())
                    .totalWithdrawalCount((long) withdrawals.size())
                    .averageTicket(averageTicket)
                    .periodStart(finalStartDate)
                    .periodEnd(finalEndDate)
                    .build();
        });

        return reportMono.map(report ->
                DtoResponse.builder()
                        .status(DtoResponseStatus.SUCCESS)
                        .data(report)
                        .message("Relatório de lucro gerado com sucesso.")
                        .build()
        );
    }

    public Mono<DtoResponse> getSummaryReport() {
        Mono<GetSummaryReport> reportMono = Mono.zip(
                depositRepository.findAllByStatus(DepositStatus.SUCCESS).collectList(),
                withdrawRepository.findAllByStatus(WithdrawStatus.APPROVED).collectList(),
                ledgerRepository.findAllByStatus(LedgerStatus.APPROVED).collectList(),
                walletRepository.count()
        ).map(tuple -> {
            var deposits = tuple.getT1();
            var withdrawals = tuple.getT2();
            var ledgers = tuple.getT3();
            var totalUsers = tuple.getT4();

            // Calcular receitas totais
            BigDecimal depositFees = deposits.stream()
                    .map(d -> d.getFee() != null ? d.getFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal withdrawalFees = withdrawals.stream()
                    .map(w -> w.getFee() != null ? w.getFee() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalRevenue = depositFees.add(withdrawalFees);
            BigDecimal totalProfit = totalRevenue; // Lucro = receitas (sem custos explícitos)

            // Volume total
            BigDecimal totalDeposits = deposits.stream()
                    .map(d -> d.getTotal() != null ? d.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalWithdrawals = withdrawals.stream()
                    .map(w -> w.getTotal() != null ? w.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalVolume = totalDeposits.add(totalWithdrawals);

            // Total de transações
            long totalTransactions = deposits.size() + withdrawals.size();

            // Ticket médio
            BigDecimal averageTicket = totalTransactions > 0
                    ? totalVolume.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            // Lucro por método (agrupando por método de depósito)
            Map<String, BigDecimal> profitByMethod = new HashMap<>();
            deposits.forEach(deposit -> {
                String method = deposit.getMethod() != null ? deposit.getMethod().name() : "UNKNOWN";
                BigDecimal fee = deposit.getFee() != null ? deposit.getFee() : BigDecimal.ZERO;
                profitByMethod.merge(method, fee, BigDecimal::add);
            });

            // Transações por método
            Map<String, Long> transactionsByMethod = deposits.stream()
                    .collect(Collectors.groupingBy(
                            d -> d.getMethod() != null ? d.getMethod().name() : "UNKNOWN",
                            Collectors.counting()
                    ));

            return GetSummaryReport.builder()
                    .totalProfit(totalProfit)
                    .totalRevenue(totalRevenue)
                    .totalVolume(totalVolume)
                    .totalUsers(totalUsers)
                    .totalTransactions(totalTransactions)
                    .averageTicket(averageTicket)
                    .profitByMethod(profitByMethod)
                    .transactionsByMethod(transactionsByMethod)
                    .build();
        });

        return reportMono.map(report ->
                DtoResponse.builder()
                        .status(DtoResponseStatus.SUCCESS)
                        .data(report)
                        .message("Relatório resumo gerado com sucesso.")
                        .build()
        );
    }

    public Mono<DtoResponse> updateGlobalTaxes(PostUpdateGlobalTaxes dto) {
        return configRepository.findById("wallet_config")
                .switchIfEmpty(Mono.defer(() -> {
                    WalletConfig newConfig = new WalletConfig();
                    newConfig.setWalletTaxPix(new WalletTaxPix(dto.getPercent(), dto.getFixed(), dto.getBlackPercent(), dto.getBlackFixed(), dto.getWithdrawPercent(), dto.getWithdrawFixed(), dto.getBlackWithdrawPercent(), dto.getBlackWithdrawFixed()));
                    return configRepository.save(newConfig);
                }))
                .flatMap(config -> {
                    config.setWalletTaxPix(new WalletTaxPix(
                            dto.getPercent(), dto.getFixed(),
                            dto.getBlackPercent(), dto.getBlackFixed(),
                            dto.getWithdrawPercent(), dto.getWithdrawFixed(),
                            dto.getBlackWithdrawPercent(), dto.getBlackWithdrawFixed()
                    ));
                    return configRepository.save(config);
                })
                .map(savedConfig ->
                        DtoResponse.builder()
                                .status(DtoResponseStatus.SUCCESS)
                                .data(savedConfig)
                                .message("Taxas globais atualizadas com sucesso.")
                                .build()
                );
    }

    public Mono<DtoResponse> updateUserTaxes(String userId, PostUpdateUserTaxes dto) {
        return walletRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Carteira não encontrada para o usuário: " + userId)))
                .flatMap(wallet -> {
                    wallet.setTax(new WalletTax(
                            true, // custom = true quando é personalizado
                            new WalletTaxPix(dto.getPercent(), dto.getFixed(), dto.getBlackPercent(), dto.getBlackFixed(), dto.getWithdrawPercent(), dto.getWithdrawFixed(), dto.getBlackWithdrawPercent(), dto.getBlackWithdrawFixed())
                    ));
                    return walletRepository.save(wallet);
                })
                .map(savedWallet ->
                        DtoResponse.builder()
                                .status(DtoResponseStatus.SUCCESS)
                                .data(savedWallet)
                                .message("Taxas do usuário atualizadas com sucesso.")
                                .build()
                )
                .onErrorResume(error ->
                        Mono.just(DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_WALLET_001)
                                .message("Erro ao atualizar taxas: " + error.getMessage())
                                .build())
                );
    }

    public Mono<DtoResponse> getGlobalConfig() {
        return configRepository.findById("wallet_config")
                .switchIfEmpty(Mono.defer(() -> {
                    WalletConfig defaultConfig = new WalletConfig();
                    return configRepository.save(defaultConfig);
                }))
                .map(config ->
                        DtoResponse.builder()
                                .status(DtoResponseStatus.SUCCESS)
                                .data(config)
                                .message("Configuração global recuperada com sucesso.")
                                .build()
                );
    }

    public Mono<DtoResponse> getWallets(int page, int limit, String userId, BigDecimal minBalance, BigDecimal maxBalance) {
        var pageRequest = org.springframework.data.domain.PageRequest.of(page, limit);
        
        return walletRepository.findAllBy(pageRequest)
                .filter(wallet -> {
                    if (userId != null && !userId.isEmpty() && !wallet.getUserId().equals(userId)) {
                        return false;
                    }
                    if (minBalance != null && wallet.getAvailable().compareTo(minBalance) < 0) {
                        return false;
                    }
                    if (maxBalance != null && wallet.getAvailable().compareTo(maxBalance) > 0) {
                        return false;
                    }
                    return true;
                })
                .collectList()
                .flatMap(wallets -> {
                    var response = new HashMap<String, Object>();
                    response.put("wallets", wallets);
                    response.put("total", wallets.size());
                    response.put("page", page);
                    response.put("limit", limit);
                    return Mono.just(DtoResponse.builder()
                            .status(DtoResponseStatus.SUCCESS)
                            .data(response)
                            .message("Carteiras recuperadas com sucesso.")
                            .build());
                });
    }

    public Mono<DtoResponse> getWalletById(String walletId) {
        return walletRepository.findById(walletId)
                .flatMap(wallet -> Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.SUCCESS)
                        .data(wallet)
                        .message("Carteira recuperada com sucesso.")
                        .build()))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_WALLET_001)
                        .message("Carteira não encontrada.")
                        .build()));
    }

    public Mono<DtoResponse> getWalletLedger(String walletId, int page, int limit, LocalDateTime startDate, LocalDateTime endDate, com.lothuspay.wallet.model.ledger.type.LedgerType type) {
        return walletRepository.findById(walletId)
                .flatMap(wallet -> {
                    var pageRequest = org.springframework.data.domain.PageRequest.of(page, limit, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "created"));
                    
                    Flux<Ledger> ledgerFlux;
                    if (startDate != null && endDate != null) {
                        ledgerFlux = ledgerRepository.findAllByWalletId(walletId, pageRequest)
                                .filter(ledger -> {
                                    if (type != null && !ledger.getType().equals(type)) {
                                        return false;
                                    }
                                    return ledger.getCreated().isAfter(startDate) && ledger.getCreated().isBefore(endDate);
                                });
                    } else {
                        ledgerFlux = ledgerRepository.findAllByWalletId(walletId, pageRequest);
                        if (type != null) {
                            ledgerFlux = ledgerFlux.filter(ledger -> ledger.getType().equals(type));
                        }
                    }
                    
                    return ledgerFlux.collectList()
                            .flatMap(ledgers -> {
                                var response = new HashMap<String, Object>();
                                response.put("ledgers", ledgers);
                                response.put("total", ledgers.size());
                                response.put("page", page);
                                response.put("limit", limit);
                                return Mono.just(DtoResponse.builder()
                                        .status(DtoResponseStatus.SUCCESS)
                                        .data(response)
                                        .message("Histórico de movimentações recuperado com sucesso.")
                                        .build());
                            });
                })
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_WALLET_001)
                        .message("Carteira não encontrada.")
                        .build()));
    }

    public Mono<DtoResponse> adjustBalance(String walletId, com.lothuspay.wallet.dto.request.admin.PostAdjustBalance dto) {
        return walletRepository.findById(walletId)
                .flatMap(wallet -> {
                    var ledger = Ledger.builder()
                            .id(java.util.UUID.randomUUID().toString())
                            .userId(wallet.getUserId())
                            .walletId(walletId)
                            .description(dto.getDescription())
                            .type(dto.getType())
                            .amount(dto.getAmount())
                            .fee(BigDecimal.ZERO)
                            .origin(com.lothuspay.wallet.model.ledger.origin.OriginType.ADMIN)
                            .originId("admin_adjust")
                            .status(LedgerStatus.APPROVED)
                            .created(LocalDateTime.now())
                            .updated(LocalDateTime.now())
                            .build();
                    
                    if (dto.getType() == com.lothuspay.wallet.model.ledger.type.LedgerType.CREDIT) {
                        wallet.setAvailable(wallet.getAvailable().add(dto.getAmount()));
                    } else if (dto.getType() == com.lothuspay.wallet.model.ledger.type.LedgerType.DEBIT) {
                        wallet.setAvailable(wallet.getAvailable().subtract(dto.getAmount()));
                    }
                    wallet.setUpdated(LocalDateTime.now());
                    
                    return ledgerRepository.save(ledger)
                            .then(walletRepository.save(wallet))
                            .flatMap(saved -> Mono.just(DtoResponse.builder()
                                    .status(DtoResponseStatus.SUCCESS)
                                    .data(saved)
                                    .message("Saldo ajustado com sucesso.")
                                    .build()));
                })
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_WALLET_001)
                        .message("Carteira não encontrada.")
                        .build()));
    }

    public Mono<DtoResponse> freezeBalance(String walletId, com.lothuspay.wallet.dto.request.admin.PostFreezeBalance dto) {
        return walletRepository.findById(walletId)
                .flatMap(wallet -> {
                    if (dto.getFrozen()) {
                        if (dto.getAmount() != null) {
                            wallet.setFrozen(wallet.getFrozen().add(dto.getAmount()));
                            wallet.setAvailable(wallet.getAvailable().subtract(dto.getAmount()));
                        }
                    } else {
                        if (dto.getAmount() != null) {
                            wallet.setFrozen(wallet.getFrozen().subtract(dto.getAmount()));
                            wallet.setAvailable(wallet.getAvailable().add(dto.getAmount()));
                        } else {
                            wallet.setAvailable(wallet.getAvailable().add(wallet.getFrozen()));
                            wallet.setFrozen(BigDecimal.ZERO);
                        }
                    }
                    wallet.setUpdated(LocalDateTime.now());
                    
                    return walletRepository.save(wallet)
                            .flatMap(saved -> Mono.just(DtoResponse.builder()
                                    .status(DtoResponseStatus.SUCCESS)
                                    .data(saved)
                                    .message(dto.getFrozen() ? "Saldo congelado com sucesso." : "Saldo descongelado com sucesso.")
                                    .build()));
                })
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_WALLET_001)
                        .message("Carteira não encontrada.")
                        .build()));
    }

    public Mono<DtoResponse> getWallets(GetWallets ids) {
        return walletRepository.findAllByUserIdIsIn(ids.getIds())
                .collectList()
                .flatMap(wallets -> Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.SUCCESS)
                        .data(wallets)
                        .message("Carteiras recuperadas com sucesso.")
                        .build()));
    }

}

