package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.Verification;
import com.skmwizard.iot.user.services.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Mono;

import java.rmi.NoSuchObjectException;
import java.time.LocalDateTime;

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
    public Mono<Verification> add(Verification verification) {
        VerificationDocument document = converter.converts(verification);
        document.setCreatedDatetime(LocalDateTime.now());
        return repository.save(document)
            .doOnSuccess(saved -> log.info("[add] Verification : {} saved", saved.getChecker()))
            .map(converter::converts);
    }

    @Override
    public Mono<Boolean> exists(Verification verification) {
        return repository.findByCheckerAndVerificationCode(verification.getChecker(), verification.getVerificationCode())
            .switchIfEmpty(Mono.error(new NoSuchObjectException("인증번호를 확인해주세요.")))
            .doOnSuccess(saved -> log.info("[exists] Verification : {} exist.", saved.getChecker()))
            .map(document -> Boolean.TRUE);
    }

    @Override
    public Mono<Void> remove(Verification verification) {
        return repository.deleteById(verification.getChecker())
            .doOnSuccess(aVoid -> log.info("[remove] Verification: {} removed.", verification.getChecker()));
    }


}
