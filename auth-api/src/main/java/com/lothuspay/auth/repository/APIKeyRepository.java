package com.lothuspay.auth.repository;

import com.lothuspay.auth.model.apikey.APIKey;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APIKeyRepository extends MongoRepository<APIKey, String> {

    List<APIKey> findByUserIdAndRevoked(String userId, boolean revoked);
    APIKey findByUserIdAndClientId(String userId, String clientId);
    boolean existsByUserIdAndRevoked(String userId, boolean revoked);

    APIKey findByClientId(String clientId);

}
