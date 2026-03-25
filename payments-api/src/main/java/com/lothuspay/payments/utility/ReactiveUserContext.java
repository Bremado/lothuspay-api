package com.lothuspay.payments.utility;

import com.lothuspay.payments.pojo.UserContext;
import com.lothuspay.payments.security.UserContextWebFilter;
import reactor.core.publisher.Mono;

public class ReactiveUserContext {

    public static Mono<UserContext> current() {
        return Mono.deferContextual(ctxView ->
                Mono.justOrEmpty(ctxView.getOrEmpty(UserContextWebFilter.USER_CONTEXT_KEY)));
    }

}
