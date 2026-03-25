package com.lothuspay.auth.model.apikey;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "api_keys")
public class APIKey {

    @Id
    private String id;
    private String userId;

    private String clientId;
    private String clientSecret;

    private String endpoint;

    private List<String> allowlist;

    private boolean revoked;

    private long created;
    private long updated;

    private long lastUsed;
}
