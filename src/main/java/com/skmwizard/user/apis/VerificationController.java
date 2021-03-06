package com.skmwizard.user.apis;

import com.skmwizard.user.services.Verification;
import com.skmwizard.user.services.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.SecureRandom;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
@Tag(name = "인증번호 관리")
@RestController
@RequiredArgsConstructor
@Slf4j
class VerificationController {
    private final VerificationService verificationService;
    private final VerificationResourceConverter converter;

    //TODO 인증번호 발송 후, 응답 정보 없어야 함.
    @Operation(summary = "인증번호 발송(SMS)", description = "SMS로 인증번호를 발송한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증번호 발송 성공",
            content = @Content(schema = @Schema(implementation = VerificationResponse.class))),
        @ApiResponse(responseCode = "400", description = "인증번호 발송 실패, 휴대폰 번호를 입력하세요")
    })
    @PostMapping("/verifications/sms")
    public Mono<VerificationResponse> sms(@RequestBody @Valid VerificationSmsRequest request) {
        log.info("[POST] /verifications/sms request: {}", request.toString());
        return verificationService
            .add(Verification.builder()
                .checker(request.getPhoneNumber())
                .verificationCode(String.format("%06d", new SecureRandom().nextInt(1000000)))
                .build())
            //TODO SMS로 발신
            .map(converter::converts);
    }

    //TODO 인증번호 발송 후, 응답 정보 없어야 함.
    @Operation(summary = "인증번호 발송(Email)", description = "이메일로 인증번호를 발송한다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증번호 발송 성공",
            content = @Content(schema = @Schema(implementation = VerificationResponse.class))),
        @ApiResponse(responseCode = "400", description = "인증번호 발송 실패, 이메일을 입력하세요")
    })
    @PostMapping("/verifications/email")
    public Mono<VerificationResponse> email(@RequestBody @Valid VerificationEmailRequest request) {
        log.info("[POST] /verifications/email request: {}", request.toString());
        return verificationService
            .add(Verification.builder()
                .checker(request.getEmail())
                .verificationCode(String.format("%06d", new SecureRandom().nextInt(1000000)))
                .build())
            //TODO Email로 발신
            .map(converter::converts);
    }

    @Operation(summary = "인증번호 확인(삭제 미포함)", description = "인증번호를 확인하다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증번호 확인 성공, 인증번호 확인 정보를 반환한다.",
            content = @Content(schema = @Schema(implementation = ResponseMessage.class))),
        @ApiResponse(responseCode = "400", description = "인증번호 확인 실패, 인증번호를 확인해주세요.")
    })
    @PostMapping("/verifications/confirm")
    public Mono<ResponseMessage> checkVerification(@RequestBody @Valid VerificationCheckRequest request) {
        log.info("[POST] /verifications/confirm request: {}", request.toString());
        return verificationService
            .exists(Verification.builder()
                .checker(request.getPhoneNumber())
                .verificationCode(request.getVerificationCode())
                .build())
            .flatMap(exists -> Mono.just(makeResponse()));
    }

    @Operation(summary = "인증번호 확인(삭제 포함)", description = "인증번호를 확인하다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "인증번호 확인 성공, 인증번호 확인 정보를 반환한다.",
            content = @Content(schema = @Schema(implementation = ResponseMessage.class))),
        @ApiResponse(responseCode = "400", description = "인증번호 확인 실패, 인증번호를 확인해주세요.")
    })
    @PostMapping("/verifications/check")
    public Mono<ResponseMessage> checkVerificationWithRemove(@RequestBody @Valid VerificationCheckRequest request) {
        log.info("[POST] /verifications/check request: {}", request.toString());
        Verification verification = Verification.builder()
            .checker(request.getPhoneNumber())
            .verificationCode(request.getVerificationCode())
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
