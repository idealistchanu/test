package com.skmwizard.iot.user.services.implementations;

import com.skmwizard.iot.user.services.Verification;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * @author jongduck_yoon
 * @since 2020-06-16
 */
@Component
@Validated
class VerificationConverter {
    Verification converts(@NotNull VerificationDocument document) {

        return Verification.builder()
            .phoneNumber(document.getPhoneNumber())
            .verificationCode(document.getVerificationCode())
            .build();
    }

    VerificationDocument converts(@NotNull Verification verification) {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(100000);
        String verificationCode = String.format("%05d", num);

        VerificationDocument document = new VerificationDocument();
        document.setUsername(verification.getUserName());
        document.setVerificationCode(verificationCode);
        document.setPhoneNumber(verification.getPhoneNumber());
        document.setLocalDateTime(LocalDateTime.now());
        return document;
    }
}
