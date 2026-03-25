package com.lothuspay.auth.event;

import com.lothuspay.auth.event.type.AuthTypeEvent;
import com.lothuspay.auth.model.accounts.Account;
import org.springframework.context.ApplicationEvent;

public class AuthEvent extends ApplicationEvent {

    private AuthTypeEvent type;
    private Account account;

    public AuthEvent(Object source, AuthTypeEvent type, Account account) {
        super(source);
        this.type = type;
        this.account = account;
    }
}
