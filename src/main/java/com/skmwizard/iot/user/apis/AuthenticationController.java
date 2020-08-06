package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.User;
import com.skmwizard.iot.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Tag(name = "인증 관리")
@RestController
@RequiredArgsConstructor
@Slf4j
class AuthenticationController {
    private final UserService userService;
    private final TokenResourceConverter converter;

    @Operation(summary = "로그인", description = "로그인 한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그인 성공, Token 정보를 반환한다.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TokenResponse.class)))),
        @ApiResponse(responseCode = "401", description = "로그인 실패, 등록되지 않은 이메일이거나, 비밀번호를 잘못 입력하셨습니다."),
        @ApiResponse(responseCode = "404", description = "로그인 실패, 등록되지 않은 이메일입니다."),
    })
    @PostMapping("/login")
    public Mono<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.info("[POST] /login request {}", loginRequest.getEmail());
        return userService
            .login(
                User.builder()
                    .email(loginRequest.getEmail())
                    .password(loginRequest.getPassword())
                    .build())
            .map(converter::converts);
    }

    @Operation(summary = "로그 아웃", description = "로그 아웃 한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그 아웃 성공."),
        @ApiResponse(responseCode = "401", description = "로그 아웃 실패, Access Token을 확인해주세요."),
    })
    @PostMapping("/logout")
    public Mono<Void> logout(@RequestHeader("Authorization") String accessToken) {
        log.info("[POST] /logout");
        accessToken = accessToken.replace("Bearer ", "");
        return userService.logout(accessToken);
    }

    @Operation(summary = "토큰 갱신", description = "토큰을 갱신한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TokenResponse.class)))),
        @ApiResponse(responseCode = "401", description = "토큰 갱신 실패, Refresh Token을 확인해주세요."),
    })
    @PostMapping("/token/refresh")
    public Mono<TokenResponse> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) {
        log.info("[POST] /token/refresh");
        return userService
            .refreshToken(
                refreshTokenRequest.getRefreshToken())
            .map(converter::converts);
    }
}
