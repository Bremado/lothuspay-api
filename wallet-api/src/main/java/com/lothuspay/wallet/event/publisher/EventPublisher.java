package com.lothuspay.wallet.event.publisher;

import com.lothuspay.events.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String type, Object payload) {
        EventMessage event = EventMessage.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .timestamp(System.currentTimeMillis())
                .payload(payload)
                .build();

        kafkaTemplate.send(topic, event.getId(), event);
    }
}
