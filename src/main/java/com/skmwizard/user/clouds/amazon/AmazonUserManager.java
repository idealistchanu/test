package com.skmwizard.user.clouds.amazon;

import com.skmwizard.user.clouds.CloudUserManager;
import com.skmwizard.user.services.Token;
import com.skmwizard.user.services.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderAsyncClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ingu_ko
 * @since 2020-10-12
 */
@Component
@Primary
@Slf4j
public class AmazonUserManager implements CloudUserManager {
    private final CognitoIdentityProviderAsyncClient providerClient;
    private final String clientId;
    private final String userPoolId;

    public AmazonUserManager(
        CognitoIdentityProviderAsyncClient providerClient,
        @Value("${aws.cognito.webclient.clientId}") String clientId,
        @Value("${aws.cognito.userPoolId}") String userPoolId) {
        this.providerClient = providerClient;
        this.clientId = clientId;
        this.userPoolId = userPoolId;
    }

    @Override
    public Mono<Token> login(User user) {
        Map<String, String> auth = new ConcurrentHashMap<>();
        auth.put("USERNAME", user.getEmail());
        auth.put("PASSWORD", user.getPassword());

        return initiateAuth(auth, AuthFlowType.USER_PASSWORD_AUTH)
            .map(response -> Token.builder()
                .accessToken(response.idToken())
                .refreshToken(response.refreshToken())
                .tokenType(response.tokenType())
                .expiresIn(response.expiresIn())
                .build());
    }

    @Override
    public Mono<Boolean> logout(String username) {
        return Mono.fromFuture(
            providerClient.adminUserGlobalSignOut(
                AdminUserGlobalSignOutRequest.builder()
                    .username(username)
                    .userPoolId(userPoolId)
                    .build()
            )
        ).map(response -> Optional.ofNullable(response).isPresent());
    }

    @Override
    public Mono<Token> refreshToken(String refreshToken) {
        Map<String, String> auth = new ConcurrentHashMap<>();
        auth.put("REFRESH_TOKEN", refreshToken);

        return initiateAuth(auth, AuthFlowType.REFRESH_TOKEN_AUTH)
            .map(response -> Token.builder()
                .accessToken(response.idToken())
                .refreshToken(response.refreshToken())
                .tokenType(response.tokenType())
                .expiresIn(response.expiresIn())
                .build());
    }

    @Override
    public Mono<User> register(User user) {

        List<AttributeType> attributeTypes = new LinkedList<>();
        attributeTypes.add(AttributeType.builder().name("name")
            .value(Optional.ofNullable(user.getName()).orElse(" "))
            .build());
        attributeTypes.add(AttributeType.builder().name("email")
            .value(Optional.ofNullable(user.getEmail()).orElse(" "))
            .build());

        return Mono.fromFuture(
            // AWS 사용자 등록
            providerClient.signUp(
                SignUpRequest.builder()
                    .clientId(clientId)
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .userAttributes(attributeTypes)
                    .build())
                .whenComplete((signUpResponse, throwable) -> {
                    if (signUpResponse != null) {
                        log.info(signUpResponse.toString());
                    } else {
                        Mono.error(throwable).subscribe();
                    }
                })
        ).doOnNext(response -> {
            // AWS 사용자 가입 승인
            Mono.fromFuture(
                providerClient.adminConfirmSignUp(
                    AdminConfirmSignUpRequest.builder()
                        .username(user.getEmail())
                        .userPoolId(userPoolId)
                        .build())
                    .whenComplete((adminConfirmSignUpResponse, throwable) -> {
                        if (adminConfirmSignUpResponse != null) {
                            log.info(adminConfirmSignUpResponse.toString());
                        } else {
                            Mono.error(throwable).subscribe();
                        }
                    })
            );
        }).map(response -> User.builder().email(user.getEmail()).name(user.getName()).build());
    }

    @Override
    public Mono<User> edit(User user) {
        return Mono.fromFuture(
            providerClient.adminUpdateUserAttributes(
                AdminUpdateUserAttributesRequest.builder()
                    .username(user.getEmail())
                    .userPoolId(userPoolId)
                    .userAttributes(
                        AttributeType.builder()
                            .name("name").value(Optional.ofNullable(user.getName()).orElse(" "))
                            .build())
                    .build())
        ).map(response -> User.builder().email(user.getEmail()).name(user.getName()).build());
    }

    @Override
    public Mono<Boolean> resetPassword(String username, String resetPassword) {
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
        ).map(response -> Optional.ofNullable(response).isPresent());
    }

    @Override
    public Mono<Boolean> remove(String username) {
        return Mono.fromFuture(
            providerClient.adminDeleteUser(
                AdminDeleteUserRequest.builder()
                    .username(username)
                    .userPoolId(userPoolId)
                    .build())
        ).map(response -> Optional.ofNullable(response).isPresent());
    }

    /**
     * 인증 FlowType 에 대한 Token 요청
     *
     * @param authParameters 인증 파라미터
     * @param authFlowType   인증 플로우 타입
     * @return 토큰
     */
    private Mono<AuthenticationResultType> initiateAuth(Map<String, String> authParameters, AuthFlowType authFlowType) {
        return Mono.fromFuture(
            providerClient.initiateAuth(
                InitiateAuthRequest.builder()
                    .clientId(clientId)
                    .authParameters(authParameters)
                    .authFlow(authFlowType)
                    .build()
            )
        ).map(InitiateAuthResponse::authenticationResult);
    }
}
