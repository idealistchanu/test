package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.Verification;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * @author jongduck_yoon
 * @since 2020-06-16
 */
@Component
@Validated
class VerificationConverter {
    Verification converts(@NotNull VerificationDocument document) {
        return Verification.builder()
            .checker(document.getChecker())
            .verificationCode(document.getVerificationCode())
            .createdDatetime(document.getCreatedDatetime())
            .build();
    }

    VerificationDocument converts(Verification verification) {
        VerificationDocument document = new VerificationDocument();
        document.setChecker(verification.getChecker());
        document.setVerificationCode(verification.getVerificationCode());
        document.setCreatedDatetime(verification.getCreatedDatetime());
        return document;
    }
}
