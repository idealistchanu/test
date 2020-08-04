package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.AgreeReceiveService;
import com.skmwizard.iot.user.services.ChangePassword;
import com.skmwizard.iot.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
class AccountController {
    private final UserService userService;
    private final AgreeReceiveService agreeReceiveService;
    private final UserResourceConverter userResourceConverter;
    private final AgreeReceiveResourceConverter agreeReceiveConverter;

    @GetMapping("/me")
    public Mono<UserResponse> getUserInfo(@RequestHeader HttpHeaders httpHeaders) {
        debuggingHeader(httpHeaders);
        return null;
        //return userService.getUserInfo(this.extractAccessToken(authorization))
        //    .onErrorMap(error -> CognitoIdentityProviderException.builder().build())
        //    .map(userResourceConverter::converts);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> register(@RequestBody @Valid UserRequest userRequest) {
        return Mono.when(
            // 사용자 등록 (Cognito, NoSQL)
            userService.signUp(userResourceConverter.converts(userRequest)),
            // 수신 동의 등록
            Flux.fromIterable(userRequest.getAgreeList())
                .doOnNext(agree ->
                    agreeReceiveService.add(userRequest.getEmail(), agreeReceiveConverter.converts(agree))
                        .subscribe()
                )
        ).then();
    }

    @PutMapping("/me")
    public Mono<UserResponse> editUserInfo(@RequestHeader("Authorization") String authorization, @RequestBody UserRequest userRequest) {
        log.info("userResource: {}", userRequest);
        return userService.updateUserInfo(this.extractAccessToken(authorization), userResourceConverter.converts(userRequest))
            .map(userResourceConverter::converts);
    }

    @PutMapping("/me/password")
    public Mono<Void> changePassword(@RequestHeader("Authorization") String authorization, @RequestBody @Valid PasswordChangeRequest request) {
        ChangePassword changePassword = new ChangePassword(request.getCurrentPassword(), request.getNewPassword());
        return userService.resetPassword(this.extractAccessToken(authorization), changePassword);
    }

    private String extractAccessToken(String authorization) {
        return authorization.replace("Bearer ", "");
    }

    private void debuggingHeader(HttpHeaders httpHeaders) {
        for (String key : httpHeaders.keySet()) {
            log.debug("{}:{}", key, httpHeaders.getFirst(key));
        }
    }
}
