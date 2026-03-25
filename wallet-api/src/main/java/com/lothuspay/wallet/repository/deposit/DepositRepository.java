package com.lothuspay.wallet.repository.deposit;

import com.lothuspay.wallet.model.deposit.Deposit;
import com.lothuspay.wallet.model.deposit.status.DepositStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface DepositRepository extends ReactiveMongoRepository<Deposit, String> {

    Flux<Deposit> findAllByWalletId(String walletId, PageRequest pageRequest);

    Flux<Deposit> findAllByIdIn(Collection<String> ids);

    Flux<Deposit> findAllByStatusAndCreatedBetween(DepositStatus status, LocalDateTime start, LocalDateTime end);

    Flux<Deposit> findAllByCreatedBetween(LocalDateTime start, LocalDateTime end);

    Flux<Deposit> findAllByStatus(DepositStatus status);

}
