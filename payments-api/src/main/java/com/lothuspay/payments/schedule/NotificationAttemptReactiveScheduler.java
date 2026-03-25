package com.lothuspay.payments.schedule;

import com.lothuspay.payments.service.deposit.DepositService;
import com.lothuspay.payments.service.notification.NotificationService;
import com.lothuspay.payments.service.withdraw.WithdrawService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Component
public class NotificationAttemptReactiveScheduler {

    @Autowired
    private NotificationService notificationService;

    @PostConstruct
    public void schedule() {
        Flux.interval(Duration.ofMinutes(1))
                .flatMap(tick -> execute())  // une ao pipeline
                .subscribe();
    }

    private Mono<Void> execute() {
        return processNotifications()
                .then();
    }

    private Flux<Void> processNotifications() {
        return notificationService.pendingNotifications()
                .collectList()
                .flatMapMany(notifications -> {
                    return Flux.fromIterable(notifications)
                            .flatMap(notify -> notificationService.sentNotification(notify)
                                    .subscribeOn(Schedulers.boundedElastic()), 10);
                });
    }

}
