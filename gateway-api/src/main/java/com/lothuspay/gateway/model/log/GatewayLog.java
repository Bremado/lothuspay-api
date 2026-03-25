package com.lothuspay.gateway.model.log;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "gateway_logs")
public class GatewayLog {

    @Id
    private String id;

    private String target;

    private String method;
    private String path;

    private String clientIp;

    private Map<String, String> headers;

    private String body;

    private int code;

    private long duration;

    private long timestamp;
}
