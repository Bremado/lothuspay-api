package com.lothuspay.wallet.controller.deposits;

import com.lothuspay.wallet.dto.response.DtoResponse;
import com.lothuspay.wallet.dto.response.status.DtoResponseStatus;
import com.lothuspay.wallet.pojo.holder.UserContextHolder;
import com.lothuspay.wallet.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/wallet/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final WalletService walletService;
    private final UserContextHolder contextHolder;

    @GetMapping
    public Mono<DtoResponse> deposits(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                      @RequestParam(value = "limit", defaultValue = "20", required = false) int limit) {
        if (page < 1) page = 1;
        if (limit < 1 || limit > 20) limit = 20;

        int finalPage = page;
        int finalLimit = limit;
        return contextHolder.current().flatMap(context -> walletService.deposits(context, (finalPage-1), finalLimit))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }

    @GetMapping("/{id}")
    public Mono<DtoResponse> depositDetails(@PathVariable("id") String id) {
        return contextHolder.current().flatMap(user -> walletService.depositDetails(user, id))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }
}
