package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.Verification;
import com.skmwizard.iot.user.services.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

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

    @PostMapping("/verifications")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<VerificationResource> add(@RequestHeader("username") String username, @RequestBody @Valid VerificationResource resource) {
        log.info("[POST] username: {}, verification: {}", username, resource.toString());
        Verification parameter = converter.converts(resource);
        return verificationService.add(username, parameter)
                .map(converter::converts);
    }

    @PostMapping("/verifications/check")
    public Mono<Void> checkVerification(@RequestHeader("username") String username, @RequestBody @Valid VerificationResource resource) {
        log.info("[POST] username: {}, verification: {}", username, resource.toString());
        Verification parameter =  converter.converts(resource);
        return verificationService.checkVerification(username, parameter);
    }
}
