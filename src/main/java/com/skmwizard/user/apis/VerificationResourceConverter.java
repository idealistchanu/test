package com.skmwizard.user.apis;

import com.skmwizard.user.services.Verification;
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
    VerificationResponse converts(@NotNull Verification verification) {
        VerificationResponse resource = new VerificationResponse();
        resource.setChecker(verification.getChecker());
        resource.setVerificationCode(verification.getVerificationCode());
        return resource;
    }
}
