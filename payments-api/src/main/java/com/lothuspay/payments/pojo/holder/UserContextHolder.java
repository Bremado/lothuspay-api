package com.lothuspay.payments.pojo.holder;

import com.lothuspay.payments.pojo.UserContext;
import com.lothuspay.payments.utility.ReactiveUserContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserContextHolder {

    public Mono<UserContext> current() {
        return ReactiveUserContext.current();
    }

}
