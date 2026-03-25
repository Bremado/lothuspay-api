package com.lothuspay.gateway.dto;

import lombok.Data;

import java.util.List;

@Data
public class InternalAuthData {
    private boolean valid;
    private String clientId;
    private String userId;
    private List<String> roles;
    private String segment;
}
