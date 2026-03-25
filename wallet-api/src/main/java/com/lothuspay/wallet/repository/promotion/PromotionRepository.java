package com.lothuspay.wallet.repository.promotion;

import com.lothuspay.wallet.model.promotion.Promotion;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRepository extends ReactiveMongoRepository<Promotion, String> {
}
