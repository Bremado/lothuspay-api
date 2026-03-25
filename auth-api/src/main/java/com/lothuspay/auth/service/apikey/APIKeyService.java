package com.lothuspay.auth.service.apikey;

import com.lothuspay.auth.dto.response.DtoResponse;
import com.lothuspay.auth.dto.response.object.apikey.GetApiKey;
import com.lothuspay.auth.dto.response.object.apikey.PostValidateKey;
import com.lothuspay.auth.dto.response.status.DtoResponseStatus;
import com.lothuspay.auth.model.accounts.Account;
import com.lothuspay.auth.model.apikey.APIKey;
import com.lothuspay.auth.repository.APIKeyRepository;
import com.lothuspay.auth.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.lothuspay.auth.dto.response.status.DtoResponseStatus.*;

@Service
public class APIKeyService {

    @Autowired
    private APIKeyRepository repository;

    @Autowired
    private AccountRepository accountRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    public DtoResponse getApiKeys(Account account) {
        var apiKeys = repository.findByUserIdAndRevoked(account.getId(), false);

        var res = apiKeys.stream().map(apiKey -> GetApiKey.builder()
                .clientId(apiKey.getClientId())
                .clientSecret("lothpay_sec_********")
                .endpoint(apiKey.getEndpoint())
                .allowlist(apiKey.getAllowlist())
                .build()).toList();
        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(res)
                .message("API Keys retrieved successfully")
                .build();
    }

    public DtoResponse createApiKey(Account account, String endpoint) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        var clientId = "lothpay_cli_" + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(bytes)
                .substring(0, 12);

        bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        var clientSecret = "lothpay_sec_" +  Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        var apiKey = APIKey.builder()
                .id(UUID.randomUUID().toString())
                .userId(account.getId())
                .clientId(clientId)
                .clientSecret(BCrypt.hashpw(clientSecret, BCrypt.gensalt()))
                .endpoint(endpoint)
                .allowlist(new ArrayList<>())
                .revoked(false)
                .created(System.currentTimeMillis())
                .updated(System.currentTimeMillis())
                .build();

        repository.save(apiKey);

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(GetApiKey.builder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .build())
                .message("API Key created successfully")
                .build();
    }
    public DtoResponse revokeApiKey(Account account, String clientId) {
        var apiKey = repository.findByUserIdAndClientId(account.getId(), clientId);
        if (apiKey == null) {
            return DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_APIKEY_001)
                    .message("API Key not found")
                    .build();
        }

        apiKey.setRevoked(true);
        apiKey.setUpdated(System.currentTimeMillis());
        repository.save(apiKey);

        return DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .message("API Key revoked successfully")
                .build();
    }

    public ResponseEntity<DtoResponse> updateAllowlist(String clientId, List<String> allowlist) {
        var apikey = repository.findByClientId(clientId);

        if (apikey == null) {
            return ResponseEntity.status(404)
                    .body(DtoResponse.builder()
                            .status(DtoResponseStatus.ERR_APIKEY_001)
                            .message("API Key not found")
                            .build()
                    );
        }

        if (apikey.isRevoked()) {
            return ResponseEntity.status(401).body(DtoResponse.builder()
                    .status(ERR_APIKEY_003)
                    .message(ERR_APIKEY_003.getMessage())
                    .build());
        }

        apikey.setAllowlist(allowlist);
        apikey.setRevoked(false);
        apikey.setUpdated(System.currentTimeMillis());
        repository.save(apikey);

        return ResponseEntity.ok(
                DtoResponse.builder()
                        .status(DtoResponseStatus.SUCCESS)
                        .message("API Key updated successfully")
                        .build()
        );
    }

    public ResponseEntity<DtoResponse> validateApiKey(String clientId, String clientSecret) {
        var apikey = repository.findByClientId(clientId);

        if (apikey == null) {
            return ResponseEntity.status(401).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_APIKEY_002)
                    .message(ERR_APIKEY_002.getMessage())
                    .build());
        }

        if (apikey.isRevoked()) {
            return ResponseEntity.status(401).body(DtoResponse.builder()
                    .status(ERR_APIKEY_003)
                    .message(ERR_APIKEY_003.getMessage())
                    .build());
        }

        boolean matches = BCrypt.checkpw(clientSecret, apikey.getClientSecret());

        if (!matches) {
            return ResponseEntity.status(401).body(DtoResponse.builder()
                    .status(DtoResponseStatus.ERR_APIKEY_004)
                    .message(ERR_APIKEY_004.getMessage())
                    .build());
        }

        var user = accountRepository.findById(apikey.getUserId()).orElse(null);

        if (user == null) {
            return ResponseEntity.status(401).body(DtoResponse.builder()
                    .status(ERR_APIKEY_002)
                    .message(ERR_APIKEY_002.getMessage())
                    .build());
        }

        apikey.setLastUsed(System.currentTimeMillis());
        repository.save(apikey);

        return ResponseEntity.ok(DtoResponse.builder()
                .status(DtoResponseStatus.SUCCESS)
                .data(PostValidateKey.builder()
                        .userId(apikey.getUserId())
                        .clientId(apikey.getClientId())
                        .valid(true)
                        .roles(user.getRoles().stream().map(Enum::name).toList())
                        .segment(user.getSegment().name())
                        .build())
                .message("API Key validated successfully")
                .build());
    }

    public boolean hasAPIKey(String userId) {
        return repository.existsByUserIdAndRevoked(userId, false);
    }
}
