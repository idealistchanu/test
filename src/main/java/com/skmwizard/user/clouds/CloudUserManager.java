package com.skmwizard.user.clouds;

import com.skmwizard.user.services.Token;
import com.skmwizard.user.services.User;
import reactor.core.publisher.Mono;

/**
 * @author ingu_ko
 * @since 2020-10-12
 */
public interface CloudUserManager {
    Mono<Token> login(User user);

    Mono<Boolean> logout(String username);

    Mono<Token> refreshToken(String refreshToken);

    Mono<User> register(User user);

    Mono<User> edit(User user);

    Mono<Boolean> resetPassword(String username, String resetPassword);

    Mono<Boolean> remove(String username);
}
