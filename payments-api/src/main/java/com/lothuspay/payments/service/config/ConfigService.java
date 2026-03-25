package com.lothuspay.payments.service.config;

import com.lothuspay.payments.model.config.PaymentConfig;
import com.lothuspay.payments.repository.config.ConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;

    public Mono<PaymentConfig> config() {
        return configRepository.findById("payment_config").switchIfEmpty(Mono.defer(() -> {
            var defaultConfig = new PaymentConfig();
            return configRepository.save(defaultConfig).flatMap(Mono::just);
        })).flatMap(Mono::just);
    }

    public Mono<PaymentConfig> updateConfig(PaymentConfig updatedConfig) {
        return configRepository.save(updatedConfig).flatMap(Mono::just);
    }
}
