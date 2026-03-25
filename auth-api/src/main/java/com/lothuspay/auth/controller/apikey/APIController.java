package com.lothuspay.auth.controller.apikey;

import com.lothuspay.auth.dto.request.DtoAuthApiKeyAllowlist;
import com.lothuspay.auth.dto.request.DtoAuthApiKeyCreate;
import com.lothuspay.auth.dto.response.DtoResponse;
import com.lothuspay.auth.dto.response.status.DtoResponseStatus;
import com.lothuspay.auth.repository.APIKeyRepository;
import com.lothuspay.auth.service.account.AccountService;
import com.lothuspay.auth.service.apikey.APIKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.util.ArrayList;

@RestController
@RequestMapping("/auth/apikey")
public class APIController {

    @Autowired
    private APIKeyService apiKeyService;

    @Autowired
    private AccountService accountService;

    @GetMapping
    public ResponseEntity<DtoResponse> apiKey(@AuthenticationPrincipal UserDetails details) {
        var account = accountService.find(details.getUsername());

        if (account == null) {
            return ResponseEntity.status(404).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build());
        }

        var has = apiKeyService.hasAPIKey(account.getId());

        if (has) {
            return ResponseEntity.ok(apiKeyService.getApiKeys(account));
        }

        return ResponseEntity.ok(
                DtoResponse.builder()
                        .status(DtoResponseStatus.SUCCESS)
                        .message("API Key is not exists.")
                        .build()
        );
    }

    @PostMapping("/create")
    public ResponseEntity<DtoResponse> create(@RequestBody DtoAuthApiKeyCreate dto, @AuthenticationPrincipal UserDetails details) {
        var account = accountService.find(details.getUsername());

        if (account == null) {
            return ResponseEntity.status(404).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build());
        }

        var has =  apiKeyService.hasAPIKey(account.getId());

        if (has) {
            return ResponseEntity.ok(apiKeyService.getApiKeys(account));
        }

        return ResponseEntity.status(201).body(apiKeyService.createApiKey(account, dto.getEndpoint()));
    }

    @PostMapping("/{clientId}/allowlist")
    public ResponseEntity<DtoResponse> allowlist(@PathVariable("clientId") String clientId, @RequestBody DtoAuthApiKeyAllowlist dto, @AuthenticationPrincipal UserDetails details) {
        var account = accountService.find(details.getUsername());

        if (account == null) {
            return ResponseEntity.status(404).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build());
        }

        var has =  apiKeyService.hasAPIKey(account.getId());

        if (!has) {
            return ResponseEntity.status(404).body(
                    DtoResponse.builder()
                            .status(DtoResponseStatus.ERR_APIKEY_001)
                            .message(DtoResponseStatus.ERR_APIKEY_001.getMessage())
                            .build()
            );
        }

        var allowlist = new ArrayList<String>();

        dto.getAllowlist().forEach(address -> {
            var is = isAddress(address);
            if (is) {
                allowlist.add(address.replace(" ", ""));
            }
        });

        return apiKeyService.updateAllowlist(clientId, allowlist);
    }

    @DeleteMapping("/{clientId}/revoke")
    public ResponseEntity<DtoResponse> revokeApiKey(@PathVariable("clientId") String clientId,@AuthenticationPrincipal UserDetails details) {
        var account = accountService.find(details.getUsername());

        if (account == null) {
            return ResponseEntity.status(404).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_007)
                    .message(DtoResponseStatus.ERR_AUTH_007.getMessage())
                    .build());
        }

        return ResponseEntity.ok(apiKeyService.revokeApiKey(account, clientId));
    }

    private boolean isAddress(String address) {
        try {
            InetAddress.getByName(address);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
