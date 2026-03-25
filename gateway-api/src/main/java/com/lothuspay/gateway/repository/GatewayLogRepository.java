package com.lothuspay.gateway.repository;

import com.lothuspay.gateway.model.log.GatewayLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayLogRepository extends ReactiveMongoRepository<GatewayLog, String> {

}
