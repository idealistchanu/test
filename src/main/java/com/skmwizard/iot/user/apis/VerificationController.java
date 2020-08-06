package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.Verification;
import com.skmwizard.iot.user.services.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.SecureRandom;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
class VerificationController {
    private final VerificationService verificationService;
    private final VerificationResourceConverter converter;

    @PostMapping("/verifications/sms")
    public Mono<VerificationResponse> sms(@RequestBody @Valid VerificationSmsRequest resource) {
        log.info("[POST] /verifications/sms request: {}", resource.toString());
        return verificationService
            .add(Verification.builder()
                .checker(resource.getPhoneNumber())
                .verificationCode(String.format("%05d", new SecureRandom().nextInt(1000000)))
                .build())
            //TODO SMS로 발신
            .map(converter::converts);
    }

    @PostMapping("/verifications/email")
    public Mono<VerificationResponse> email(@RequestBody @Valid VerificationEmailRequest resource) {
        log.info("[POST] /verifications/email request: {}", resource.toString());
        return verificationService
            .add(Verification.builder()
                .checker(resource.getEmail())
                .verificationCode(String.format("%05d", new SecureRandom().nextInt(1000000)))
                .build())
            //TODO Email로 발신
            .map(converter::converts);
    }

    @PostMapping("/verifications/confirm")
    public Mono<ResponseMessage> checkVerification(@RequestBody @Valid VerificationCheckRequest resource) {
        log.info("[POST] /verifications/confirm request: {}", resource.toString());
        return verificationService
            .exists(Verification.builder()
                .checker(resource.getPhoneNumber())
                .verificationCode(resource.getVerificationCode())
                .build())
            .flatMap(exists -> Mono.just(makeResponse()));
    }

    @PostMapping("/verifications/check")
    public Mono<ResponseMessage> checkVerificationWithRemove(@RequestBody @Valid VerificationCheckRequest resource) {
        log.info("[POST] /verifications/check request: {}", resource.toString());
        Verification verification = Verification.builder()
            .checker(resource.getPhoneNumber())
            .verificationCode(resource.getVerificationCode())
            .build();
        return verificationService.exists(verification)
            .doOnNext(exists -> verificationService.remove(verification).subscribe())
            .flatMap(exists -> Mono.just(makeResponse()));
    }

    private ResponseMessage makeResponse() {
        ResponseMessage response = new ResponseMessage();
        response.setMessage("인증번호 확인완료");
        response.setStatusCode(HttpStatus.OK.value());
        return response;
    }
}
