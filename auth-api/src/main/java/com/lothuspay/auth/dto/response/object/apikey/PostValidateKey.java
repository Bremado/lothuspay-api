package com.lothuspay.auth.dto.response.object.apikey;

import lombok.*;

import java.util.List;

@Builder
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostValidateKey {

    private String userId;
    private String clientId;

    private boolean valid;

    private List<String> roles;

    private String segment;
}

