package com.lothuspay.email.service.resend;

import com.lothuspay.email.model.layout.Layout;
import com.lothuspay.events.dto.email.EmailSend;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ResendService {

    private final Resend resend;

    public void sendEmail(EmailSend emailSend, Layout layout) {
        
        try {
            resend.emails().send(
                    CreateEmailOptions.builder()
                            .from("Lothus Pay <" + emailSend.getFrom() + ">")
                            .to(emailSend.getTo())
                            .subject(layout.getSubject())
                            .html(layout.getHtmlContent())
                            .text(layout.getTextContent())
                            .build()
            );
        } catch (ResendException e) {
            System.out.println("Failed to send email via Resend: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
