package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.ChangePassword;
import com.skmwizard.iot.user.services.User;
import com.skmwizard.iot.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CognitoIdentityProviderException;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
class UserController {
    private final UserService userService;
    private final UserResourceConverter userResourceConverter;

    @GetMapping("/me")
    public UserResource getUserInfo(@RequestHeader("Authorization") String authorization) {
        Optional<User> userOptional = userService.getUserInfo(this.extractAccessToken(authorization));
        return userOptional.map(userResourceConverter::converts).orElseThrow(CognitoIdentityProviderException.builder()::build);
    }

    @PostMapping("/signup")
    public UserResource register(@RequestBody @Valid UserResource userResource) {
        User user = userResourceConverter.converts(userResource);
        User registeredUser = userService.signUp(user);
        return userResourceConverter.converts(registeredUser);
    }

    @PutMapping("/me")
    public UserResource editUserInfo(@RequestHeader("Authorization") String authorization, @RequestBody UserResource userResource) {
        log.info("userResource: {}", userResource);
        User user = userService.updateUserInfo(this.extractAccessToken(authorization), userResourceConverter.converts(userResource));
        return userResourceConverter.converts(user);
    }

    @PutMapping("/me/password")
    public void changePassword(@RequestHeader("Authorization") String authorization, @RequestBody @Valid PasswordChangeRequest request) {
        ChangePassword changePassword = new ChangePassword(request.getCurrentPassword(), request.getNewPassword());
        userService.resetPassword(this.extractAccessToken(authorization), changePassword);
    }

    private String extractAccessToken(String authorization) {
        return authorization.replace("Bearer ", "");
    }
}
