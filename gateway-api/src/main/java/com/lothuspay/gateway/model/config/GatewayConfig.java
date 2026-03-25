package com.lothuspay.gateway.model.config;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "gateway_configs")
public class GatewayConfig {

    @Id
    private String id;

    private String name;
    private String description;

    private String logoUrl;
    private String faviconUrl;

}
