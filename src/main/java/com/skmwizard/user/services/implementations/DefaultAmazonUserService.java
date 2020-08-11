package com.skmwizard.user.services.implementations;

import com.skmwizard.user.services.ChangePassword;
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
    public Mono<User> find(String username, String phoneNumber) {
        return userRepository.findByNameAndAndPhoneNumber(username, phoneNumber)
            .switchIfEmpty(Mono.error(new NoSuchObjectException(phoneNumber)))
            .map(userConverter::converts);
    }

    @Override
    public Mono<Void> exists(String email) {
        return userRepository.existsById(email)
            .flatMap(exists -> {
                if (exists.equals(Boolean.TRUE)) {
                    return Mono.error(() -> new DuplicateKeyException(email));
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
                        .username(user.getEmail())
                        .userPoolId(userPoolId)
                        .build()))
        ).flatMap(response ->
            // DB에 사용자 등록
            userRepository.save(
                new UserDocument(user.getEmail(),
                    response.userSub(),
                    user.getName(),
                    user.getPhoneNumber(),
                    LocalDateTime.now())
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
    public Mono<Void> logout(String accessToken) {
        return Mono.fromFuture(
            providerClient.globalSignOut(
                GlobalSignOutRequest.builder()
                    .accessToken(accessToken)
                    .build()
            )
        ).then();
    }

    @Override
    public Mono<User> getUserInfo(String accessToken) {
        log.info("getUserInfo {}", accessToken);
        return userInfo(accessToken)
            .flatMap(user -> this.get(user.getEmail()));
    }

    @Override
    public Mono<User> get(String username) {
        log.info("get {}", username);
        return userRepository.findById(username)
            .switchIfEmpty(Mono.error(new NoSuchElementException(username)))
            .map(userConverter::converts);
    }

    @Override
    public Mono<User> updateUserInfo(String accessToken, User user) {
        return Mono.zip(
            Mono.fromFuture(providerClient.updateUserAttributes(
                UpdateUserAttributesRequest.builder()
                    .accessToken(accessToken)
                    .userAttributes(
                        AttributeType.builder()
                            .name("name").value(user.getName())
                            .build())
                    .build()
            )),
            this.get(user.getEmail())
                .map(edit -> userRepository.save(userConverter.converts(user)))
        ).flatMap(aVoid -> userRepository.findById(user.getEmail()).map(userConverter::converts));
    }

    @Override
    public Mono<Void> changePassword(String accessToken, ChangePassword changePassword) {
        return Mono.just(
            providerClient.changePassword(
                ChangePasswordRequest.builder()
                    .accessToken(accessToken)
                    .previousPassword(changePassword.getOldPassword())
                    .proposedPassword(changePassword.getNewPassword())
                    .build()
            ).join()
        ).then();
    }

    @Override
    public Mono<Void> resetPassword(String email, String resetPassword) {
        Map<String, String> auth = new ConcurrentHashMap<>();
        auth.put("USERNAME", email);
        auth.put("NEW_PASSWORD", resetPassword);

        return Mono.fromFuture(
            providerClient.adminSetUserPassword(
                AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(email)
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

    /**
     * 사용자 정보 요청
     *
     * @param accessToken 토큰
     * @return 사용자 정보
     */
    private Mono<User> userInfo(String accessToken) {
        return Mono.fromFuture(
            providerClient.getUser(
                GetUserRequest.builder()
                    .accessToken(accessToken)
                    .build())
        ).map(response -> converter.converts(response.userAttributes()));
    }
}
