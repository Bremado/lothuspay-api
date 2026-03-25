package com.lothuspay.gateway.dto;

import lombok.*;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ValidateTokenRequest {
    private String token;
}
