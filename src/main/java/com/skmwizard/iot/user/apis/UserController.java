package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.ChangePassword;
import com.skmwizard.iot.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
class UserController {
    private final UserService userService;
    private final UserResourceConverter userResourceConverter;

    @GetMapping("/me")
    public Mono<UserResponse> getUserInfo(@RequestHeader("Authorization") String authorization) {
        return userService.getUserInfo(this.extractAccessToken(authorization))
            .onErrorMap(error -> CognitoIdentityProviderException.builder().build())
            .map(userResourceConverter::converts);
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> register(@RequestBody @Valid UserRequest userRequest) {
        return userService.signUp(userResourceConverter.converts(userRequest));
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
}
