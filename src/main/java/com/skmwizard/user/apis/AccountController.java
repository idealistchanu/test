package com.skmwizard.user.apis;

import com.skmwizard.user.services.*;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private final VerificationService verificationService;

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
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID", schema = @Schema(type = "string"), required = true)
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공, 사용자 정보를 반환한다.",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패, Access Token을 확인해주세요.")
    })
    @GetMapping("/me")
    public Mono<UserResponse> getUserInfo(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("email");
        log.info("username: {}", username);
        return userService.get(username)
            .map(userResourceConverter::converts);
    }

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID", schema = @Schema(type = "string"), required = true)
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 정보 수정 성공, 사용자 정보가 수정되어 반환한다.",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "사용자 등록 실패, 잘못된 요청입니다."),
        @ApiResponse(responseCode = "401", description = "인증 실패, Access Token을 확인해주세요.")
    })
    @PutMapping("/me")
    public Mono<UserResponse> editUserInfo(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid UserUpdateRequest request) {
        String username = jwt.getClaimAsString("email");
        log.info("username: {}, userResource: {}", username, request);
        return userService.updateUserInfo(username, userResourceConverter.converts(request))
            .map(userResourceConverter::converts);
    }

    @Operation(summary = "사용자 비밀번호 재설정", description = "사용자 비밀번호를 재설정한다.")
    @Parameters({
        @Parameter(name = "Authorization", description = "인증 토큰", in = ParameterIn.HEADER, example = "Authorization Bearer INVALID", schema = @Schema(type = "string"), required = true)
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 비밀번호 변경 성공."),
        @ApiResponse(responseCode = "400", description = "사용자 비밀번호 변경 실패, 잘못된 요청입니다."),
        @ApiResponse(responseCode = "401", description = "인증 실패, Access Token을 확인해주세요. 또는 현재 비밀번호를 확인해주세요.")
    })
    @PutMapping("/me/password")
    public Mono<Void> changePassword(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid PasswordChangeRequest request) {
        String username = jwt.getClaimAsString("cognito:username");
        log.info("username: {}", username);
        return userService.resetPassword(username, request.getResetPassword());
    }

    @Operation(summary = "사용자 중복 확인", description = "사용자 중복 확인한다.")
    @Parameters({
        @Parameter(name = "email", description = "이메일", in = ParameterIn.QUERY, example = "user_id@gmail.com")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용 중복 확인 성공, 사용 가능한 이메일입니다."),
        @ApiResponse(responseCode = "409", description = "사용자 중복 확인 실패, 이미 가입된 이메일입니다.")
    })
    @GetMapping("/users/check")
    public Mono<Void> check(@RequestParam(name = "email") String email) {
        log.info("[GET] /users/check?email={}", email);
        // TODO 이메일 형식을 확인 필요
        return userService.exists(email);
    }

    @Operation(summary = "사용자 이메일 찾기", description = "가입된 사용자의 이메일을 찾는다.")
    @Parameters({
        @Parameter(name = "name", description = "이름", in = ParameterIn.QUERY, example = "홍길동"),
        @Parameter(name = "phoneNumber", description = "휴대 전화번호", in = ParameterIn.QUERY, example = "01012348765"),
        @Parameter(name = "code", description = "인증번호", in = ParameterIn.QUERY, example = "123456")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 이메일 찾기 성공, 사용자의 이메일 정보를 반환한다.",
            content = @Content(schema = @Schema(implementation = UserFindResponse.class))),
        @ApiResponse(responseCode = "400", description = "사용자 이메일 찾기 실패, 가입한 사용자가 아닙니다. 또는 인증번호를 확인해주세요."),
    })
    @GetMapping("/users/find")
    public Mono<UserFindResponse> find(
        @RequestParam(name = "name") String name,
        @RequestParam(name = "phoneNumber") String phoneNumber,
        @RequestParam(name = "code") String code) {
        log.info("[GET] /users/find?username={}&phoneNumber={}&code={}", name, phoneNumber, code);
        Verification verification = Verification.builder().checker(phoneNumber).verificationCode(code).build();
        return Mono
            .zip(
                // 사용자 이메일 찾기
                userService.find(name, phoneNumber),
                // 휴대폰 인증 확인 후, 인증 정보 삭제
                verificationService.exists(verification)
                    .doOnNext(exists -> verificationService.remove(verification).subscribe())
            ).flatMap(objects -> {
                UserFindResponse response = new UserFindResponse();
                response.setEmail(objects.getT1().getEmail());
                return Mono.just(response);
            });
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 재설정한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공, 비밀번호 재설정 성공 정보를 반환한다.",
            content = @Content(schema = @Schema(implementation = ResponseMessage.class))),
        @ApiResponse(responseCode = "400", description = "비밀번호 재설정 실패, 가입한 이메일이 아닙니다. 또는 인증번호를 확인해주세요."),
    })
    @PutMapping("/users/reset-password")
    public Mono<ResponseMessage> resetPassword(
        @RequestBody PasswordResetRequest request) {
        log.info("[GET] /users/reset-password request: {}", request);

        Verification verification = Verification.builder()
            .checker(request.getEmail())
            .verificationCode(request.getCode())
            .build();
        // 이메일 인증 확인
        return verificationService.exists(verification)
            .doOnNext(exists ->
                // 사용자 이메일이 있으면, 해당 비밀번호 재설정
                userService.get(request.getEmail())
                    .doOnSuccess(user ->
                        userService.resetPassword(request.getEmail(), request.getResetPassword()).subscribe())
                    .subscribe()
            )
            // 이메일 인증 확인 후, 인증 정보 삭제
            .doOnSuccess(exists ->
                verificationService.remove(verification).subscribe())
            .map(exists -> {
                ResponseMessage response = new ResponseMessage();
                response.setMessage("Password change is complete.");
                response.setStatusCode(HttpStatus.OK.value());
                return response;
            });
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
