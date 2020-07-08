package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.Token;
import com.skmwizard.iot.user.services.User;
import com.skmwizard.iot.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
class AuthenticationController {
    private final UserService userService;
    private final TokenResourceConverter tokenResourceConverter;

    @PostMapping("/login")
    public TokenResource login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("email: {}", loginRequest.getEmail());

        User user = User.builder()
                .email(loginRequest.getEmail())
                .password(loginRequest.getPassword())
                .build();
        Token token = userService.login(user);

        return tokenResourceConverter.converts(token);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String accessToken) {
        accessToken = accessToken.replace("Bearer ", "");
        userService.logout(accessToken);
    }

    @PostMapping("/token/refresh")
    public TokenResource refreshToken(@RequestBody TokenResource tokenResource) {
        Token token = userService.refreshToken(tokenResource.getRefreshToken());
        return tokenResourceConverter.converts(token);
    }
}
