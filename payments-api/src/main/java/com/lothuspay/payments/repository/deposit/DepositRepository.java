package com.lothuspay.payments.repository.deposit;

import com.lothuspay.payments.model.deposit.DepositRequest;
import com.lothuspay.payments.model.deposit.method.DepositRequestMethod;
import com.lothuspay.payments.model.deposit.status.DepositRequestStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface DepositRepository extends ReactiveMongoRepository<DepositRequest, String> {

    Flux<DepositRequest> findAllBy(PageRequest pageRequest);
    Flux<DepositRequest> findByStatusAndCreatedBefore(DepositRequestStatus status, LocalDateTime createdBefore);
    Flux<DepositRequest> findByStatus(DepositRequestStatus status);
    Flux<DepositRequest> findByStatusAndCreatedBetween(DepositRequestStatus status, LocalDateTime start, LocalDateTime end);
    Flux<DepositRequest> findByMethodAndCreatedBetween(DepositRequestMethod method, LocalDateTime start, LocalDateTime end);
    Flux<DepositRequest> findByWalletIdAndCreatedBetween(String walletId, LocalDateTime start, LocalDateTime end);
    Flux<DepositRequest> findByCreatedBetween(LocalDateTime start, LocalDateTime end);
}
