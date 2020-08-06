package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.ChangePassword;
import com.skmwizard.iot.user.services.UserService;
import com.skmwizard.iot.user.services.Verification;
import com.skmwizard.iot.user.services.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author ingu_ko
 * @since 2020-07-30
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final VerificationService verificationService;

    @RequestMapping("/users/{email}")
    public Mono<Void> check(@PathVariable("email") String email) {
        log.info("[GET] /users/{}", email);
        return userService.exists(email);
    }

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

    @PutMapping("/users/change-password")
    public Mono<ResponseMessage> changePassword(
        @RequestParam(name = "email") String email,
        @RequestParam(name = "code") String code,
        @RequestBody PasswordChangeRequest request) {
        log.info("[GET] /users/change-password?email={}&code={} request: {}", email, code, request);

        Verification verification = Verification.builder()
            .checker(email)
            .verificationCode(code)
            .build();
        return verificationService.exists(verification)
            .doOnNext(exists ->
                userService.get(email)
                    .doOnSuccess(user ->
                        userService.changePassword(email, new ChangePassword(request.getCurrentPassword(), request.getNewPassword())).subscribe())
                    .subscribe()
            )
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
