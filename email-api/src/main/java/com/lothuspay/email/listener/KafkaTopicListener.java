package com.lothuspay.email.listener;

import java.util.Map;
import java.util.HashMap;

import com.lothuspay.email.model.layout.slug.LayoutSlug;
import com.lothuspay.email.service.layout.LayoutService;
import com.lothuspay.email.service.resend.ResendService;
import com.lothuspay.events.EventMessage;
import com.lothuspay.events.dto.email.EmailSend;
import com.resend.core.exception.ResendException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaTopicListener {

    private final LayoutService layoutService;
    private final ResendService resendService;

    @KafkaListener(topics = "email.send", groupId = "email-service")
    public void listenDepositCompleted(EventMessage message) {
        var send = message.getPayloadAs(EmailSend.class);

        var layout = layoutService.findBySlug(LayoutSlug.fromString(send.getSlug()));

        layout.flatMap((l) -> {
                            return Mono.fromCallable(() -> {
                                if (l.isActive()) {
                                    
            String html = replaceVariables(
                l.getHtmlContent(),
                send.getVariables()
            );
            l.setHtmlContent(html);
                                    resendService.sendEmail(send, l);
                                }
                                return true;
                            });
                        }
                ).doOnSuccess(v -> log.info("Email sent successfully to {}", send.getTo()))
                .doOnError(e -> log.error("Failed to send email to {}", send.getTo(), e))
                .subscribe();
    }

    private String replaceVariables(String html, Map<String, String> variables) {
    if (variables == null || variables.isEmpty()) {
        return html;
    }

    String result = html;

    for (Map.Entry<String, String> entry : variables.entrySet()) {
        result = result.replace(
            entry.getKey(),
            entry.getValue() != null ? entry.getValue() : ""
        );
    }

    return result;
}
}
