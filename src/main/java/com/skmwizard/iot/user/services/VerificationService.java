package com.skmwizard.iot.user.services;

import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
public interface VerificationService {
    Mono<Verification> add(@NotBlank String username, Verification verification);

    Mono<Void> checkVerification(@NotBlank String username, Verification verification);
}
