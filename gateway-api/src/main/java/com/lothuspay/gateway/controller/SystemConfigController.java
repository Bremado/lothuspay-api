package com.lothuspay.gateway.controller;

import com.lothuspay.gateway.model.config.GatewayConfig;
import com.lothuspay.gateway.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final ConfigService service;

    @GetMapping
    public Mono<GatewayConfig> getConfig() {
        return service.getConfig();
    }

    @PutMapping
    public Mono<GatewayConfig> updateConfig(@RequestBody GatewayConfig config) {
        return service.updateConfig(config);
    }
}