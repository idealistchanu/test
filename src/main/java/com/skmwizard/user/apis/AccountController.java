package com.skmwizard.user.apis;

import com.skmwizard.user.services.AgreeReceiveService;
import com.skmwizard.user.services.ChangePassword;
import com.skmwizard.user.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@Tag(name = "계정 관리")
@RestController
@RequiredArgsConstructor
@Slf4j
class AccountController {
    private final UserService userService;
    private final AgreeReceiveService agreeReceiveService;
    private final UserResourceConverter userResourceConverter;
    private final AgreeReceiveResourceConverter agreeReceiveConverter;

    @Operation(summary = "사용자 등록", description = "사용자를 동록한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 등록 성공."),
        @ApiResponse(responseCode = "400", description = "사용자 등록 실패, 잘못된 요청입니다."),
        @ApiResponse(responseCode = "409", description = "사용자 등록 실패, 이미 가입된 이메일입니다.")
    })
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

    @Operation(summary = "사용자 정보 조회", description = "사용자 정보를 조회한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공, 사용자 정보를 반환한다.",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패, Access Token을 확인해주세요.")
    })
    @GetMapping("/me")
    public Mono<UserResponse> getUserInfo(@RequestHeader("Authorization") String authorization) {
        return userService.getUserInfo(this.extractAccessToken(authorization == null ? "" : authorization))
            .map(userResourceConverter::converts);
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공, 사용자 정보가 수정되어 반환한다.",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "사용자 등록 실패, 잘못된 요청입니다."),
        @ApiResponse(responseCode = "401", description = "인증 실패, Access Token을 확인해주세요.")
    })
    @PutMapping("/me")
    public Mono<UserResponse> editUserInfo(@RequestHeader("Authorization") String authorization, @RequestBody UserRequest userRequest) {
        log.info("userResource: {}", userRequest);
        return userService.updateUserInfo(this.extractAccessToken(authorization), userResourceConverter.converts(userRequest))
            .map(userResourceConverter::converts);
    }

    @Operation(summary = "사용자 비밀번호 변경", description = "사용자 비밀번호를 변경한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 비밀번호 변경 성공."),
        @ApiResponse(responseCode = "400", description = "사용자 비밀번호 변경 실패, 잘못된 요청입니다."),
        @ApiResponse(responseCode = "401", description = "인증 실패, Access Token을 확인해주세요. 또는 현재 비밀번호를 확인해주세요.")
    })
    @PutMapping("/me/password")
    public Mono<Void> changePassword(@RequestHeader("Authorization") String authorization, @RequestBody @Valid PasswordChangeRequest request) {
        ChangePassword changePassword = new ChangePassword(request.getCurrentPassword(), request.getNewPassword());
        return userService.changePassword(this.extractAccessToken(authorization), changePassword);
    }

    /**
     * 인증 토큰 분리
     *
     * @param authorization
     * @return
     */
    private String extractAccessToken(String authorization) {
        return authorization.replace("Bearer ", "");
    }

    /**
     * Headers 디버그용 로그 표시
     *
     * @param httpHeaders
     */
    private void debuggingHeader(HttpHeaders httpHeaders) {
        for (String key : httpHeaders.keySet()) {
            log.debug("{}:{}", key, httpHeaders.getFirst(key));
        }
    }
}
