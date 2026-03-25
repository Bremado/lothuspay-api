package com.lothuspay.payments.repository.notification;

import com.lothuspay.payments.model.notification.attempt.NotificationAttempt;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface NotificationAttemptRepository extends ReactiveMongoRepository<NotificationAttempt, String> {

    Flux<NotificationAttempt> findAllByTimestampBefore(LocalDateTime timestampBefore);

}
