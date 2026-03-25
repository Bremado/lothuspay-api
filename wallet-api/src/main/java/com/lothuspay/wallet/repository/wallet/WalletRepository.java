package com.lothuspay.wallet.repository.wallet;

import com.lothuspay.wallet.model.wallet.Wallet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Repository
public interface WalletRepository extends ReactiveMongoRepository<Wallet, String> {

    Mono<Wallet> findByUserId(String userId);
    
    reactor.core.publisher.Flux<Wallet> findAllByUserId(String userId);
    
    reactor.core.publisher.Flux<Wallet> findByAvailableBetween(java.math.BigDecimal min, java.math.BigDecimal max);

    Flux<Wallet> findAllBy(PageRequest page);

    Flux<Wallet> findAllByUserIdIsIn(Collection<String> userIds);

}
