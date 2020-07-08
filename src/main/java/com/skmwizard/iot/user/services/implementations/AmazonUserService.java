package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.ChangePassword;
import com.skmwizard.iot.user.services.Token;
import com.skmwizard.iot.user.services.User;
import com.skmwizard.iot.user.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
@Slf4j
class AmazonUserService implements UserService {
    private final CognitoIdentityProviderClient providerClient;
    private final AmazonUserConverter converter;
    private final UserRepository userRepository;
    private final String clientId;
    private final String userPoolId;

    public AmazonUserService(
            CognitoIdentityProviderClient providerClient,
            AmazonUserConverter converter,
            UserRepository userRepository,
            @Value("${aws.cognito.webclient.clientId}") String clientId,
            @Value("${aws.cognito.userPoolId}") String userPoolId) {
        this.providerClient = providerClient;
        this.converter = converter;
        this.userRepository = userRepository;
        this.clientId = clientId;
        this.userPoolId = userPoolId;
    }

    @Override
    public User signUp(User user) {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(clientId)
                .username(user.getEmail())
                .password(user.getPassword())
                .userAttributes(converter.converts(user))
                .build();

        try {
            SignUpResponse signUpResponse = providerClient.signUp(signUpRequest);
            log.info("response code: {}", signUpResponse.sdkHttpResponse().statusCode());

            if (signUpResponse.sdkHttpResponse().isSuccessful()) {
                log.info("Create User: {}", user.getEmail());

                AdminConfirmSignUpRequest adminConfirmSignUpRequest = AdminConfirmSignUpRequest.builder()
                        .username(user.getEmail())
                        .userPoolId(userPoolId)
                        .build();

                if (user.getStatus().equals("AUTH") && providerClient.adminConfirmSignUp(adminConfirmSignUpRequest).sdkHttpResponse().isSuccessful()) {
                    log.info("Signing up confirmed.");
                    UserDocument entity = new UserDocument(user.getEmail(), user.getNickname());
                    userRepository.save(entity);
                }
                return user;
            }

            throw InternalErrorException.builder().build();
        } catch (CognitoIdentityProviderException e) {
            log.error("{}", e.getMessage().replaceAll("\\(.*\\)", ""));
            throw e;
        }
    }

    @Override
    public Token login(User user) {
        Map<String, String> auth = new ConcurrentHashMap<>();
        auth.put("USERNAME", user.getEmail());
        auth.put("PASSWORD", user.getPassword());

        return initiateAuth(auth, AuthFlowType.USER_PASSWORD_AUTH)
                .orElseThrow(InternalErrorException.builder()::build);
    }

    @Override
    public void logout(String accessToken) {
        GlobalSignOutRequest globalSignOutRequest = GlobalSignOutRequest.builder()
                .accessToken(accessToken)
                .build();

        providerClient.globalSignOut(globalSignOutRequest);
    }

    @Override
    public Optional<User> getUserInfo(String accessToken) {
        GetUserRequest getUserRequest = GetUserRequest.builder().accessToken(accessToken).build();
        GetUserResponse response = providerClient.getUser(getUserRequest);

        return Optional.of(converter.converts(response.userAttributes()));
    }

    @Override
    public User updateUserInfo(String accessToken, User user) {
        UpdateUserAttributesRequest updateUserAttributesRequest = UpdateUserAttributesRequest.builder()
                .accessToken(accessToken)
                .userAttributes(
                        user.getNickname() == null ? null : AttributeType.builder().name("custom:nickname").value(user.getNickname()).build(),
                        user.getAddress() == null ? null : AttributeType.builder().name("custom:address").value(user.getAddress()).build()
                )
                .build();

        UpdateUserAttributesResponse response = providerClient.updateUserAttributes(updateUserAttributesRequest);
        if (response.sdkHttpResponse().isSuccessful()) {
            return getUserInfo(accessToken)
                    .map(existUser -> {
                        userRepository.existsById(existUser.getEmail())
                                .flatMap(exists -> {
                                    if (exists.equals(Boolean.TRUE)) {
                                        return null;
                                    }
                                    UserDocument userDocument = new UserDocument();
                                    userDocument.setNickname(existUser.getNickname());

                                    return userRepository.save(userDocument);
                                });
                        return existUser;
                    }).orElseThrow(NoSuchElementException::new);
        }

        throw InternalErrorException.builder().build();
    }

    @Override
    public void resetPassword(String accessToken, ChangePassword changePassword) {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.builder()
                .accessToken(accessToken)
                .previousPassword(changePassword.getOldPassword())
                .proposedPassword(changePassword.getNewPassword())
                .build();

        providerClient.changePassword(changePasswordRequest);
    }

    @Override
    public Token refreshToken(String refreshToken) {
        Map<String, String> auth = new ConcurrentHashMap<>();
        auth.put("REFRESH_TOKEN", refreshToken);

        return initiateAuth(auth, AuthFlowType.REFRESH_TOKEN_AUTH)
                .orElseThrow(NotAuthorizedException.builder()::build);
    }

    /**
     * 인증 FlowType 에 대한 Token 요청
     *
     * @param authParameters 인증 파라미터
     * @param authFlowType   인증 플로우 타입
     * @return 토큰
     */
    private Optional<Token> initiateAuth(Map<String, String> authParameters, AuthFlowType authFlowType) {
        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                .clientId(clientId)
                .authParameters(authParameters)
                .authFlow(authFlowType)
                .build();

        InitiateAuthResponse response = providerClient.initiateAuth(initiateAuthRequest);
        if (response.sdkHttpResponse().isSuccessful()) {
            AuthenticationResultType authenticationResultType = response.authenticationResult();
            return Optional.of(converter.converts(authenticationResultType));
        }

        return Optional.empty();
    }
}
