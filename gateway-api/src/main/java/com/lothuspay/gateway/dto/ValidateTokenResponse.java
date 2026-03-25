package com.lothuspay.gateway.dto;

import lombok.*;

import java.util.List;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidateTokenResponse {

    private String status;
    private Data data;

    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {

        private boolean valid;
        private String email;
        private String userId;
        private List<String> roles;

        private boolean verified;
        private boolean emailVerified;

        private String segment;

    }
}