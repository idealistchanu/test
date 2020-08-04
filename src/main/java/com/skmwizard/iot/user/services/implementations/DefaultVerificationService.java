package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.Verification;
import com.skmwizard.iot.user.services.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import java.rmi.NoSuchObjectException;

/**
 * @author jongduck_yoon
 * @since 2020-06-12
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Validated
class DefaultVerificationService implements VerificationService {
    private final VerificationRepository repository;
    private final VerificationConverter converter;

    @Override
    public Mono<Verification> add(@NotBlank String username, Verification verification) {
        VerificationDocument document = converter.converts(verification);
        document.setUsername(username);
        return repository.save(document).map(converter::converts)
                .doOnSuccess(savedVerification -> log.info("[add] Verification : {}", savedVerification.toString()));
    }

    @Override
    public Mono<Void> checkVerification(@NotBlank String username, Verification verification) {
        return repository.findByUsername(username)
                .switchIfEmpty(Mono.error(new NoSuchObjectException("Username")))
                .doOnSuccess(document -> {
                    repository.deleteByUsername(username).doOnSuccess(monoVoid -> log.info("[remove] Verification: {} {} removed.", username, verification.toString())).subscribe();
                    if(!document.getVerificationCode().equals(verification.getVerificationCode())) {
                        throw new IllegalArgumentException("Verification code is not valid");
                    }
                }).then();

    }
}
