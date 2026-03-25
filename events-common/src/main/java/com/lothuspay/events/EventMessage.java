package com.lothuspay.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.*;

@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventMessage {

    private String id;
    private String type;
    private long timestamp;
    private Object payload;

    public <T> T getPayloadAs(Class<T> type) {
        var MAPPER = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return MAPPER.convertValue(payload, type);
    }
}
