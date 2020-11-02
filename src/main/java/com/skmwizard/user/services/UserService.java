package com.skmwizard.user.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface UserService {

    Mono<User> add(@NotBlank String username, @NotNull User user);

    Mono<User> get(@NotBlank String username);

    Flux<User> get(@NotNull User user);

    Mono<User> edit(@NotBlank String username, @NotNull User user);

    Mono<Void> remove(@NotBlank String username);

    Mono<Long> count(@NotBlank User user);
}
