package com.skmwizard.user.services;

import reactor.core.publisher.Mono;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
public interface VerificationService {
    Mono<Verification> add(Verification verification);

    Mono<Boolean> exists(Verification verification);

    Mono<Void> remove(Verification verification);
}
