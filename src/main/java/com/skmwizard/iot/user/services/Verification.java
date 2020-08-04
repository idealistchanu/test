package com.skmwizard.iot.user.services;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
@Getter
@Builder
@ToString
public class Verification {
    private String userName;
    private String phoneNumber;
    private String verificationCode;
}
