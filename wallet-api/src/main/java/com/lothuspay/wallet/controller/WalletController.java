package com.lothuspay.wallet.controller;

import com.lothuspay.wallet.dto.response.DtoResponse;
import com.lothuspay.wallet.dto.response.status.DtoResponseStatus;
import com.lothuspay.wallet.pojo.holder.UserContextHolder;
import com.lothuspay.wallet.service.ledger.LedgerService;
import com.lothuspay.wallet.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final LedgerService ledgerService;
    private final UserContextHolder contextHolder;

    @GetMapping
    public Mono<DtoResponse> wallet() {
        return contextHolder.current().flatMap(walletService::wallet)
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                                .status(DtoResponseStatus.ERR_AUTH_001)
                                .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @GetMapping("/statement")
    public Mono<DtoResponse> statement(@RequestParam(name = "page", defaultValue = "1") int page, @RequestParam(name = "limit", defaultValue = "20", required = false) int limit) {
        if (page < 1) page = 1;
        if (limit < 1 || limit > 100) limit = 20;

        int finalPage = page;
        int finalLimit = limit;
        return contextHolder.current().flatMap(context -> ledgerService.statement(context, (finalPage-1), finalLimit))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }
}
