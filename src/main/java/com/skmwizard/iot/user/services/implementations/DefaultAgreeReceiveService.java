package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.AgreeReceive;
import com.skmwizard.iot.user.services.AgreeReceiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
class DefaultAgreeReceiveService implements AgreeReceiveService {
    private final AgreeReceiveRepository repository;
    private final AgreeReceiveConverter converter;

    @Override
    public Mono<AgreeReceive> get(@NotBlank String code) {
        log.debug("[get] id: {}", code);
        return repository.findById(code).map(converter::converts);
    }

    @Override
    public Flux<AgreeReceive> get(AgreeReceive agreeReceive, Integer page, Integer sizePerPage) {
        log.debug("[get] AgreeReceive: {}, page: {}, sizePerPage: {}", agreeReceive, page, sizePerPage);
        Example<AgreeReceiveDocument> example = makeDocumentExample(agreeReceive);

        return Optional.ofNullable(page)
            .map(pageNumber -> {
                long size = Optional.ofNullable(sizePerPage).orElse(20);
                return repository.findAll(example)
                    .skip(pageNumber * size)
                    .take(size)
                    .map(converter::converts);
            }).orElseGet(() -> repository.findAll(example).map(converter::converts));
    }

    @Override
    public Mono<AgreeReceive> add(@NotBlank String email, @NotNull @Valid AgreeReceive request) {
        log.debug("[add] AgreeReceive: {}", request);
        return repository.existsByCodeAndEmail(request.getCode(), email)
            .flatMap(exists -> {
                if (exists.equals(Boolean.TRUE)) {
                    return Mono.error(() -> new DuplicateKeyException(request.getCode()));
                }
                AgreeReceiveDocument document = converter.converts(request);
                document.setCode(request.getCode());
                document.setAgreedDatetime(LocalDateTime.now());
                document.setEmail(email);
                return repository.save(document)
                    .doOnSubscribe(saved -> log.info("[add] AgreeReceive: {} added.", saved))
                    .map(converter::converts);
            });
    }

    @Override
    public Mono<AgreeReceive> edit(@NotBlank String email, @NotBlank String code, @NotNull @Valid AgreeReceive request) {
        log.debug("[edit] code: {}, AgreeReceive: {}", code, request);
        return repository.findById(code)
            .flatMap(document -> {
                document.setCode(request.getCode());
                document.setEmail(email);
                document.setAgreedDatetime(LocalDateTime.now());

                return repository.save(document)
                    .doOnSuccess(saved -> log.info("[edit] AgreeReceive: {} updated.", saved))
                    .map(converter::converts);
            }).switchIfEmpty(Mono.error(() -> new NoSuchElementException(code)));
    }

    @Override
    public Mono<Void> remove(@NotBlank String code) {
        log.debug("[remove] code: {}", code);
        return repository.deleteById(code).doOnSuccess(voidMono -> log.info("[remove] AgreeReceive: {} removed.", code));
    }

    @Override
    public Mono<Long> count(AgreeReceive parameter) {
        return repository.count(makeDocumentExample(parameter));
    }

    private Example<AgreeReceiveDocument> makeDocumentExample(AgreeReceive parameter) {
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
            .withIgnoreNullValues()
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
            .withIgnorePaths("logo_image_path, created_datetime, updated_datetime, creator_id, updater_id");

        return Example.of(converter.converts(parameter), exampleMatcher);
    }
}
