package com.lothuspay.auth.controller.internal;

import com.lothuspay.auth.dto.response.DtoResponse;
import com.lothuspay.auth.service.apikey.APIKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/internal")
public class AuthInternalController {

    @Autowired
    private APIKeyService apiKeyService;

    @PostMapping("/apikey/validate")
    public ResponseEntity<DtoResponse> validateApiKey(@RequestHeader("X-Client-ID") String clientId,
                                                      @RequestHeader("X-Client-Secret") String clientSecret) {
        return apiKeyService.validateApiKey(clientId, clientSecret);
    }
}
