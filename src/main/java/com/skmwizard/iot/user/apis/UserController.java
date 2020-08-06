package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.ChangePassword;
import com.skmwizard.iot.user.services.UserService;
import com.skmwizard.iot.user.services.Verification;
import com.skmwizard.iot.user.services.VerificationService;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author ingu_ko
 * @since 2020-07-30
 */
@Tag(name = "사용자 관리")
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final VerificationService verificationService;

    @Operation(summary = "사용자 중복 확인", description = "사용자 중복 확인한다.")
    @Parameters({
        @Parameter(name = "email", description = "이메일", in = ParameterIn.PATH, example = "user_id@gmail.com")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용 중복 확인 성공, 사용 가능한 이메일입니다."),
        @ApiResponse(responseCode = "409", description = "사용자 중복 확인 실패, 이미 가입된 이메일입니다.")
    })
    @GetMapping("/users/{email}")
    public Mono<Void> check(@PathVariable("email") String email) {
        log.info("[GET] /users/{}", email);
        return userService.exists(email);
    }

    @Operation(summary = "사용자 이메일 찾기", description = "가입된 사용자의 이메일을 찾는다.")
    @Parameters({
        @Parameter(name = "username", description = "이름", in = ParameterIn.QUERY, example = "홍길동"),
        @Parameter(name = "phoneNumber", description = "휴대 전화번호", in = ParameterIn.QUERY, example = "01012348765"),
        @Parameter(name = "code", description = "인증번호", in = ParameterIn.QUERY, example = "123456")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 이메일 찾기 성공, 사용자의 이메일 정보를 반환한다.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserFindResponse.class)))),
        @ApiResponse(responseCode = "400", description = "사용자 이메일 찾기 실패, 가입한 사용자가 아닙니다. 또는 인증번호를 확인해주세요."),
    })
    @GetMapping("/users/find")
    public Mono<UserFindResponse> find(
        @RequestParam(name = "username") String username,
        @RequestParam(name = "phoneNumber") String phoneNumber,
        @RequestParam(name = "code") String code) {
        log.info("[GET] /users/find?username={}&phoneNumber={}&code={}", username, phoneNumber, code);
        Verification verification = Verification.builder().checker(phoneNumber).verificationCode(code).build();
        return Mono
            .zip(
                // 사용자 이메일 찾기
                userService.find(username, phoneNumber),
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
    @Parameters({
        @Parameter(name = "email", description = "이메일", in = ParameterIn.QUERY, example = "user_id@gmail.com"),
        @Parameter(name = "code", description = "인증번호", in = ParameterIn.QUERY, example = "123456")
    })
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공, 비밀번호 재설정 성공 정보를 반환한다.",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseMessage.class)))),
        @ApiResponse(responseCode = "400", description = "비밀번호 재설정 실패, 가입한 이메일이 아닙니다. 또는 인증번호를 확인해주세요."),
    })
    @PutMapping("/users/change-password")
    public Mono<ResponseMessage> changePassword(
        @RequestParam(name = "email") String email,
        @RequestParam(name = "code") String code,
        @RequestBody PasswordResetRequest request) {
        log.info("[GET] /users/change-password?email={}&code={} request: {}", email, code, request);

        Verification verification = Verification.builder()
            .checker(email)
            .verificationCode(code)
            .build();
        // 이메일 인증 확인
        return verificationService.exists(verification)
            .doOnNext(exists ->
                // 사용자 이메일이 있으면, 해당 비밀번호 재설정
                userService.get(email)
                    .doOnSuccess(user ->
                        userService.resetPassword(email, request.getResetPassword()).subscribe())
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
}
