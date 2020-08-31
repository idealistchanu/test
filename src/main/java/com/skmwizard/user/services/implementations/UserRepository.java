package com.skmwizard.user.services.implementations;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

interface UserRepository extends ReactiveCrudRepository<UserDocument, String> {
    Mono<UserDocument> findByNameAndPhoneNumber(String name, String phoneNumber);
}
