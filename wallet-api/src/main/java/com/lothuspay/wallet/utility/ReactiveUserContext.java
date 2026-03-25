package com.lothuspay.wallet.utility;

import com.lothuspay.wallet.pojo.UserContext;
import com.lothuspay.wallet.security.UserContextWebFilter;
import reactor.core.publisher.Mono;

public class ReactiveUserContext {

    public static Mono<UserContext> current() {
        return Mono.deferContextual(ctxView ->
                Mono.justOrEmpty(ctxView.getOrEmpty(UserContextWebFilter.USER_CONTEXT_KEY)));
    }

}
