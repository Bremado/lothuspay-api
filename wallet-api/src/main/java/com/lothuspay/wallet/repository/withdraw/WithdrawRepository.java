package com.lothuspay.wallet.repository.withdraw;

import com.lothuspay.wallet.model.withdraw.Withdraw;
import com.lothuspay.wallet.model.withdraw.status.WithdrawStatus;
import lombok.With;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface WithdrawRepository extends ReactiveMongoRepository<Withdraw, String> {

    Flux<Withdraw> findAllByWalletId(String walletId, PageRequest request);

    Flux<Withdraw> findAllByIdIn(Collection<String> ids);

    Flux<Withdraw> findAllByStatusAndCreatedBetween(WithdrawStatus status, LocalDateTime start, LocalDateTime end);

    Flux<Withdraw> findAllByCreatedBetween(LocalDateTime start, LocalDateTime end);

    Flux<Withdraw> findAllByStatus(WithdrawStatus status);

}
