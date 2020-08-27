package com.skmwizard.user.services;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * @author jongduck_yoon
 * @since 2020-06-11
 */
@Getter
@Builder
@ToString
public class Verification {
    private final String checker;
    private final String verificationCode;
    private final LocalDateTime createdDatetime;
}
