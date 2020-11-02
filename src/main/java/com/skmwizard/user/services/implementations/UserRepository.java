package com.skmwizard.user.services.implementations;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

interface UserRepository extends ReactiveMongoRepository<UserDocument, String> {
    Mono<UserDocument> findByNameAndPhoneNumber(String name, String phoneNumber);
}
