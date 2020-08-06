package com.skmwizard.iot.user.apis;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ingu_ko
 * @since 2020-08-05
 */
@Getter
@Setter
@ToString
class VerificationResponse {
    private String checker;
    private String verificationCode;
}
