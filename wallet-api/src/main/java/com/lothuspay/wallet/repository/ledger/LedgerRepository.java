package com.lothuspay.wallet.repository.ledger;

import com.lothuspay.wallet.model.ledger.Ledger;
import com.lothuspay.wallet.model.ledger.status.LedgerStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@Repository
public interface LedgerRepository extends ReactiveMongoRepository<Ledger, String> {

    Flux<Ledger> findAllByWalletId(String walletId, PageRequest request);

    Flux<Ledger> findAllByWalletIdAndStatus(String walletId, LedgerStatus status);

    Flux<Ledger> findAllByStatusAndCreatedBetween(LedgerStatus status, LocalDateTime start, LocalDateTime end);

    Flux<Ledger> findAllByCreatedBetween(LocalDateTime start, LocalDateTime end);

    Flux<Ledger> findAllByStatus(LedgerStatus status);
}
