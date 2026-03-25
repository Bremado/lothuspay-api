package com.lothuspay.payments.controller.withdraw;

import com.lothuspay.payments.dto.request.PostCreateWithdraw;
import com.lothuspay.payments.dto.response.DtoResponse;
import com.lothuspay.payments.dto.response.status.DtoResponseStatus;
import com.lothuspay.payments.pojo.holder.UserContextHolder;
import com.lothuspay.payments.service.withdraw.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments/withdraw")
public class WithdrawController {

    private final WithdrawService withdrawService;
    private final UserContextHolder contextHolder;

    @PostMapping
    public Mono<DtoResponse> create(@RequestBody PostCreateWithdraw dto) {
        return contextHolder.current().flatMap(context -> withdrawService.create(context, dto))
                .switchIfEmpty(Mono.just(DtoResponse.builder()
                        .status(DtoResponseStatus.ERR_AUTH_001)
                        .message(DtoResponseStatus.ERR_AUTH_001.getMessage())
                        .build()));
    }
}
