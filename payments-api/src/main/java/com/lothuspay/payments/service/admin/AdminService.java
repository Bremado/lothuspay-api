package com.lothuspay.payments.service.admin;

import com.lothuspay.payments.dto.request.admin.PostUpdateConfig;
import com.lothuspay.payments.dto.response.DtoResponse;
import com.lothuspay.payments.dto.response.object.admin.GetConfig;
import com.lothuspay.payments.dto.response.object.admin.GetDepositStats;
import com.lothuspay.payments.dto.response.object.admin.GetWithdrawStats;
import com.lothuspay.payments.dto.response.status.DtoResponseStatus;
import com.lothuspay.payments.model.deposit.DepositRequest;
import com.lothuspay.payments.model.deposit.method.DepositRequestMethod;
import com.lothuspay.payments.model.deposit.status.DepositRequestStatus;
import com.lothuspay.payments.model.withdraw.WithdrawRequest;
import com.lothuspay.payments.model.withdraw.status.WithdrawRequestStatus;
import com.lothuspay.payments.pojo.UserContext;
import com.lothuspay.payments.repository.deposit.DepositRepository;
import com.lothuspay.payments.repository.withdraw.WithdrawRepository;
import com.lothuspay.payments.service.config.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ConfigService configService;
    private final DepositRepository depositRepository;
    private final WithdrawRepository withdrawRepository;

    public Mono<DtoResponse> getConfig() {
        return configService.config().flatMap(config -> Mono.just(
                DtoResponse.builder()
                        .status(DtoResponseStatus.SUCCESS)
                        .data(new GetConfig(config))
                        .build()
        ));
    }
    public Mono<DtoResponse> updateConfig(PostUpdateConfig dto) {
        return configService.updateConfig(dto.toPaymentConfig()).flatMap(config -> Mono.just(
                DtoResponse.builder()
                        .status(DtoResponseStatus.SUCCESS)
                        .data(new GetConfig(config))
                        .build()
        ));
    }

    public Mono<DtoResponse> getDeposits(int page, int size, DepositRequestStatus status, DepositRequestMethod method, String walletId, LocalDateTime startDate, LocalDateTime endDate) {
        Flux<DepositRequest> depositsFlux;
        
        if (status != null && startDate != null && endDate != null) {
            depositsFlux = depositRepository.findByStatusAndCreatedBetween(status, startDate, endDate);
        } else if (method != null && startDate != null && endDate != null) {
            depositsFlux = depositRepository.findByMethodAndCreatedBetween(method, startDate, endDate);
        } else if (walletId != null && startDate != null && endDate != null) {
            depositsFlux = depositRepository.findByWalletIdAndCreatedBetween(walletId, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            depositsFlux = depositRepository.findByCreatedBetween(startDate, endDate);
        } else if (status != null) {
            depositsFlux = depositRepository.findByStatus(status);
        } else {
            depositsFlux = depositRepository.findAllBy(PageRequest.of(page, size, Sort.by("created").descending()));
        }
        
        return depositsFlux
                .skip((long) page * size)
                .take(size)
                .collectList()
                .flatMap(deposits -> Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.SUCCESS)
                                .data(deposits)
                                .build()
                ));
    }
    
    public Mono<DtoResponse> getDepositById(String id) {
        return depositRepository.findById(id)
                .flatMap(deposit -> Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.SUCCESS)
                                .data(deposit)
                                .build()
                ))
                .switchIfEmpty(Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_DEPOSIT_001)
                                .message("Depósito não encontrado.")
                                .build()
                ));
    }
    
    public Mono<DtoResponse> getWithdrawals(int page, int size, WithdrawRequestStatus status, String walletId, LocalDateTime startDate, LocalDateTime endDate) {
        Flux<WithdrawRequest> withdrawsFlux;
        
        if (status != null && startDate != null && endDate != null) {
            withdrawsFlux = withdrawRepository.findByStatusAndCreatedBetween(status, startDate, endDate);
        } else if (walletId != null && startDate != null && endDate != null) {
            withdrawsFlux = withdrawRepository.findByWalletIdAndCreatedBetween(walletId, startDate, endDate);
        } else if (startDate != null && endDate != null) {
            withdrawsFlux = withdrawRepository.findByCreatedBetween(startDate, endDate);
        } else if (status != null) {
            withdrawsFlux = withdrawRepository.findByStatus(status);
        } else {
            withdrawsFlux = withdrawRepository.findAllBy(PageRequest.of(page, size, Sort.by("created").descending()));
        }
        
        return withdrawsFlux
                .skip((long) page * size)
                .take(size)
                .collectList()
                .flatMap(withdrawals -> Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.SUCCESS)
                                .data(withdrawals)
                                .build()
                ));
    }
    
    public Mono<DtoResponse> getWithdrawalById(String id) {
        return withdrawRepository.findById(id)
                .flatMap(withdraw -> Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.SUCCESS)
                                .data(withdraw)
                                .build()
                ))
                .switchIfEmpty(Mono.just(
                        DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_WITHDRAWAL_001)
                                .message("Saque não encontrado.")
                                .build()
                ));
    }

    public Mono<DtoResponse> getDepositsStatistics() {
        return depositRepository.findAll().collectList()
                .flatMap(list -> {
                    int totalDeposits = list.size();

                    int completedDeposits = Math.toIntExact(
                            list.stream()
                                    .filter(d -> d.getStatus() == DepositRequestStatus.SUCCESS)
                                    .count()
                    );

                    int failedDeposits = Math.toIntExact(
                            list.stream()
                                    .filter(d -> d.getStatus() == DepositRequestStatus.FAILED)
                                    .count()
                    );

                    int pendingDeposits = totalDeposits - (completedDeposits + failedDeposits);

                    BigDecimal totalAmountDeposited = list.stream()
                            .filter(d -> d.getStatus() == DepositRequestStatus.SUCCESS)
                            .map(DepositRequest::getSubTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalPendingAmount = list.stream()
                            .filter(d -> d.getStatus() == DepositRequestStatus.PENDING)
                            .map(DepositRequest::getSubTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalFailedAmount = list.stream()
                            .filter(d -> d.getStatus() == DepositRequestStatus.FAILED)
                            .map(DepositRequest::getSubTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalCompletedAmount = totalAmountDeposited;

                    BigDecimal totalFeesCollected = list.stream()
                            .filter(d -> d.getStatus() == DepositRequestStatus.SUCCESS)
                            .map(DepositRequest::getFee)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal averageDepositAmount =
                            totalDeposits > 0
                                    ? list.stream()
                                    .map(DepositRequest::getSubTotal)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .divide(
                                            BigDecimal.valueOf(totalDeposits),
                                            2,
                                            RoundingMode.HALF_UP
                                    )
                                    : BigDecimal.ZERO;

                    var stats = new GetDepositStats(
                            totalDeposits,
                            pendingDeposits,
                            completedDeposits,
                            failedDeposits,
                            totalAmountDeposited,
                            totalPendingAmount,
                            totalFailedAmount,
                            totalCompletedAmount,
                            totalFeesCollected,
                            averageDepositAmount
                    );

                    return Mono.just(
                            DtoResponse.builder()
                                    .status(DtoResponseStatus.SUCCESS)
                                    .data(stats)
                                    .build()
                    );
                });
    }

    public Mono<DtoResponse> getWithdrawalsStatistics() {
        return withdrawRepository.findAll().collectList()
                .flatMap(list -> {
                    int totalWithdrawals = list.size();

                    int completed = Math.toIntExact(
                            list.stream()
                                    .filter(w -> w.getStatus() == WithdrawRequestStatus.APPROVED)
                                    .count()
                    );

                    int failed = Math.toIntExact(
                            list.stream()
                                    .filter(w -> w.getStatus() == WithdrawRequestStatus.REJECTED)
                                    .count()
                    );

                    int pending = totalWithdrawals - (completed + failed);

                    BigDecimal totalAmountWithdrawn = list.stream()
                            .filter(w -> w.getStatus() == WithdrawRequestStatus.APPROVED)
                            .map(WithdrawRequest::getSubTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalAmountPending = list.stream()
                            .filter(w -> w.getStatus() == WithdrawRequestStatus.PENDING)
                            .map(WithdrawRequest::getSubTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal totalFeesCollected = list.stream()
                            .filter(w -> w.getStatus() == WithdrawRequestStatus.APPROVED)
                            .map(WithdrawRequest::getFee)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal averageWithdrawalAmount =
                            totalWithdrawals > 0
                                    ? list.stream()
                                    .map(WithdrawRequest::getSubTotal)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .divide(
                                            BigDecimal.valueOf(totalWithdrawals),
                                            2,
                                            RoundingMode.HALF_UP
                                    )
                                    : BigDecimal.ZERO;

                    var stats = new GetWithdrawStats(
                            totalWithdrawals,
                            completed,
                            failed,
                            pending,
                            totalAmountWithdrawn,
                            totalAmountPending,
                            totalFeesCollected,
                            averageWithdrawalAmount
                    );

                    return Mono.just(
                            DtoResponse.builder()
                                    .status(DtoResponseStatus.SUCCESS)
                                    .data(stats)
                                    .build()
                    );
                });
    }

}
