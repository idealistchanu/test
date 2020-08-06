package com.skmwizard.iot.user.services.implementations;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

interface UserRepository extends ReactiveCrudRepository<UserDocument, String> {
    Mono<UserDocument> findByNameAndAndPhoneNumber(String name, String phoneNumber);
}
