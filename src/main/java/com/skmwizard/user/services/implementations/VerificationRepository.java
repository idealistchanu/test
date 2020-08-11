package com.skmwizard.user.services.implementations;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
public interface VerificationRepository extends ReactiveMongoRepository<VerificationDocument, String> {
    Mono<VerificationDocument> findByCheckerAndVerificationCode(String phoneNumber, String verificationCode);
}
