package com.lothuspay.payments.controller.callback;

import com.lothuspay.payments.dto.request.callback.SubadquirerCallbackDto;
import com.lothuspay.payments.dto.response.DtoResponse;
import com.lothuspay.payments.service.deposit.DepositService;
import com.lothuspay.payments.service.withdraw.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments/callback")
public class CallbackController {

    private final DepositService depositService;
    private final WithdrawService withdrawService;

    @PostMapping("/deposit/{walletId}/{depositId}")
    public Mono<DtoResponse> depositCallback(@PathVariable("walletId") String walletId, @PathVariable("depositId") String depositId, @RequestBody SubadquirerCallbackDto payload) {
        return depositService.callback(walletId, depositId, payload);
    }

    @PostMapping("/withdraw/{walletId}/{withdrawId}")
    public Mono<DtoResponse> withdrawCallback(@PathVariable("walletId") String walletId, @PathVariable("withdrawId") String withdrawId, @RequestBody SubadquirerCallbackDto payload) {
        return withdrawService.callback(walletId, withdrawId, payload);
    }

}
