package com.lothuspay.wallet.pojo.holder;

import com.lothuspay.wallet.pojo.UserContext;
import com.lothuspay.wallet.utility.ReactiveUserContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserContextHolder {

    public Mono<UserContext> current() {
        return ReactiveUserContext.current();
    }

}
