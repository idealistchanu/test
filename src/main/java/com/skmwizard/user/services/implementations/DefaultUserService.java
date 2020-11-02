package com.skmwizard.user.services.implementations;

import com.skmwizard.user.clouds.CloudUserManager;
import com.skmwizard.user.services.User;
import com.skmwizard.user.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.rmi.NoSuchObjectException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@Primary
@Slf4j
class DefaultUserService implements UserService {
    private final CloudUserManager cloudUserManager;
    private final UserRepository userRepository;
    private final UserConverter userConverter;

    DefaultUserService(CloudUserManager cloudUserManager,
                       UserRepository userRepository,
                       UserConverter userConverter) {
        this.cloudUserManager = cloudUserManager;
        this.userRepository = userRepository;
        this.userConverter = userConverter;
    }

    @Override
    public Mono<User> add(@NotBlank String username, @NotNull User user) {
        log.debug("[add] user: {}", user);
        UserDocument parameter = userConverter.converts(user);
        parameter.setCreator(username);
        parameter.setCreatedDatetime(LocalDateTime.now());
        parameter.setUpdater(username);
        parameter.setUpdatedDatetime(LocalDateTime.now());
        return cloudUserManager.register(user)
            .flatMap(response ->
                // DB에 사용자 등록
                userRepository.save(parameter))
            .map(userConverter::converts);
    }

    @Override
    public Mono<User> get(String username) {
        log.debug("[get] username: {}", username);
        return userRepository.findById(username)
            .switchIfEmpty(Mono.error(new NoSuchObjectException(username)))
            .map(userConverter::converts);
    }

    @Override
    public Flux<User> get(@NotNull User user) {
        log.debug("[get] user: {}", user);
        return userRepository.findAll(this.makeExample(user), Sort.by(Sort.Order.desc("createdDatetime")))
            .switchIfEmpty(Mono.error(NoSuchElementException::new))
            .map(userConverter::converts);
    }

    @Override
    public Mono<User> edit(@NotBlank String username, @NotNull User user) {
        log.debug("[edit] user: {}", user);
        return cloudUserManager.edit(user)
            .flatMap(edited ->
                userRepository.findById(username)
                    .switchIfEmpty(Mono.error(new NoSuchObjectException(username)))
                    .flatMap(userDocument -> {
                        userDocument.setEmail(user.getEmail());
                        userDocument.setName(user.getName());
                        userDocument.setPhoneNumber(user.getPhoneNumber());
                        userDocument.setUpdatedDatetime(LocalDateTime.now());
                        userDocument.setUpdater(username);
                        return userRepository.save(userDocument);
                    })
            ).map(userConverter::converts);
    }

    @Override
    public Mono<Void> remove(@NotBlank String username) {
        log.debug("[remove] username: {}", username);
        return userRepository.deleteById(username)
            .doFinally(signalType -> cloudUserManager.remove(username))
            .doOnSuccess(v -> log.info("user: {} removed.", username))
            .then();
    }

    @Override
    public Mono<Long> count(@NotBlank User user) {
        log.debug("[get] count: {}", user);
        return userRepository.count(this.makeExample(user));
    }

    private Example<UserDocument> makeExample(User user) {
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
            .withIgnorePaths("_class")
            .withIgnoreNullValues()
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        UserDocument parameter = userConverter.converts(user);
        return Example.of(parameter, exampleMatcher);
    }
}
