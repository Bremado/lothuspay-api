package com.lothuspay.auth.controller.admin;

import com.lothuspay.auth.dto.response.DtoResponse;
import com.lothuspay.auth.dto.response.status.DtoResponseStatus;
import com.lothuspay.auth.model.accounts.role.AccountRole;
import com.lothuspay.auth.model.apikey.APIKey;
import com.lothuspay.auth.repository.APIKeyRepository;
import com.lothuspay.auth.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth/admin/apikeys")
public class APIKeyAdminController {

    @Autowired
    private APIKeyRepository apiKeyRepository;

    @Autowired
    private AccountService accountService;

    @GetMapping
    public ResponseEntity<DtoResponse> getAllApiKeys(
            @AuthenticationPrincipal UserDetails details,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "revoked", required = false) Boolean revoked) {
        
        var found = accountService.find(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER))) {
            return ResponseEntity.status(403).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build());
        }

        var pageable = PageRequest.of(page - 1, limit);
        Page<APIKey> apiKeysPage;

        if (userId != null && revoked != null) {
            var allApiKeys = apiKeyRepository.findByUserIdAndRevoked(userId, revoked);
            var apiKeysList = allApiKeys.stream()
                    .skip(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .toList();
            apiKeysPage = new org.springframework.data.domain.PageImpl<>(apiKeysList, pageable, allApiKeys.size());
        } else if (userId != null) {
            var allApiKeys = apiKeyRepository.findAll();
            var filteredApiKeys = allApiKeys.stream()
                    .filter(key -> key.getUserId().equals(userId))
                    .toList();
            var apiKeysList = filteredApiKeys.stream()
                    .skip(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .toList();
            apiKeysPage = new org.springframework.data.domain.PageImpl<>(apiKeysList, pageable, filteredApiKeys.size());
        } else if (revoked != null) {
            var allApiKeys = apiKeyRepository.findAll();
            var filteredApiKeys = allApiKeys.stream()
                    .filter(key -> key.isRevoked() == revoked)
                    .toList();
            var apiKeysList = filteredApiKeys.stream()
                    .skip(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .toList();
            apiKeysPage = new org.springframework.data.domain.PageImpl<>(apiKeysList, pageable, filteredApiKeys.size());
        } else {
            apiKeysPage = apiKeyRepository.findAll(pageable);
        }

        var response = new HashMap<String, Object>();
        response.put("apiKeys", apiKeysPage.getContent());
        response.put("total", apiKeysPage.getTotalElements());
        response.put("page", page);
        response.put("limit", limit);
        response.put("totalPages", apiKeysPage.getTotalPages());

        return ResponseEntity.ok(DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(response)
                .message("API Keys recuperadas com sucesso.")
                .build());
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<DtoResponse> getApiKeyById(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable("clientId") String clientId) {
        
        var found = accountService.find(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER))) {
            return ResponseEntity.status(403).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build());
        }

        var apiKey = apiKeyRepository.findByClientId(clientId);
        if (apiKey == null) {
            return ResponseEntity.status(404).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_APIKEY_001)
                    .message("API Key não encontrada.")
                    .build());
        }

        return ResponseEntity.ok(DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(apiKey)
                .message("API Key recuperada com sucesso.")
                .build());
    }

    @DeleteMapping("/{clientId}/revoke")
    public ResponseEntity<DtoResponse> revokeApiKey(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable("clientId") String clientId) {
        
        var found = accountService.find(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER))) {
            return ResponseEntity.status(403).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build());
        }

        var apiKey = apiKeyRepository.findByClientId(clientId);
        if (apiKey == null) {
            return ResponseEntity.status(404).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_APIKEY_001)
                    .message("API Key não encontrada.")
                    .build());
        }

        apiKey.setRevoked(true);
        apiKey.setUpdated(System.currentTimeMillis());
        apiKeyRepository.save(apiKey);

        return ResponseEntity.ok(DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .message("API Key revogada com sucesso.")
                .build());
    }

    @GetMapping("/{clientId}/stats")
    public ResponseEntity<DtoResponse> getApiKeyStats(
            @AuthenticationPrincipal UserDetails details,
            @PathVariable("clientId") String clientId) {
        
        var found = accountService.find(details.getUsername());
        if (found == null || (!found.getRoles().contains(AccountRole.CEO) && !found.getRoles().contains(AccountRole.MANAGER))) {
            return ResponseEntity.status(403).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_AUTH_009)
                    .message(DtoResponseStatus.ERR_AUTH_009.getMessage())
                    .build());
        }

        var apiKey = apiKeyRepository.findByClientId(clientId);
        if (apiKey == null) {
            return ResponseEntity.status(404).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_APIKEY_001)
                    .message("API Key não encontrada.")
                    .build());
        }

        var stats = new HashMap<String, Object>();
        stats.put("clientId", apiKey.getClientId());
        stats.put("revoked", apiKey.isRevoked());
        stats.put("created", apiKey.getCreated());
        stats.put("updated", apiKey.getUpdated());
        stats.put("lastUsed", apiKey.getLastUsed());
        stats.put("endpoint", apiKey.getEndpoint());
        stats.put("allowlistSize", apiKey.getAllowlist() != null ? apiKey.getAllowlist().size() : 0);

        return ResponseEntity.ok(DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(stats)
                .message("Estatísticas de API Key recuperadas com sucesso.")
                .build());
    }

}

