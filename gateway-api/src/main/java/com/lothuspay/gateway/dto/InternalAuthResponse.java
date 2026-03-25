package com.lothuspay.gateway.dto;

import lombok.Data;

@Data
public class InternalAuthResponse {
    private String status;
    private String message;
    private InternalAuthData data;
}
