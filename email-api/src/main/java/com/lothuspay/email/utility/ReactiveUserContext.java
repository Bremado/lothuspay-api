package com.lothuspay.email.utility;

import com.lothuspay.email.pojo.UserContext;
import com.lothuspay.email.security.UserContextWebFilter;
import reactor.core.publisher.Mono;

public class ReactiveUserContext {

    public static Mono<UserContext> current() {
        return Mono.deferContextual(ctxView ->
                Mono.justOrEmpty(ctxView.getOrEmpty(UserContextWebFilter.USER_CONTEXT_KEY)));
    }

}
