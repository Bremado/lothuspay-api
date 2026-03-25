package com.lothuspay.payments.repository.notification;

import com.lothuspay.payments.model.notification.Notification;
import com.lothuspay.payments.model.notification.status.NotificationStatus;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Repository
public interface NotificationRepository extends ReactiveMongoRepository<Notification, String> {

    Flux<Notification> findAllByStatus(NotificationStatus status);

    Flux<Notification> findALlByIdIn(Collection<String> ids);
}
