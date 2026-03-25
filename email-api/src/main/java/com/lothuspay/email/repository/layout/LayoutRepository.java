package com.lothuspay.email.repository.layout;

import com.lothuspay.email.model.layout.Layout;
import com.lothuspay.email.model.layout.slug.LayoutSlug;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface LayoutRepository extends ReactiveMongoRepository<Layout, String> {

    Mono<Layout> findBySlug(LayoutSlug slug);
}
