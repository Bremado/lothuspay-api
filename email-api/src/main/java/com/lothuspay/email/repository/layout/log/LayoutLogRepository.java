package com.lothuspay.email.repository.layout.log;

import com.lothuspay.email.model.layout.logs.LayoutLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LayoutLogRepository extends ReactiveMongoRepository<LayoutLog, String> {
}
