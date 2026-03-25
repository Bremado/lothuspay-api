package com.lothuspay.payments.repository.config;

import com.lothuspay.payments.model.config.PaymentConfig;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends ReactiveMongoRepository<PaymentConfig, String> {

}
