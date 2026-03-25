package com.lothuspay.auth.dto.response.object.apikey;

import lombok.*;

import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetApiKey {

    private String clientId;
    private String clientSecret;

    private String endpoint;

    private List<String> allowlist;
}
