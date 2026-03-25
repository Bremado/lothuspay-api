package com.lothuspay.payments.integration.wallet;

import com.lothuspay.payments.integration.wallet.dto.GetWallet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class WalletIntegration {

    private final WebClient walletWebClient;

    public WalletIntegration(@Qualifier("walletWebClient") WebClient walletWebClient) {
        this.walletWebClient = walletWebClient;
    }

    @Value("${internal.secret}")
    private String internalToken;

    public Mono<GetWallet> getWallet(String userId) {
        return walletWebClient.get()
                .uri("/int/wallet/internal/wallet/" + userId)
                .header("X-Internal", internalToken)
                .retrieve()
                .bodyToMono(GetWallet.class);
    }
}
