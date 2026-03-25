package com.lothuspay.gateway.repository;

import com.lothuspay.gateway.model.config.GatewayConfig;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayConfigRepository extends ReactiveMongoRepository<GatewayConfig, String> {

}
