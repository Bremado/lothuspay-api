package com.lothuspay.wallet.repository.config;

import com.lothuspay.wallet.model.config.WalletConfig;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends ReactiveMongoRepository<WalletConfig, String> {
}
