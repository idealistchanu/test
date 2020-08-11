package com.skmwizard.user.services.implementations;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

interface AgreeReceiveRepository extends ReactiveMongoRepository<AgreeReceiveDocument, String> {
    Mono<Boolean> existsByCodeAndEmail(String code, String email);
}
