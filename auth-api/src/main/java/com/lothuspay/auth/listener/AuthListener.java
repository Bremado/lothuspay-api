package com.lothuspay.auth.listener;

import com.lothuspay.auth.event.AuthEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuthListener {

    @EventListener
    public void handleAuthEvent(AuthEvent event) {

    }
}
