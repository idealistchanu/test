package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.User;
import com.skmwizard.iot.user.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@Slf4j
class AuthenticationController {
    private final UserService userService;
    private final TokenResourceConverter converter;

    @PostMapping("/login")
    public Mono<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("email: {}", loginRequest.getEmail());
        return userService
            .login(
                User.builder()
                    .email(loginRequest.getEmail())
                    .password(loginRequest.getPassword())
                    .build())
            .map(converter::converts);
    }

    @PostMapping("/logout")
    public Mono<Void> logout(@RequestHeader("Authorization") String accessToken) {
        accessToken = accessToken.replace("Bearer ", "");
        return userService.logout(accessToken);
    }

    @PostMapping("/token/refresh")
    public Mono<TokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        return userService
            .refreshToken(
                refreshTokenRequest.getRefreshToken())
            .map(converter::converts);
    }
}
