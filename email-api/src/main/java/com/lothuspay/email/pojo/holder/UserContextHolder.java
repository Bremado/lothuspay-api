package com.lothuspay.email.pojo.holder;

import com.lothuspay.email.pojo.UserContext;
import com.lothuspay.email.utility.ReactiveUserContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UserContextHolder {

    public Mono<UserContext> current() {
        return ReactiveUserContext.current();
    }

}
