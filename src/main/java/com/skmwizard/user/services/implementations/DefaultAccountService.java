package com.skmwizard.user.services.implementations;

import com.skmwizard.user.clouds.CloudUserManager;
import com.skmwizard.user.services.AccountService;
import com.skmwizard.user.services.Token;
import com.skmwizard.user.services.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.rmi.NoSuchObjectException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Primary
@Slf4j
class DefaultAccountService implements AccountService {
    private final CloudUserManager cloudUserManager;
    private final UserConverter userConverter;
    private final UserRepository userRepository;

    public DefaultAccountService(
        CloudUserManager cloudUserManager,
        UserConverter userConverter,
        UserRepository userRepository) {
        this.cloudUserManager = cloudUserManager;
        this.userConverter = userConverter;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> find(String name, String phoneNumber) {
        return userRepository.findByNameAndPhoneNumber(name, phoneNumber)
            .switchIfEmpty(Mono.error(new NoSuchObjectException(phoneNumber)))
            .map(userConverter::converts);
    }

    @Override
    public Mono<Void> exists(String username) {
        return userRepository.existsById(username)
            .flatMap(exists -> {
                if (exists.equals(Boolean.TRUE)) {
                    return Mono.error(() -> new DuplicateKeyException(username));
                }
                return Mono.empty();
            });
    }

    @Override
    public Mono<User> signUp(User user) {
        UserDocument parameter = new UserDocument(user.getEmail(),
            null,
            user.getName(),
            user.getPhoneNumber(),
            LocalDateTime.now(),
            user.getEmail(),
            LocalDateTime.now(),
            user.getEmail());
        return cloudUserManager.register(user)
            .flatMap(response ->
                // DB에 사용자 등록
                userRepository.save(parameter))
            .map(userConverter::converts);
    }

    @Override
    public Mono<Token> login(User user) {
        return cloudUserManager.login(user);
    }

    @Override
    public Mono<Void> logout(String username) {
        return cloudUserManager.logout(username).then();
    }

    @Override
    public Mono<User> updateUserInfo(String username, User user) {
        return cloudUserManager.edit(user)
            .flatMap(edited ->
                userRepository.findById(username)
                    .switchIfEmpty(Mono.error(new NoSuchElementException(user.getEmail())))
                    .flatMap(userDocument -> {
                        userDocument.setName(user.getName());
                        userDocument.setPhoneNumber(user.getPhoneNumber());
                        userDocument.setUpdatedDatetime(LocalDateTime.now());
                        userDocument.setUpdater(username);
                        return userRepository.save(userDocument);
                    })
            ).map(userConverter::converts);
    }

    @Override
    public Mono<Void> changePicture(String username, String picture) {
        return userRepository.findById(username)
            .switchIfEmpty(Mono.error(new NoSuchObjectException(username)))
            .flatMap(userDocument -> {
                userDocument.setPicture(picture);
                userDocument.setUpdatedDatetime(LocalDateTime.now());
                userDocument.setUpdater(username);
                return userRepository.save(userDocument);
            })
            .doOnSuccess(userDocument -> log.debug("change picture complete"))
            .then();
    }

    @Override
    public Mono<Void> resetPassword(String username, String resetPassword) {
        return cloudUserManager.resetPassword(username, resetPassword).then();
    }

    @Override
    public Mono<Token> refreshToken(String refreshToken) {
        return cloudUserManager.refreshToken(refreshToken);
    }
}
