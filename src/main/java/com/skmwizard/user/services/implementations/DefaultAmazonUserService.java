package com.skmwizard.user.services.implementations;

import com.skmwizard.user.services.Token;
import com.skmwizard.user.services.User;
import com.skmwizard.user.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.rmi.NoSuchObjectException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
@Slf4j
class DefaultAmazonUserService implements UserService {
    private final CognitoIdentityProviderAsyncClient providerClient;
    private final AmazonUserConverter converter;
    private final UserConverter userConverter;
    private final UserRepository userRepository;
    private final String clientId;
    private final String userPoolId;

    public DefaultAmazonUserService(
        CognitoIdentityProviderAsyncClient providerClient,
        AmazonUserConverter converter,
        UserConverter userConverter,
        UserRepository userRepository,
        @Value("${aws.cognito.webclient.clientId}") String clientId,
        @Value("${aws.cognito.userPoolId}") String userPoolId) {
        this.providerClient = providerClient;
        this.converter = converter;
        this.userConverter = userConverter;
        this.userRepository = userRepository;
        this.clientId = clientId;
        this.userPoolId = userPoolId;
    }

    @Override
    public Mono<User> find(String name, String phoneNumber) {
        return userRepository.findByNameAndAndPhoneNumber(name, phoneNumber)
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
    public Mono<Void> signUp(User user) {
        return Mono.fromFuture(
            // AWS 사용자 등록
            providerClient.signUp(
                SignUpRequest.builder()
                    .clientId(clientId)
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .userAttributes(converter.converts(user))
                    .build())
        ).doOnNext(response ->
            // AWS 사용자 가입 승인
            Mono.fromFuture(
                providerClient.adminConfirmSignUp(
                    AdminConfirmSignUpRequest.builder()
                        .username(response.userSub())
                        .userPoolId(userPoolId)
                        .build()))
        ).flatMap(response ->
            // DB에 사용자 등록
            userRepository.save(
                new UserDocument(user.getEmail(),
                    user.getName(),
                    user.getPhoneNumber(),
                    LocalDateTime.now(),
                    user.getEmail(),
                    LocalDateTime.now(),
                    user.getEmail())
            )
        ).then();
    }

    @Override
    public Mono<Token> login(User user) {
        Map<String, String> auth = new ConcurrentHashMap<>();
        auth.put("USERNAME", user.getEmail());
        auth.put("PASSWORD", user.getPassword());

        return initiateAuth(auth, AuthFlowType.USER_PASSWORD_AUTH);
    }

    @Override
    public Mono<Void> logout(String username) {
        return Mono.fromFuture(
            providerClient.adminUserGlobalSignOut(
                AdminUserGlobalSignOutRequest.builder()
                    .username(username)
                    .userPoolId(userPoolId)
                    .build()
            )
        ).then();
    }

    @Override
    public Mono<User> get(String username) {
        log.info("get {}", username);
        return userRepository.findById(username)
            .switchIfEmpty(Mono.error(new NoSuchElementException(username)))
            .map(userConverter::converts);
    }

    @Override
    public Mono<User> updateUserInfo(String username, User user) {
        return Mono.zip(
            Mono.fromFuture(providerClient.adminUpdateUserAttributes(
                AdminUpdateUserAttributesRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .userAttributes(
                        AttributeType.builder()
                            .name("name").value(user.getName())
                            .build())
                    .build()
            )),
            userRepository.findById(username)
                .switchIfEmpty(Mono.error(new NoSuchElementException(user.getEmail())))
                .flatMap(userDocument -> {
                    userDocument.setName(user.getName());
                    userDocument.setPhoneNumber(user.getPhoneNumber());
                    userDocument.setUpdatedDatetime(LocalDateTime.now());
                    userDocument.setUpdater(username);
                    return userRepository.save(userDocument);
                })
        ).flatMap(aVoid -> userRepository.findById(username).map(userConverter::converts));
    }

    @Override
    public Mono<Void> resetPassword(String username, String resetPassword) {
        Map<String, String> auth = new ConcurrentHashMap<>();
        auth.put("USERNAME", username);
        auth.put("NEW_PASSWORD", resetPassword);

        return Mono.fromFuture(
            providerClient.adminSetUserPassword(
                AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .permanent(true)
                    .password(resetPassword)
                    .build()
            )
        ).doOnNext(response ->
            providerClient.respondToAuthChallenge(
                RespondToAuthChallengeRequest.builder()
                    .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                    .clientId(clientId)
                    .challengeResponses(auth)
                    .build())
        ).then();
    }

    @Override
    public Mono<Token> refreshToken(String refreshToken) {
        Map<String, String> auth = new ConcurrentHashMap<>();
        auth.put("REFRESH_TOKEN", refreshToken);

        return initiateAuth(auth, AuthFlowType.REFRESH_TOKEN_AUTH);
    }

    /**
     * 인증 FlowType 에 대한 Token 요청
     *
     * @param authParameters 인증 파라미터
     * @param authFlowType   인증 플로우 타입
     * @return 토큰
     */
    private Mono<Token> initiateAuth(Map<String, String> authParameters, AuthFlowType authFlowType) {
        return Mono.fromFuture(
            providerClient.initiateAuth(
                InitiateAuthRequest.builder()
                    .clientId(clientId)
                    .authParameters(authParameters)
                    .authFlow(authFlowType)
                    .build()
            )
        ).map(response -> converter.converts(response.authenticationResult()));
    }
}
