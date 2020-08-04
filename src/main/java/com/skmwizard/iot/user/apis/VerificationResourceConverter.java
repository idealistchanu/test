package com.skmwizard.iot.user.apis;

import com.skmwizard.iot.user.services.Verification;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * @author jongduck_yoon
 * @since 2020-06-12
 */
@Component
@Validated
class VerificationResourceConverter {

    Verification converts(@NotNull VerificationResource resource) {
        return Verification.builder()
            .phoneNumber(resource.getPhoneNumber())
            .verificationCode(resource.getVerificationCode())
            .build();
    }

    VerificationResource converts(@NotNull Verification verification) {
        VerificationResource resource = new VerificationResource();
        resource.setPhoneNumber(verification.getPhoneNumber());
        resource.setVerificationCode(verification.getVerificationCode());
        resource.setUsername(verification.getUserName());

        return resource;
    }
}
