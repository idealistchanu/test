package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.ChangePassword;
import com.skmwizard.iot.user.services.Token;
import com.skmwizard.iot.user.services.User;
import com.skmwizard.iot.user.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
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
    public Mono<Void> exists(String email) {
        return userRepository.existsById(email).flatMap(exists -> {
            if(exists.equals(Boolean.TRUE)) {
                return Mono.error(() -> new DuplicateKeyException(email));
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<Void> signUp(User user) {
        return Mono.just(
            providerClient.signUp(
                SignUpRequest.builder()
                    .clientId(clientId)
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .userAttributes(converter.converts(user))
                    .build()
            ).whenComplete((response, throwable) -> {
                if (response == null) log.error(throwable.getMessage());
                Mono.when(
                    // 사용자 가입 상태를 확인된 상태로 변경 - 관리자 권한
                    Mono.just(providerClient.adminConfirmSignUp(
                        AdminConfirmSignUpRequest.builder()
                            .username(user.getEmail())
                            .userPoolId(userPoolId)
                            .build()).join()),
                    // 사용자 정보 DB에 저장
                    userRepository.save(new UserDocument(user.getEmail(), user.getName(), user.getPhoneNumber()))
                ).subscribe();
            }).join()
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
        return Mono.just(
            providerClient.globalSignOut(
                GlobalSignOutRequest.builder()
                    .accessToken(accessToken)
                    .build()
            ).join()
        ).then();
    }

    @Override
    public Mono<User> getUserInfo(String accessToken) {
        log.info("getUserInfo {}", accessToken);
        return userInfo(accessToken)
            .flatMap(user -> userRepository.findById(user.getEmail()))
            .map(userConverter::converts);
    }

    @Override
    public Mono<User> updateUserInfo(String accessToken, User user) {
        return Mono.zip(Mono.just(providerClient.updateUserAttributes(
            UpdateUserAttributesRequest.builder()
                .accessToken(accessToken)
                .userAttributes(
                    Optional.ofNullable(
                        AttributeType.builder()
                            .name("name").value(user.getName())
                            .build())
                        .orElseThrow(NoSuchElementException::new)
                )
                .build()
            ).join()),
            userRepository.findById(user.getEmail())
                .switchIfEmpty(Mono.error(new NoSuchElementException(user.getEmail())))
                .map(userDocument -> userRepository.save(userConverter.converts(user))))
            .flatMap(aVoid -> userRepository.findById(user.getEmail()).map(userConverter::converts));
    }

    @Override
    public Mono<Void> resetPassword(String accessToken, ChangePassword changePassword) {
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

    public Mono<Void> resetPasswordByAdministrator(String email, ChangePassword changePassword) {
        // DB에 cognito username 을 저장해서 가져와야함

        return Mono.just(
            providerClient.adminSetUserPassword(
                AdminSetUserPasswordRequest.builder()
                    .username(email)
                    .userPoolId(userPoolId)
                    .password(changePassword.getNewPassword())
                    .build()
            ).join()
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
        return Mono.just(
            providerClient.initiateAuth(
                InitiateAuthRequest.builder()
                    .clientId(clientId)
                    .authParameters(authParameters)
                    .authFlow(authFlowType)
                    .build()
            ).whenComplete((response, throwable) -> {
                if (response == null) log.error(throwable.getMessage());
            }).join().authenticationResult()
        ).map(converter::converts);
    }

    private Mono<Token> adminInitiateAuth(Map<String, String> authParameters) {
        return Mono.just(
          providerClient.adminInitiateAuth(AdminInitiateAuthRequest.builder()
              .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
              .authParameters(authParameters)
              .clientId(clientId)
              .userPoolId(userPoolId)
              .build()
          ).whenComplete((response, throwable) -> {
              if (response == null) log.error(throwable.getMessage());
          }).join().authenticationResult()
        ).map(converter::converts);
    }

    /**
     * 사용자 정보 요청
     *
     * @param accessToken 토큰
     * @return 사용자 정보
     */
    private Mono<User> userInfo(String accessToken) {
        return Mono.just(
            providerClient.getUser(
                GetUserRequest.builder()
                    .accessToken(accessToken)
                    .build()).join()
        ).map(response -> converter.converts(response.userAttributes()));
    }
}
