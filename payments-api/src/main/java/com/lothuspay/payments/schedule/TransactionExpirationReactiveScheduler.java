package com.lothuspay.payments.schedule;

import com.lothuspay.payments.service.deposit.DepositService;
import com.lothuspay.payments.service.withdraw.WithdrawService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Component
public class TransactionExpirationReactiveScheduler {

    @Autowired
    private DepositService depositService;

    @Autowired
    private WithdrawService withdrawService;

    @PostConstruct
    public void schedule() {
        Flux.interval(Duration.ofMinutes(1))
                .flatMap(tick -> execute())  // une ao pipeline
                .subscribe();
    }

    private Mono<Void> execute() {
        return Flux.merge(processExpiredDeposits(), processExpiredWithdrawals())
                .then();
    }

    private Mono<Void> processExpiredDeposits() {
        return depositService.expiredDeposits()
                .collectList()
                .flatMap(deposits -> {
                    return Flux.fromIterable(deposits)
                            .flatMap(deposit -> depositService.expireDeposit(deposit)
                                    .subscribeOn(Schedulers.boundedElastic()), 10) // limit concurrency to 10
                            .then();
                });
    }

    private Flux<Void> processExpiredWithdrawals() {
        return withdrawService.expiredWithdrawals()
                .collectList()
                .flatMapMany(withdrawals -> {
                    return Flux.fromIterable(withdrawals)
                            .flatMap(withdrawal -> withdrawService.expireWithdrawal(withdrawal)
                                    .subscribeOn(Schedulers.boundedElastic()), 10); // limit concurrency to 10
                });
    }

}
