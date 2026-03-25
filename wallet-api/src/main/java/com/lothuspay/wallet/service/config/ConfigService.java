package com.lothuspay.wallet.service.config;

import com.lothuspay.wallet.model.config.WalletConfig;
import com.lothuspay.wallet.repository.config.ConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final ConfigRepository configRepository;

    public Mono<WalletConfig> config() {
        return configRepository.findById("wallet_config").switchIfEmpty(Mono.defer(() -> {
            var defaultConfig = new WalletConfig();
            return configRepository.save(defaultConfig).flatMap(Mono::just);
        })).flatMap(Mono::just);
    }
}
