package com.lothuspay.payments.repository.withdraw;

import com.lothuspay.payments.model.withdraw.WithdrawRequest;
import com.lothuspay.payments.model.withdraw.status.WithdrawRequestStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface WithdrawRepository extends ReactiveMongoRepository<WithdrawRequest, String> {

    Flux<WithdrawRequest> findAllBy(PageRequest pageRequest);
    Flux<WithdrawRequest> findByStatusAndCreatedBefore(WithdrawRequestStatus status, LocalDateTime before);
    Flux<WithdrawRequest> findByStatus(WithdrawRequestStatus status);
    Flux<WithdrawRequest> findByStatusAndCreatedBetween(WithdrawRequestStatus status, LocalDateTime start, LocalDateTime end);
    Flux<WithdrawRequest> findByWalletIdAndCreatedBetween(String walletId, LocalDateTime start, LocalDateTime end);
    Flux<WithdrawRequest> findByCreatedBetween(LocalDateTime start, LocalDateTime end);
}
