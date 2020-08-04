package com.skmwizard.iot.user.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface AgreeReceiveService {
    Mono<AgreeReceive> get(@NotBlank String code);

    Flux<AgreeReceive> get(AgreeReceive request, Integer page, Integer sizePerPage);

    Mono<AgreeReceive> add(@NotBlank String username, @NotNull @Valid AgreeReceive request);

    Mono<AgreeReceive> edit(@NotBlank String username, @NotBlank String code, @NotNull @Valid AgreeReceive request);

    Mono<Void> remove(@NotBlank String code);

    Mono<Long> count(AgreeReceive request);
}
