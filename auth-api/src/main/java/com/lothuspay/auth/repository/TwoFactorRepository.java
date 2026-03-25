package com.lothuspay.auth.repository;

import com.lothuspay.auth.model.twofactor.TwoFactor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TwoFactorRepository extends MongoRepository<TwoFactor, String> {

    TwoFactor findByTempToken_TempToken(String tempTokenTempToken);

}
