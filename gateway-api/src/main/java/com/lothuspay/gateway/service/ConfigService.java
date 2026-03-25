package com.lothuspay.gateway.service;

import com.lothuspay.gateway.model.config.GatewayConfig;
import com.lothuspay.gateway.repository.GatewayConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConfigService {

    @Autowired
    private GatewayConfigRepository repository;

    public Mono<GatewayConfig> getConfig() {
        return repository.findAll().next();
    }

    public Mono<GatewayConfig> updateConfig(GatewayConfig config) {
        config.setId("CONFIG");
        return repository.save(config);
    }
}
