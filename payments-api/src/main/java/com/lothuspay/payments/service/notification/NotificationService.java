package com.lothuspay.payments.service.notification;

import com.lothuspay.payments.model.notification.Notification;
import com.lothuspay.payments.model.notification.attempt.NotificationAttempt;
import com.lothuspay.payments.model.notification.status.NotificationStatus;
import com.lothuspay.payments.repository.notification.NotificationAttemptRepository;
import com.lothuspay.payments.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationAttemptRepository notificationAttemptRepository;

    public Mono<Void> sentNotification(Notification notification) {
        var body = notification.body();

        System.out.println("Sending notification to URL: " + notification.getUrl());

        WebClient webClient = WebClient.create();

        var attempt = new NotificationAttempt(
                UUID.randomUUID().toString(),
                notification.getId(),
                LocalDateTime.now(),
                "",
                "",
                ""
        );

        System.out.println(body);

        return webClient.post()
                .uri(notification.getUrl())
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(response -> {
                    attempt.setStatus("SENT");
                    attempt.setResponseBody(response);
                    attempt.setResponseCode("200");

                    var save1 = notificationRepository.save(notification);
                    var save2 = notificationAttemptRepository.save(attempt);

                    return Mono.when(save1, save2);
                })
                .onErrorResume(error -> {
                    attempt.setStatus("FAILED");
                    attempt.setResponseBody(error.getMessage());
                    attempt.setResponseCode("500");

                    return notificationAttemptRepository.save(attempt).then();
                }).then();
    }

    public Flux<Notification> pendingNotifications() {
        return notificationAttemptRepository
                .findAllByTimestampBefore(LocalDateTime.now().minusMinutes(5))
                .filter(attempt -> !attempt.getStatus().equals("SENT"))
                .map(NotificationAttempt::getNotificationId)
                .distinct()
                .collectList()
                .flatMapMany(notificationRepository::findALlByIdIn);
    }

}

