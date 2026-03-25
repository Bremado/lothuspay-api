package com.lothuspay.wallet.controller.internal;

import com.lothuspay.wallet.dto.response.DtoResponse;
import com.lothuspay.wallet.dto.response.object.GetWallet;
import com.lothuspay.wallet.service.internal.InternalService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/wallet/internal")
public class InternalController {

    private final InternalService internalService;

    @GetMapping("/wallet/{userId}")
    public Mono<GetWallet> walletByUserId(@PathVariable("userId") String userId) {
        return internalService.wallet(userId);
    }


}
